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
 Description:The apis-asset-manager is used to retriew assets for api calls
 Filename: asset_api.js
 Created Date: 7/24/2014
 */
var api = {};
var result;

(function (api) {
    var utils = require('utils');
    var rxtModule = require('rxt');
    var log = new Log('asset_api');
    var exceptionModule = utils.exception;
    var constants = rxtModule.constants;
    /**
     *
     * @param  fieldParam   The raw string comes as field parameter of the request
     * @return Array of fields which are required to be filtered out from assets
     */
    var getExpansionFileds = function (fieldParam) {
        var rawFields = fieldParam.split(',');//set fields
        for (var fieldIndex = 0; fieldIndex < rawFields.length; fieldIndex++) {
            rawFields[fieldIndex] = rawFields[fieldIndex].trim();
        }
        return rawFields;
    };

    /**
     * The function filter the requested fields from assets objects and build new asset object with requested fields
     * @param  options   The object contains array of required fields and array of assets for filtering fields
     */

    var fieldExpansion = function (options) {
        var fields = options.fields;
        var artifacts = options.assets;
        var artifact;
        var asset;
        var field;
        var modifiedAssets = [];
        for (var artifactIndex = 0; artifactIndex < artifacts.length; artifactIndex++) {
            artifact = artifacts[artifactIndex];
            asset = {};
            for (var feildIndex = 0; feildIndex < fields.length; feildIndex++) {
                field = fields[feildIndex];
                //First check if the field is top level in the artifact
                if (artifact.hasOwnProperty(field)) {
                    asset[field] = artifact[field];

                } else {
                    //Check if the appears in the attributes property
                    if (artifact.attributes && artifact.attributes.hasOwnProperty(field)) {
                        //if asset.attribute key is available copy it to attributes variable, if not initialize asset.attributes
                        var attributes = asset.attributes ? asset.attributes : (asset.attributes = {});
                        attributes[field] = artifact.attributes[field];
                    }
                }

            }
            modifiedAssets.push(asset);// add asset to the list
        }
        return modifiedAssets;// return the asset list
    };

    /**
     *
     * @param exception The exception body
     * @param type      The type of exception that how it should be handled
     * @param code      Exception status code
     */
    var handleError = function (exception, type, code) {
        var e;
        if (type == constants.THROW_EXCEPTION_TO_CLIENT) {
            log.debug(exception);
            e = exceptionModule.buildExceptionObject(exception, code);
            throw e;
        } else if (type == constants.LOG_AND_THROW_EXCEPTION) {
            log.error(exception);
            throw exception;
        } else if (type == constants.LOG_EXCEPTION_AND_TERMINATE) {
            log.error(exception);
            var msg = 'An error occurred while serving the request!';
            e = exceptionModule.buildExceptionObject(msg, constants.STATUS_CODES.INTERNAL_SERVER_ERROR);
            throw e;
        } else if (type == constants.LOG_EXCEPTION_AND_CONTINUE) {
            log.debug(exception);
        } else {
            log.error(exception);
            throw exception;
        }
    };

    /**
     * This function put asset to the storage
     * @param am    The asset manager instance
     * @param asset The asset to be saved
     */

    var putInStorage = function (asset, am) {
        var resourceFields = am.getAssetResources();
        var ref = utils.file;
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
     * The function get current asset in the storage
     * @param original  The current asset resources available in the store
     * @param asset     The new asset resources to continue with updating
     */
    var putInOldResources = function (original, asset, am) {
        var resourceFields = am.getAssetResources();
        var resourceField;
        for (var index in resourceFields) {
            resourceField = resourceFields[index];
            //If the asset attribute value is null then use the old resource
//            if ((!asset.attributes[resourceField]) || (asset.attributes[resourceField] == '')) {
            if (!asset.attributes[resourceField]) {
                log.debug('Copying old resource attribute value for ' + resourceField);
                asset.attributes[resourceField] = original.attributes[resourceField];
            }
        }
    };

    /**
     *Check whether key:value available in data
     */
    var isPresent = function (key, data) {
        return (data[key]) || (data[key] == '');
    };

    /**
     * keep unchanged values as they are
     * @param  original old asset
     * @param  asset    asset
     * @param  sentData to change
     * @return The updated-asset
     */

    var putInUnchangedValues = function (original, asset, sentData) {
        for (var key in original.attributes) {
            //We need to add the original values if the attribute was not present in the data object sent from the client
            //and it was not deleted by the user (the sent data has an empty value)
            if (original.attributes.hasOwnProperty(key)) {
                if (((!asset.attributes[key]) || (asset.attributes[key].length == 0)) && (!isPresent(key, sentData))) {
                    log.debug('Copying old attribute value for ' + key);
                    asset.attributes[key] = original.attributes[key];
                }
            }
        }
    };

    /**
     * api to create a new asset
     * @param  options incoming values
     * @param  req     jaggery request
     * @param  res     jaggery response
     * @param  session  sessionId
     * @return The created asset or null if failed to create the asset
     */
    api.create = function (options, req, res, session) {

        var asset = require('rxt').asset;
        var am = asset.createUserAssetManager(session, options.type);
        var assetReq = req.getAllParameters('UTF-8');

        var assetModule = rxtModule.asset;
        var am = assetModule.createUserAssetManager(session, options.type);
        var assetReq = req.getAllParameters('UTF-8');//get asset attributes from the request

        var asset = null;
        if (request.getParameter("asset")) {
            asset = parse(request.getParameter("asset"));
        } else {
            asset = am.importAssetFromHttpRequest(assetReq);
        }//generate asset object

        try {
            putInStorage(asset, am);//save to the storage
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
     * The function to update an existing asset via api
     * @param  options  incoming
     * @param  req      jaggery-request
     * @param  res      jaggery-response
     * @param  session  sessionID
     * @return updated-asset
     */
    api.update = function (options, req, res, session) {

        var assetModule = rxtModule.asset;
        var am = assetModule.createUserAssetManager(session, options.type);

        var assetReq = req.getAllParameters('UTF-8');
        var asset = null;
        if (request.getParameter("asset")) {
            asset = parse(request.getParameter("asset"));
        } else {
            asset = am.importAssetFromHttpRequest(assetReq);
        }
        var original = null;
        asset.id = options.id;
        try {
            original = am.get(options.id);
        } catch (e) {
            log.error(e);
            asset = null;
            var msg = 'Unable to locate the asset with id: ' + options.id;
            handleError(msg, constants.THROW_EXCEPTION_TO_CLIENT, constants.STATUS_CODES.NOT_FOUND);
        }
        if (original) {
            putInStorage(asset, am);
            putInOldResources(original, asset, am);//load current asset values
            putInUnchangedValues(original, asset, assetReq);
            //If the user has not uploaded any new resources then use the old resources
            if (!asset.name) {
                asset.name = am.getName(asset);
            }
            try {
                am.update(asset);
            } catch (e) {
                asset = null;
                var errMassage = 'Failed to update the asset of id:' + options.id;
                log.error(e);
                if (log.isDebugEnabled()) {
                    log.debug('Failed to update the asset ' + stringify(asset));
                }
                handleError(errMassage, constants.LOG_EXCEPTION_AND_TERMINATE, constants.STATUS_CODES.INTERNAL_SERVER_ERROR);

            }
        }
        return asset;
    };

    /**
     *
     * @param sortParam The sort query parameter comes with request
     * @param paging    Paging object populated with default paging values
     */
    var populateSortingValues = function (sortParam, paging) {
        var constants = rxtModule.constants;
        var sortBy;
        if (sortParam) {
            var order = sortParam.charAt(0);
            if (order == '+' || order == ' ') {// ascending
                paging.sortOrder = constants.Q_SORT_ORDER_ASCENDING;//TODO get as constants
                sortBy = sortParam.slice(1);
            } else if (order == '-') {//descending
                paging.sortOrder = constants.Q_SORT_ORDER_DESCENDING;
                sortBy = sortParam.slice(1);
            }
            paging.sortBy = (sortBy || paging.sortBy);
        }
    };

    /**
     * This function id to validate and build the query object from the string
     * @param query This is the query string to be parsed
     * @return Returns the parsed Json object containing query
     */
    function validateQuery(query) {
        var q = {};
        try {
            q = parse(query);

        } catch (e) {
            log.error("Invalid Query \'" + query + "\'");
            if (log.isDebugEnabled()) {
                log.debug(e);
            }
        }
        return q;
    }

    /**
     * The function search for assets
     * @param req      The request
     * @param res      The response
     * @param options  Object containing parameters
     * @param session sessionID
     */
    api.search = function (options, req, res, session) {
        var asset = rxtModule.asset;
        var assetManager = asset.createUserAssetManager(session, options.type);
        var sort = (request.getParameter("sort") || '');
        var paging = rxtModule.constants.DEFAULT_ASSET_PAGIN;
        populateSortingValues(sort, paging);// populate sortOrder and sortBy
        paging.count = (request.getParameter("count") || paging.count);
        paging.start = (request.getParameter("start") || paging.start);
        paging.paginationLimit = (request.getParameter("paginationLimit") || paging.paginationLimit);

        var q = (request.getParameter("q") || '');
        try {
            var assets;
            if (q) {//if search-query parameters are provided
                var qString = '{' + q + '}';
                var query = validateQuery(qString);
                assets = assetManager.search(query, paging);// asset manager back-end call with search-query
            } else {
                assets = assetManager.list(paging);// asset manager back-end call for asset listing
            }
            var expansionFieldsParam = (request.getParameter('fields') || '');
            if (expansionFieldsParam) {//if field expansion is requested
                options.fields = getExpansionFileds(expansionFieldsParam);//set fields
                options.assets = assets;//set assets
                result = fieldExpansion(options);//call field expansion methods to filter fields
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
     * @param options  Object containing parameters id, type
     * @param req      Jaggery request
     * @param res      Jaggery response
     * @param session  A string containing sessionID
     * @return The retrieved asset or null if an asset not found
     */
    api.get = function (options, req, res, session) {
        var asset = rxtModule.asset;
        var assetManager = asset.createUserAssetManager(session, options.type);
        try {
            var retrievedAsset = assetManager.get(options.id); //backend call to get asset by id
            if (!retrievedAsset) {
                return null;
            } else {

                var expansionFieldsParam = (request.getParameter('fields') || '');
                if (expansionFieldsParam) {//if field expansion requested
                    options.fields = getExpansionFileds(expansionFieldsParam);//set fields
                    var assets = [];
                    assets.push(retrievedAsset);
                    options.assets = assets;
                    result = fieldExpansion(options)[0];//call field-expansion to filter-out fields
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
     * @param options  Object containing parameters id, type
     * @param req      Jaggery request
     * @param res      Jaggery response
     * @param session  A string containing sessionID
     * @return Boolean value whether deleted or not
     */
    api.remove = function (options, req, res, session) {
        var asset = rxtModule.asset;
        var am = asset.createUserAssetManager(session, options.type);
        var retrievedAsset = api.get(options, req, res, session);
        if (!retrievedAsset) {
            log.error('Id not valid');
            return false;
        }
        try {
            am.remove(options.id); //call asset manager to remove asset
            return true;
        } catch (e) {
            log.error('Asset with id: ' + asset.id + ' was not deleted due to ' + e);
            success = false;
        }
    };

}(api));
