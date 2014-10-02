/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
var api = {};
var responseProcessor = require('utils').response;
(function(api) {

/**
 * The function filter the requested fields from assets objects and build new asset object with requested fields
 * @param {[json]} options         :{"fields":<requested-fields>, "artifacts":<list-of-assets>}      
 * @param {[obj]} req             :Request object
 * @param {[obj]} res             :Response object
 * @param {[string]} [session]       :[sessioinid]
 */
    var fieldExpansion = function(options, req, res, session) {
        var fields = options.fields;
        var artifacts = options.assets;
        var extendedAssetTemplate = {};
        for (var val in fields) {
            var key = fields[val];
            var value = '';
            extendedAssetTemplate[key] = value;
        }//build template asset object
        var newArtifactTemplateString = stringify(extendedAssetTemplate);
        var modifiedAssets = [];
        for (var j in artifacts) {
            var artifactObject = parse(newArtifactTemplateString);// new asset object with the template key:value
            for (var i in extendedAssetTemplate) {
                if (artifacts[j][i]) {
                     artifactObject[i] = artifacts[j][i];
                } else {
                    artifactObject[i] = artifacts[j].attributes[i];
                }
            }//populate asset artifacts, attributes
            modifiedAssets.push(artifactObject);// add asset to the list
        }
        return modifiedAssets;// return the list
    };

 /**
 * The function put asset to the storage
 * @param {[json]} [options] []
 * @param {[json]} [asset] [asset obj to be saved] 
 */   
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

/**
 * The function update asset in the storage
 * @param {[json]} [original] [old asset]
 * @param {[json]} [asset] [new asset] 
 */
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
/**
 * 
 */
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

/**
 * The function create new asset
 */
    api.create = function(options, req, res, session) {
        var asset = require('rxt').asset;
        var am = asset.createUserAssetManager(session, options.type);
        var assetReq = req.getAllParameters('UTF-8');//get asset attributes from the request
        var asset = am.importAssetFromHttpRequest(assetReq);//generate asset object
        putInStorage(options, asset, am, req, session);//save to the storage
        try {
            am.create(asset);
        } catch (e) {
            log.error('Asset of type: ' + options.type + ' was not created due to ' + e);
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

/**
 * The function update asset
 */
    api.update = function(options, req, res, session) {
        var asset = require('rxt').asset;
        var am = asset.createUserAssetManager(session, options.type);
        var assetReq = req.getAllParameters('UTF-8');
        
        
        var asset = null;
        if (request.getParameter("asset") != null){
            asset = parse(request.getParameter("asset"));
        }else{
            asset = am.importAssetFromHttpRequest(assetReq);
        }

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

/**
 * The function search for assets
 * @param {[options]} [varname] [fields,assets]
 * @param {[obj]}  [req]      [Request object]
 * @param {[obj]}  [res]           [Request object]
 */
    api.search = function(options, req, res, session) {
        var asset = require('rxt').asset;
        var assetManager = asset.createUserAssetManager(session, options.type);
        var sort = (request.getParameter("sort") || '');
        var sortOrder = DEFAULT_PAGIN.sortOrder;

        if (sort) {// if sort parameter is available in request (sort=-/+<attribute>)
            var order = sort.charAt(0);
            if (order == '+' || order == ' ') {// ascending
                sortOrder = 'ASC';
                sort = sort.slice(1);
            } else if (order == '-') {//descending
                sortOrder = 'DESC';
                sort = sort.slice(1);
            } else {//not mentioned
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
            };//build paging object
        var q = (request.getParameter("q") || '');
        try {
            if (q) {//if search-query parameters are provided
                var qString = '{' + q + '}';
                var query = parse(qString);
                
                var assets = assetManager.search(query, paging);// asset manager back-end call with search-query
            } else {
                
                var assets = assetManager.list(paging);// asset manager back-end call for asset listing
            }
            var expansionFields = (request.getParameter('fields') || '');
            if (expansionFields) {//if field expansion is requested
                options.fields = expansionFields.split(',');//set fields
                options.assets = assets;//set assets
                result =fieldExpansion(options, req, res, session);//call field expansion methos to filter fields
            } else {                
                result = assets;                
            }            
        } catch (e) {
            result = null;
            log.error(e);
        }
        return result;
    };

/**
 * The function get an asset by id
 * @param {json} [options] [options.id:<asset-id>]
 * @param {[obj]}  [req]      [Request object]
 * @param {[obj]}  [res]      [Request object]
 */
    api.get = function(options, req, res, session) {
        var asset = require('rxt').asset;
        var assetManager = asset.createUserAssetManager(session, options.type);
        try {
            var retrievedAsset = assetManager.get(options.id);//backend call to get asset by id
            if (!retrievedAsset) {
                return null;
            } else {
                var expansionFields = (request.getParameter('fields') || '');
                if (expansionFields) {//if field expansion requested
                    options.fields = expansionFields.split(',');
                    var assets = [];
                    assets.push(retrievedAsset);
                    options.assets = assets;
                    result = fieldExpansion(options, req, res, session);//call fieldexpansion to filterout fields
                } else {
                    result = retrievedAsset;
                }
            }
        } catch (e) {
            log.error(e);
            result = null;       
        }
        return result;
    };

/**
 * The function deletes an asset by id
 * @param {json} [options] [options.id:<asset-id>, options.type=<asset-type>]
 * @param {[obj]}  [req]      [Request object]
 * @param {[obj]}  [res]      [Request object]
 */
    api.remove = function(options, req, res, session) {
        var asset = require('rxt').asset;
        var am = asset.createUserAssetManager(session, options.type);
        try {
            am.remove(options.id);//call asset manager to remove asset
            result=true;
        } catch (e) {
            log.error('Asset with id: ' + asset.id + ' was not deleted due to ' + e);
            result = false;
        }
        return result;
    };
}(api))