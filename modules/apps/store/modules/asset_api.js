/*
 * Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
    Descripiton:The apis-asset-manager is used to retriew assets for api calls
    Filename: asset_api.js
    Created Date: 7/24/2014
*/
var api = {};
var responseProcessor = require('utils').response;
(function(api) {
    var fieldExpansion = function(options, req, res, session) {
        var fields = options.fields;
        var artifacts = options.assets;
        var extendedAssetTemplate = {};
        for (var val in fields) {
            var key = fields[val];
            var value = '';
            extendedAssetTemplate[key] = value;
        }
        var newArtifactTemplateString = stringify(extendedAssetTemplate);
        var modifiedAssets = [];
        for (var j in artifacts) {
            var artifactObject = parse(newArtifactTemplateString);
            for (var i in extendedAssetTemplate) {
                if (artifacts[j][i]) {
                     artifactObject[i] = artifacts[j][i];
                } else {
                    artifactObject[i] = artifacts[j].attributes[i];
                }
            }
            modifiedAssets.push(artifactObject);
        }
        //print(modifiedArtifacts);
        return modifiedAssets;
    };
    var putInStorage = function(options, asset, am, req, session) {
        var resourceFields = am.getAssetResources();
        var ref = require('utils').file;
        var storageModule = require('/modules/data/storage.js').storageModule();
        var storageConfig = require('/config/storage.json');

        var storageManager = new storageModule.StorageManager({
            context: 'storage',
            isCached: false,
            connectionInfo: {
                dataSource: storageConfig.dataSource
            }
        });
        var resource = {};
        var extension = '';
        var uuid;
        var key;
        //Get all of the files that have been sent in the request
        var files = request.getAllFiles();
        if (!files) {
            log.debug('User has not provided any resources such any new images or files when updating the asset with id ' + asset.id);
            return;
        }
        for (var index in resourceFields) {
            key = resourceFields[index];
            if (files[key]) {
                resource = {};
                resource.file = files[key];
                extension = ref.getExtension(files[key]);
                resource.contentType = ref.getMimeType(extension);
                uuid = storageManager.put(resource);
                asset.attributes[key] = uuid;
            }
        }
    };
    var putInOldResources = function(original, asset, am) {
        var resourceFields = am.getAssetResources();
        var resourceField;
        for (var index in resourceFields) {
            resourceField = resourceFields[index];
            //If the asset attribute value is null then use the old resource
            if ((!asset.attributes[resourceField]) || (asset.attributes[resourceField] == '')) {
                log.debug('Copying old resource attribute value for ' + resourceField);
                asset.attributes[resourceField] = original.attributes[resourceField];
            }
        }
    };
    var isPresent=function(key,data){
        if((data[key])||(data[key]=='')){
            return true;
        }
        return false;
    };
    var putInUnchangedValues = function(original, asset, sentData) {
        for (var key in original.attributes) {
            //We need to add the original values if the attribute was not present in the data object sent from the client
            //and it was not deleted by the user (the sent data has an empty value)
            if (((!asset.attributes[key]) || (asset.attributes[key].length==0)) && (!isPresent(key,sentData))) {
                log.debug('Copying old attribute value for '+key);
                asset.attributes[key] = original.attributes[key];
            }
        }
    };
    api.create = function(options, req, res, session) {
        var asset = require('rxt').asset;
        var am = asset.createUserAssetManager(session, options.type);
        var assetReq = req.getAllParameters('UTF-8');
        var asset = am.importAssetFromHttpRequest(assetReq);
        putInStorage(options, asset, am, req, session);
        try {
            am.create(asset);
        } catch (e) {
            log.error('Asset of type: ' + options.type + ' was not created due to ' + e);
            //print(responseProcessor.buildErrorResponse(500, 'Failed to create asset of type: ' + options.type));
            return null;
        }
        var isLcAttached = am.attachLifecycle(asset);
        //Check if the lifecycle was attached
        if (isLcAttached) {
            var synched = am.synchAsset(asset);
            if (synched) {
                am.invokeDefaultLcAction(asset);
            } else {
                log.warn('Failed to invoke default action as the asset could not be synched.')
            }
        }
        return asset;
    };
    api.update = function(options, req, res, session) {
        var asset = require('rxt').asset;
        var am = asset.createUserAssetManager(session, options.type);
        var assetReq = req.getAllParameters('UTF-8');
        var asset = am.importAssetFromHttpRequest(assetReq);
        asset.id = options.id;
        putInStorage(options, asset, am, req, session);
        var original = am.get(options.id);
        putInOldResources(original, asset, am);
        putInUnchangedValues(original, asset, assetReq);
        //If the user has not uploaded any new resources then use the old resources
        if(!asset.name){
            asset.name=am.getName(asset);
        }
        try {
            am.update(asset);
        } catch (e) {
            log.debug('Failed to update the asset ' + stringify(asset));
            log.debug(e);
            asset = null;
        }
        return asset;
    };
    api.search = function(options, req, res, session) {
        var asset = require('rxt').asset;
        var assetManager = asset.createUserAssetManager(session, options.type);
        var sort = (request.getParameter("sort") || '');
        var sortOrder = DEFAULT_PAGIN.sortOrder;
        if (sort) {
            var order = sort.charAt(0);
            if (order == '+' || order == ' ') {
                sortOrder = 'ASC';
                sort = sort.slice(1);
            } else if (order == '-') {
                sortOrder = 'DESC';
                sort = sort.slice(1);
            } else {
                sortOrder = DEFAULT_PAGIN.sortOrder;
            }
        }
        var sortBy = (sort || DEFAULT_PAGIN.sortBy);
        var count = (request.getParameter("count") || DEFAULT_PAGIN.count);
        var start = (request.getParameter("start") || DEFAULT_PAGIN.start);
        var paginationLimit = (request.getParameter("paginationLimit") || DEFAULT_PAGIN.paginationLimit);
        var paging = {
                'start': start,
                'count': count,
                'sortOrder': sortOrder,
                'sortBy': sortBy,
                'paginationLimit': paginationLimit
            };
        var q = (request.getParameter("q") || '');
        try {
            if (q) {
                var qString = '{' + q + '}';
                var query = parse(qString);
                
                var assets = assetManager.search(query, paging); //doesnt work properly
            } else {
                
                var assets = assetManager.list(paging);
            }
            var expansionFields = (request.getParameter('fields') || '');
            if (expansionFields) {
                options.fields = expansionFields.split(',');
                options.assets = assets;
                result =fieldExpansion(options, req, res, session);
                //return;                    
            } else {                
                result = assets;
                
            }            
            //res = responseProcessor.buildSuccessResponse(res,200,result);
        } catch (e) {
            //res = responseProcessor.buildErrorResponse(400, "Your request is malformed");
            //print();
            result = null;
            log.error(e);
        }
        return result;
    };
    api.get = function(options, req, res, session) {
        var asset = require('rxt').asset;
        var assetManager = asset.createUserAssetManager(session, options.type);
        try {
            var retrievedAsset = assetManager.get(options.id);
            if (!retrievedAsset) {
               // print(responseProcessor.buildSuccessResponse(200, 'No matching asset found by' + options.id, []));
                return null;
            } else {
                var expansionFields = (request.getParameter('fields') || '');
                if (expansionFields) {
                    options.fields = expansionFields.split(',');
                    var assets = [];
                    assets.push(retrievedAsset);
                    options.assets = assets;
                    result = fieldExpansion(options, req, res, session);
                } else {
                    result = retrievedAsset;
                }
               // print(responseProcessor.buildSuccessResponse(200, 'Request Served Sucessfully', result));
            }
        } catch (e) {
        //res.sendError(400, "No matching asset found");
         //   print(responseProcessor.buildErrorResponse(400, "No matching asset found"));
            log.error(e);
            result = null;       
        }
        return result;
    };
    api.remove = function(options, req, res, session) {
        var asset = require('rxt').asset;
        var am = asset.createUserAssetManager(session, options.type);
        try {
            am.remove(options.id);
            result=true;
        } catch (e) {
            log.error('Asset with id: ' + asset.id + ' was not deleted due to ' + e);
            result = false;
        }
        return result;
    };

    /**
     * @param req       The global request object
     * @param constants The constants rxt object where all the constants are defined
     * @return returns the String for sortby parameter
     */
    var getSort = function (req, constants) {
        return req.getParameter(constants.Q_SORT) || constants.ASSET_DEFAULT_SORT;
    };

    /**
     * @param req       The global request object
     * @param constants The constants rxt object where all the constants are defined
     * @return returns the String for sortOrder parameter
     */
    var getSortOrder = function (req, constants) {
        return req.getParameter(constants.Q_SORT_ORDER) || constants.ASSET_DEFAULT_SORT_ORDER;
    };

    /**
     * @param req       The global request object
     * @param constants The constants rxt object where all the constants are defined
     * @return returns the String for tag
     */
    var getTag = function (req, constants) {
        return req.getParameter(constants.Q_TAG) || null;
    };

    /**
     * This function is to create paging object for store list page
     * @param assets list of assets
     * @param type Asset type
     * @param req Global request object
     * @return Returns a json object of paging information
     */
    api.assetsPaging = function (assets, type, req) {
        var paging = {};
        var constants = require('rxt').constants;
        var sort = getSort(req, constants);
        var sortOrder = getSortOrder(req, constants);
        var url = constants.ASSET_BASE_URL + type + '/list?sortBy=' + sort + "&sort=" + sortOrder;
        paging.sort = sort;
        paging.size = assets.length;
        paging.tag = getTag(req, constants);
        paging.url = url;
        return paging;
    };
}(api))