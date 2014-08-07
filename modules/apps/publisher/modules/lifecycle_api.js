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
    api.changeState = function(options, req, res, session) {
        var success = false;
        if (!options.type) {
            log.warn('Unable to change state without knowing type of asset ' + options.id);
            return success;
        }
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
        if (!asset) {
            throw 'Unable to locate asset information of ' + options.id;
        }
        if (!asset.lifecycle) {
            throw 'The asset ' + options.id + ' does not have an associated lifecycle';
        }
        if (!asset.lifecycleState) {
            throw 'The asset ' + options.id + ' does not have a lifecycle state.';
        }
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
}(api));