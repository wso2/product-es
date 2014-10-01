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
(function(api) {
    var log = new Log('lifecycle_api');
    /**
     * get asset by id
     * @param  {json} options {options.id:<asset-id>}
     * @param  {[type]} am      [description]
     */
    var getAsset = function(options, am) {
        var asset;
        try {
            asset = am.get(options.id);
        } catch (e) {
            log.warn('Unable to locate the asset with id: ' + options.id);
        }
        return asset;
    };

    /**
     * Validate asset type  
     * @param  {JSON} options [options.id:<asset-id>,options.type:<asset-type>]
     * @param  {[type]} req     [description]
     * @param  {[type]} res     [description]
     * @param  {[type]} session [description]
     */
    var validateOptions = function(options, req, res, session) {
        if (!options.type) {
            log.error('Unable to obtain state information without knowing the type of asset ' + options.id);
            throw 'Unable to obtain state information without knowing the type of asset ' + options.id;
        }
    };
    /**
     * Validate asset by id, check whether a asset is available by id
     * @param  {[type]} asset   [description]
     * @param  {[type]} options [description]
     * @return {[type]}         [description]
     */
    var validateAsset = function(asset, options) {
        if (!asset) {
            log.error('Unable to locate asset information of ' + options.id);
            throw 'Unable to locate asset information of ' + options.id;
        }
        if (!asset.lifecycle) {
            log.error('The asset ' + options.id + ' does not have an associated lifecycle');
            throw 'The asset ' + options.id + ' does not have an associated lifecycle';
        }
        if (!asset.lifecycleState) {
            log.error('The asset ' + options.id + ' does not have a lifecycle state.');
            throw 'The asset ' + options.id + ' does not have a lifecycle state.';
        }
    };
    /**
     * change the state of an asset
     * @param  {Json} options [description]
     * @param  {[type]} req     [description]
     * @param  {[type]} res     [description]
     * @param  {[type]} session [description]
     */
    api.changeState = function(options, req, res, session) {
        var success = false;
        validateOptions(options, req, res, session);
        if (!options.nextState) {
            log.warn('A next state has not been provided');
            throw 'A next state has not been provided';            
            return success;
        }
        //Obtain the tenantId
        var server = require('store').server;
        var asset = require('rxt').asset;
        var user = server.current(session);//get current user
        var tenantId = user.tenantId;//get tenant 
        var am = asset.createUserAssetManager(session, options.type);//get asset manager
        var asset = getAsset(options, am);//get asset
        validateAsset(asset, options)//validate asset
        //Obtain the lifecycle
        var lcApi = require('lifecycle').api;
        var lifecycle = lcApi.getLifecycle(asset.lifecycle, tenantId);//get lifecycle binded with asset
        var action = lifecycle.transitionAction(asset.lifecycleState, options.nextState);//
        if (!action) {
            log.warn('It is not possible to reach ' + options.nextState + ' from ' + asset.lifecycleState);
            throw 'It is not possible to reach ' + options.nextState + ' from ' + asset.lifecycleState;
            return success;
        }
        success = am.invokeLcAction(asset, action);
        return success;
    };

    api.invokeStateTransition = function(options, req, res, session) {
        var stateReq = req.getAllParameters('UTF-8');  
        log.info(stateReq);
        var successChecking = false;
        var successStateChange = false;
        var msg = '';
        
        
        if(stateReq.checkItems){            
            try{
                options.checkItems = JSON.parse(stateReq.checkItems);
                successChecking = checkItems(options, req, res, session);
                if(successChecking){
                    msg = msg+'Checklist items checked successfully! ';
                }else{
                    msg = msg+'Checklist items checking failed! ';
                }
                
            }catch(e){
                 msg =msg +' '+e;
                 throw msg;
            }
        }            
        if(stateReq.nextState){
            var isCommentsEnabled = true;
            if(isCommentsEnabled){
                if(stateReq.comment){
                    options.comment = stateReq.comment;
                }else{
                    msg = msg+' Please provide a comment for this state transition!'; 
                    throw msg;
                }          
            }
            options.nextState = stateReq.nextState;
            try{
                successStateChange = this.changeState(options, req, res, session);
                if(successStateChange){                    
                    msg = msg+' State changed successfully to '+options.nextState+'!';
                }else{                    
                    msg = msg+' An error occured while changing state to '+options.nextState+'!';                   
                }
            }catch(e){
                msg =msg+ ' '+e;                
                throw msg;
            }            
        }
        if(!stateReq.checkItems && !stateReq.nextState){
             msg = 'Checklist items or next state is not provided!';             
             throw msg;
        }
        
        return msg;
    };

    var isDeletable = function(assetState, deletableStates) {
        var assetState = assetState ? assetState.toLowerCase() : assetState;
        for (var index in deletableStates) {
            if (deletableStates[index].toLowerCase() == assetState) {
                return true;
            }
        }
        return false;
    };
    var setCheckItemState = function(checkItems, asset, am) {
        //Obtain the check item states for the asset
        //TODO; This method throws an exception if check list items are not present
        var assetCheckItems = am.getLifecycleCheckItems(asset);
        var item;
        for (var index in assetCheckItems) {
            item = assetCheckItems[index];
            if (checkItems[index]) {
                checkItems[index].checked = item.checked;
            }
        }
        return checkItems;
    };
    /**
     * The function changes the state of a single check item
     * @param  {[type]} checkItemIndex      The index of the check item to be changed
     * @param  {[type]} checkItemIndexState The new state of the check item
     * @param  {[type]} asset               The asset on which the operation needs to be performed
     * @param  {[type]} state               The state information of the current asset
     * @param  {[type]} am                  [description]
     */
    var updateCheckItemState = function(checkItemIndex, checkItemIndexState, asset, state, am) {
        //Check if the index provided is valid
        var msg;
        if ((checkItemIndex < 0) || (checkItemIndex > state.checkItems.length-1)) {
            msg = 'Unable to change the state of the check item as the index does not point to a valid check item.The check item index must be between 0 and ' + state.checkItems.length+'.';
            log.warn(msg);
            throw msg;
            //return;
        }
        //Check if the check item state is the same as the next state
        if (state.checkItems[checkItemIndex].checked == checkItemIndexState) {
            msg= 'The state of the check item at index ' + checkItemIndex + ' was not changed as it is already ' + checkItemIndexState;
            log.warn(msg);
            //throw msg;
        }       
        //Invoke the state change
        try {
            am.invokeLifecycleCheckItem(asset, checkItemIndex, checkItemIndexState);
        } catch (e) {
            log.error(e);
            msg = 'Unable to change the state of check item ' + checkItemIndex + ' to ' + checkItemIndexState;
            throw msg;
        }
    };

    /**
     * The function updates the check items for a given asset
     * @param  {[type]} options [description]
     * @param  {[type]} asset   [description]
     * @param  {[type]} am      [description]
     * @param  {[type]} state   [description]
     * @return {[type]}         [description]
     */
    var updateCheckItemStates = function(options, asset, am, state) {
        var success = false;
        var msg = '';
        //Check if the current state has any check items
        if ((state.checkItems) && (state.checkItems.length < 1)) {
            msg = 'Unable to change the state of the check item as the current state(' + state.id + ') does not have any check items';
            log.warn(msg);
            throw  msg;
        }
        //Check if the check items has been provided
        if (!options.checkItems) {
            log.warn('Unable to update check items as no check items have been provided.');
            success = true;
            return success;
        }       
        //Assume checking items will succeed
        success = true;
        var checkItemsList = options.checkItems;
        var checkItemIndex;
        var checkItemIndexState;
        var checkItem;
        //Go through each check item in the check items
        for (var index in checkItemsList) {
            checkItem = checkItemsList[index];
            checkItemIndex = checkItem.index;
            checkItemIndexState = checkItem.checked;
            if ((checkItemIndex!=null) && (checkItemIndexState==true || checkItemIndexState==false)) {
                updateCheckItemState(checkItemIndex, checkItemIndexState, asset, state, am);          
            }
        }
        return success;
    };

    /**
     * The function will obtain the state detail of a asset 
     * @return {[type]}         A JSON object definining the structure of the lifecycle
     * @param  {[type]} options :options.id=<asset-id>
     * @param  {[type]} req     :request
     * @param  {[type]} res     :response
     * @param  {[type]} session  : sessionID
     */
    api.getState = function(options, req, res, session) {
        var state = {};
        validateOptions(options, req, res, session);
        var assetApi = require('rxt').asset;
        var coreApi = require('rxt').core;
        var am = assetApi.createUserAssetManager(session, options.type);
        var server = require('store').server;
        var user = server.current(session);
        var tenantId = user.tenantId;
        var asset = getAsset(options, am);
        validateAsset(asset, options);
        var lcApi = require('lifecycle').api;
        var lifecycle = lcApi.getLifecycle(asset.lifecycle, tenantId);
        var rxtManager = coreApi.rxtManager(tenantId);
        //Obtain the state data
        state = lifecycle.state(asset.lifecycleState);
        if (!state) {
            throw 'Unable to locate state information for ' + asset.lifecycleState;
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
     * @return {[type]}         A JSON object definining the structure of the lifecycle
     */
    api.getLifecycle = function(options, req, res, session) {
        var lcApi = require('lifecycle').api;
        var server = require('store').server;
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
     * @return {[type]}         An array of strings with the names of the lifecycles
     */
    api.getLifecycles = function(options, req, res, session) {
        var lcApi = require('lifecycle').api;
        var server = require('store').server;
        var lifecycle = null;
        var user = server.current(session);
        var lifecycles = lcApi.getLifecycleList(user.tenantId);
        return lifecycles;
    };

    /**
     * The function changes the state of a set of check items sent as an array with each element been  { index: number , checked:true}
     * @return {[type]}         A boolean value indicating the success of the operation
     */
    api.checkItems = function(options, req, res, session) {
        var success = false;
        validateOptions(options, req, res, session);
        var assetApi = require('rxt').asset;
        var am = assetApi.createUserAssetManager(session, options.type);
        var asset = getAsset(options, am);
        validateAsset(asset, options);
        var state = this.getState(options, req, res, session);
        success = changeCheckitemState(options, asset, am, state, true);
        return success;
    };
}(api));