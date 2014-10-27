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

var api = {};
var error = '';
(function (api) {
    var log = new Log('lifecycle_api');
    var rxtModule = require('rxt');
    var storeModule = require('store');
    var lifecycleModule = require('lifecycle');
    var utils = require('utils');
    var exceptionModule = utils.exception;
    var constants = rxtModule.constants;
    /**
     * Function to get asset by id
     * @param options  Object contains asset-id
     * @param am       The asset-manager instance
     */
    var getAsset = function (options, am) {
        try {
            asset = am.get(options.id);
        } catch (e) {
            var asset;
            var msg = 'Unable to locate the asset with id: ' + options.id;
            handleError(msg, constants.THROW_EXCEPTION_TO_CLIENT, constants.STATUS_CODES.NOT_FOUND);
        }
        return asset;
    };

    /**
     *
     * @param exception The exception body
     * @param type      The type of exception that how it should be handled
     * @param code      Exception status code
     */
    var handleError = function (exception, type, code) {
        if (type == constants.THROW_EXCEPTION_TO_CLIENT) {
            log.debug(exception);
            var e = exceptionModule.buildExceptionObject(exception, code);
            throw e;
        } else if (type == constants.LOG_AND_THROW_EXCEPTION) {
            log.error(exception);
            throw exception;
        } else if (type == constants.LOG_EXCEPTION_AND_TERMINATE) {
            log.error(exception);
            var msg = 'An error occurred while serving the request!';
            var e = exceptionModule.buildExceptionObject(msg, constants.STATUS_CODES.INTERNAL_SERVER_ERROR);
            throw e;
        } else if (type == constants.LOG_EXCEPTION_AND_CONTINUE) {
            log.debug(exception);
        }
        else {
            log.error(exception);
            throw e;
        }
    };


    /**
     * Validate asset type
     * @param  options Object contains asset type
     */
    var validateOptions = function (options) {
        if (!options.type) {
            var error = 'Unable to obtain state information without knowing the type of asset of id: ' + options.id;
            handleError(error, constants.THROW_EXCEPTION_TO_CLIENT, constants.STATUS_CODES.BAD_REQUEST);
        }
    };

    /**
     * Validate asset by id, check whether a asset is available by id
     * @param  asset   The asset object
     * @param  options Object contains asset-id
     */
    var validateAsset = function (asset, options) {
        var error = null;
        if (!asset) {
            error = 'Unable to locate asset information of ' + options.id;
        }
        else if (!asset.lifecycle) {
            error = 'The asset ' + options.id + ' does not have an associated lifecycle';
        }
        else if (!asset.lifecycleState) {
            error = 'The asset ' + options.id + ' does not have a lifecycle state.';
        }
        if(error){
            handleError(error, constants.THROW_EXCEPTION_TO_CLIENT, constants.STATUS_CODES.NOT_FOUND);
        }

    };
    /**
     * change the state of an asset
     * @param  options  Object contains asset-id
     * @param  req      jaggery request
     * @param  res      jaggery response
     * @param  session  SessionId
     * @return Boolean whether state changed/not
     */
    api.changeState = function (options, req, res, session) {
        var success = false;
        validateOptions(options);
        if (!options.nextState) {
            error = 'A next state has not been provided';
            handleError(error, constants.THROW_EXCEPTION_TO_CLIENT, constants.STATUS_CODES.BAD_REQUEST)
        }
        //Obtain the tenantId
        var server = storeModule.server;
        var assetM = rxtModule.asset;
        var user = server.current(session);//get current user
        var tenantId = user.tenantId;//get tenant
        var am = assetM.createUserAssetManager(session, options.type);//get asset manager
        var asset = getAsset(options, am);//get asset
        validateAsset(asset, options);//validate asset
        //Obtain the lifecycle
        var lcApi = lifecycleModule.api;
        var lifecycle = lcApi.getLifecycle(asset.lifecycle, tenantId);//get lifecycle bind with asset
        var action = lifecycle.transitionAction(asset.lifecycleState, options.nextState);
        //check whether a transition action available from asset.lifecycleState to options.nextState
        if (!action) {
            error = 'It is not possible to reach ' + options.nextState + ' from ' + asset.lifecycleState;
            handleError(error, constants.THROW_EXCEPTION_TO_CLIENT, constants.STATUS_CODES.BAD_REQUEST)
        }
        try {
            success = am.invokeLcAction(asset, action);
        } catch (e) {
            error = 'Error while changing state to ' + options.nextState + ' from ' + asset.lifecycleState;
            handleError(error, constants.THROW_EXCEPTION_TO_CLIENT, constants.STATUS_CODES.INTERNAL_SERVER_ERROR)
        }
        return success;
    };

    /**
     * The method to invoke state transition and check-items state changes
     * @param   options Object contains asset-id
     * @param   req      jaggery request
     * @param   res      jaggery response
     * @param   session  SessionId
     * @return {string}
     */
    api.invokeStateTransition = function (options, req, res, session) {
        var stateReq = req.getAllParameters('UTF-8');
        var successChecking = false;
        var successStateChange = false;
        var msg = '';

        if (!stateReq.checkItems && !stateReq.nextState) {
            var error = 'Checklist items or next state is not provided!';
            handleError(error, constants.THROW_EXCEPTION_TO_CLIENT, constants.STATUS_CODES.BAD_REQUEST);//TODO use utility method
        }
        if (stateReq.checkItems) {
            options.checkItems = JSON.parse(stateReq.checkItems);
            successChecking = this.checkItems(options, req, res, session);
            if (successChecking) {
                msg = 'Checklist items checked successfully! ';
            } else {
                msg = 'Checklist items checking failed! ';
            }
        }
        if (stateReq.nextState) {
            var isCommentsEnabled = true;//TODO load from asset.js
            if (isCommentsEnabled) {
                if (stateReq.comment) {
                    options.comment = stateReq.comment;
                } else {
                    msg = msg + ' Please provide a comment for this state transition!';
                    handleError(msg, constants.THROW_EXCEPTION_TO_CLIENT, constants.STATUS_CODES.BAD_REQUEST);
                }
            }
            options.nextState = stateReq.nextState;
            successStateChange = this.changeState(options, req, res, session);
            if (successStateChange) {
                msg = msg + ' State changed successfully to ' + options.nextState + '!';
            } else {
                msg = msg + ' An error occurred while changing state to ' + options.nextState + '!';
                handleError(error, constants.THROW_EXCEPTION_TO_CLIENT, constants.STATUS_CODES.INTERNAL_SERVER_ERROR);
                //msg = '';
            }

        }
        return msg;
    };

    var isDeletable = function (assetState, deletableStates) {
        var astState = assetState ? assetState.toLowerCase() : assetState;
        for (var index in deletableStates) {
            if (deletableStates[index].toLowerCase() == astState) {
                return true;
            }
        }
        return false;
    };

    /**
     * to set the checkItems of the currents asset's state
     * @param checkItems
     * @param asset current asset
     * @param am asset-manager
     * @return A list of check-items and checked:true/false
     */
    var setCheckItemState = function (checkItems, asset, am) {
        //Obtain the check item states for the asset
        try {
            var assetCheckItems = am.getLifecycleCheckItems(asset);
            var item;
            for (var index in assetCheckItems) {
                item = assetCheckItems[index];
                if (checkItems[index]) {
                    checkItems[index].checked = item.checked;
                }
            }
        } catch (e) {
            var msg = 'No check items are available for this asset state';
            handleError(msg, constants.LOG_EXCEPTION_AND_CONTINUE, null);
        }

        return checkItems;
    };
    /**
     * The function changes the state of a single check item
     * @param  checkItemIndex      The index of the check item to be changed
     * @param  checkItemState The new state of the check item
     * @param  asset               The asset on which the operation needs to be performed
     * @param  state               The state information of the current asset
     * @param  am                  The asset Manager instance
     */
    var updateCheckItemState = function (checkItemIndex, checkItemState, asset, state, am) {
        //Check if the index provided is valid
        var msg;
        if ((checkItemIndex < 0) || (checkItemIndex > state.checkItems.length - 1)) {
            msg = 'Unable to change the state of the check item as the index does not point to a valid check item.The check item index must be between 0 and ' + state.checkItems.length + '.';
            handleError(msg, constants.THROW_EXCEPTION_TO_CLIENT, constants.STATUS_CODES.BAD_REQUEST);
        }
        //Check if the check item state is the same as the next state
        if (state.checkItems[checkItemIndex].checked == checkItemState) {
            msg = 'The state of the check item at index ' + checkItemIndex + ' was not changed as it is already ' + checkItemState;
            handleError(msg, constants.LOG_EXCEPTION_AND_CONTINUE, null)
            //throw msg;
        }
        //Invoke the state change
        try {
            am.invokeLifecycleCheckItem(asset, checkItemIndex, checkItemState);
        } catch (e) {
            msg = 'Unable to change the state of check item ' + checkItemIndex + ' to ' + checkItemState;
            handleError(msg, constants.THROW_EXCEPTION_TO_CLIENT, constants.STATUS_CODES.INTERNAL_SERVER_ERROR);
        }
    };

    /**
     * The function updates the check items for a given asset
     * @param  options Incoming details
     * @param  asset   current asset
     * @param  am      asset-manager
     * @param  state   the current state of the asset
     * @return Boolean       Whether the request is successful or not
     */
    var updateCheckItemStates = function (options, asset, am, state) {
        var success = false;
        var msg = '';
        //Check if the current state has any check items
        if ((state.checkItems) && (state.checkItems.length < 1)) {
            msg = 'Unable to change the state of the check item as the current state(' + state.id + ') does not have any check items';
            handleError(msg, constants.THROW_EXCEPTION_TO_CLIENT, constants.STATUS_CODES.BAD_REQUEST);

        }

        //Assume checking items will succeed
        success = true;
        var checkItemsList = options.checkItems;
        var checkItemIndex;
        var checkItemState;
        var checkItem;

        //Go through each check item in the check items
        for (var index in checkItemsList) {
            checkItem = checkItemsList[index];
            checkItemIndex = checkItem.index;
            checkItemState = checkItem.checked;
            if ((checkItemIndex != null) && (checkItemState == true || checkItemState == false)) {
                updateCheckItemState(checkItemIndex, checkItemState, asset, state, am);
            }
        }
        return success;
    };

    /**
     * The function will obtain the state detail of a asset
     * @param  options options.id=<asset-id>
     * @param  req     jaggery request
     * @param  res     jaggery response
     * @param  session   sessionID
     * @return A JSON object defining the structure of the lifecycle
     */
    api.getState = function (options, req, res, session) {
        var state;
        validateOptions(options);
        var assetApi = rxtModule.asset;
        var coreApi = rxtModule.core;
        var am = assetApi.createUserAssetManager(session, options.type);//get asset manager
        var server = storeModule.server;//get current server instance
        var user = server.current(session);//get current user
        var tenantId = user.tenantId;//get tenantID
        var asset = getAsset(options, am);//get asset
        validateAsset(asset, options);//validate asset
        var lcApi = lifecycleModule.api;//load lifecycle module
        var lifecycle = lcApi.getLifecycle(asset.lifecycle, tenantId);
        var rxtManager = coreApi.rxtManager(tenantId);
        //Obtain the state data
        state = lifecycle.state(asset.lifecycleState);
        if (!state) {
            var msg = 'Unable to locate state information for ' + asset.lifecycleState;
            handleError(msg, constants.THROW_EXCEPTION_TO_CLIENT, constants.STATUS_CODES.NOT_FOUND);
        }
        //Obtain the deletable states
        state.deletableStates = rxtManager.getDeletableStates(options.type);
        //Determine if the current state is a deletable state
        state.isDeletable = isDeletable(asset.lifecycleState, state.deletableStates);
        //Update the state of the check items
        state.checkItems = setCheckItemState(state.checkItems, asset, am);
        return state;
    };

    /**
     * The function will obtain the definition of the requested lifecycle
     * @param  options options.id=<asset-id>
     * @param  req     jaggery request
     * @param  res     jaggery response
     * @param  session   sessionID
     * @return A JSON object defining the structure of the
     */
    api.getLifecycle = function (options, req, res, session) {
        var lcApi = lifecycleModule.api;//load lifecycle module
        var server = storeModule.server;//load server instance
        var lifecycle = null;
        var user = server.current(session);
        if (!options.name) {
            log.warn('Unable to locate lifecycle definition as a name has not been provided.Please invoke the API with a lifecycle name');
            return lifecycle;
        }
        lifecycle = lcApi.getLifecycle(options.name, user.tenantId);
        if (!lifecycle) {
            log.warn('A lifecycle was not located for the lifecycle name: ' + options.name);
        }
        return lifecycle;
    };
    /**
     * The function will return a list of all available lifecycles for the currently logged in user's tenant.
     * @param  options options.id=<asset-id>
     * @param  req     jaggery request
     * @param  res     jaggery response
     * @param  session sessionID
     * @return An array of strings with the names of the lifecycles
     */
    api.getLifecycles = function (options, req, res, session) {
        var lcApi = lifecycleModule.api;
        var server = storeModule.server;
        //var lifecycle = null;
        var user = server.current(session);
        return lcApi.getLifecycleList(user.tenantId);
    };

    /**
     * The function changes the state of a set of check items sent as an array with each element been  { index: number , checked:true}
     * @param  options options.id=<asset-id>
     * @param  req     jaggery request
     * @param  res     jaggery response
     * @param  session   sessionID
     * @return A boolean value indicating the success of the operation
     */
    api.checkItems = function (options, req, res, session) {
        var isSuccess = false;
        validateOptions(options);
        var assetApi = rxtModule.asset;
        var am = assetApi.createUserAssetManager(session, options.type);
        var asset = getAsset(options, am);
        validateAsset(asset, options);
        var state = this.getState(options, req, res, session);
        isSuccess = updateCheckItemStates(options, asset, am, state);
        return isSuccess;
    };
}(api));