var api = {};
(function(api) {
    var log = new Log('lifecycle_api');
    var getAsset = function(options, am) {
        var asset;
        try {
            asset = am.get(options.id);
        } catch (e) {
            log.warn('Unable to locate the asset with id: ' + options.id);
        }
        return asset;
    };
    var validateOptions = function(options, req, res, session) {
        if (!options.type) {
            log.error('Unable to obtain state information without knowing the type of asset ' + options.id);
            throw 'Unable to obtain state information without knowing the type of asset ' + options.id;
        }
    };
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
    api.changeState = function(options, req, res, session) {
        var success = false;
        validateOptions(options, req, res, session);
        if (!options.nextState) {
            log.warn('A next state has not been provided');
            return success;
        }
        //Obtain the tenantId
        var server = require('store').server;
        var asset = require('rxt').asset;
        var user = server.current(session);
        var tenantId = user.tenantId;
        var am = asset.createUserAssetManager(session, options.type);
        var asset = getAsset(options, am);
        validateAsset(asset, options)
        //Obtain the lifecycle
        var lcApi = require('lifecycle').api;
        var lifecycle = lcApi.getLifecycle(asset.lifecycle, tenantId);
        var action = lifecycle.transitionAction(asset.lifecycleState, options.nextState);
        if (!action) {
            log.warn('It is not possible to reach ' + options.nextState + ' from ' + asset.lifecycleState);
            return success;
        }
        success = am.invokeLcAction(asset, action);
        return success;
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
}(api));