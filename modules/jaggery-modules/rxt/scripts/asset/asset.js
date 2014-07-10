var asset = {};
(function(asset, core) {
    function AssetManager(registry, type, ctx) {
        this.registry = registry;
        this.type = type;
    }
    AssetManager.prototype.init = function() {};
    AssetManager.prototype.create = function(options) {};
    AssetManager.prototype.update = function(options) {};
    AssetManager.prototype.remove = function(options) {};
    AssetManager.prototype.search = function(options) {};
    AssetManager.prototype.invokeAction = function(options) {};
    AssetManager.prototype.createVersion = function(options, newVersion) {};

    function AssetRenderer(rxtManager, type) {
        this.rxtManager = rxtManager;
        this.type = type;
    }
    AssetRenderer.prototype.create = function(asset) {};
    AssetRenderer.prototype.update = function(asset) {};
    AssetRenderer.prototype.listAsset = function(asset) {};
    AssetRenderer.prototype.listAssets = function(assets) {};
    AssetRenderer.prototype.leftNav = function(asset) {};
    /**
     * The function create an asset manage given a registry instance,type and tenantId
     * @param  {[type]} tenantId The id of the tenant
     * @param  {[type]} registry The registry instance used to create the underlying artifact manager
     * @param  {[type]} type     The type of the assets managed by the asset manager
     * @return An asset manager instance
     */
    var createAssetManager = function(tenantId, registry, type) {
        var reflection = require('utils').reflection;
        var assetManager = new AssetManager(registry, type);
        var assetResourcesTemplate = core.assetResources(tenantId, type);
        var context = core.createAssetContext(tenantId, type);
        var assetResources = assetResourcesTemplate.manager ? assetResourcesTemplate.manager(context) : {};
        reflection.override(assetManager, assetResources);
        return assetManager;
    };
    var createRenderer = function(session, type) {
        var reflection = require('utils').reflection;
        var context = core.createAssetContext(session, type);
        var assetResources = core.assetResources(context.tenantId, type);
        context.endpoints = asset.getAssetEndpoints(session, type);
        var customRenderer = (assetResources.renderer) ? assetResources.renderer(context) :{};
        var rxtManager = core.rxtManager(context.tenantId);
        var renderer = new AssetRenderer(rxtManager, type);
        reflection.override(renderer, customRenderer);
    };
    /**
     * The function will create an Asset Manager instance using the registry of the currently
     * logged in user
     * @return An Asset Manager instance which will store assets in the currently logged in users registry
     */
    asset.createUserAssetManager = function(session, type) {
        var server = require('store').server;
        var user = require('store').user;
        var userDetails = server.current(session);
        var userRegistry = user.userRegistry(session);
        return createAssetManager(userDetails.tenantId, userRegistry, type);
    };
    /**
     * The function will create an Asset Manager using the system registry of the provided tenant
     * @return An Asset Manager
     */
    asset.createSystemAssetManager = function(tenantId, type) {
        var server = require('store').server;
        var sysRegistry = server.systemRegistry(tenantId);
        return createAssetManager(tenantId, sysRegistry, type);
    };
    asset.createRenderer = function(session, type) {
        return createRenderer(session, type);
    };
    /**
     * The function obtains a list of all endpoints available to currently
     * logged in user for the provided asset type
     * @param  {[type]} session [description]
     * @param  {[type]} type    [description]
     * @return {[type]}         [description]
     */
    asset.getAssetEndpoints = function(session, type) {
        log.info('Starting to create context');
        var context = core.createAssetContext(session, type);
        log.info('Finished building context');
        var assetResources = core.assetResources(context.tenantId, type);
        return assetResources.server ? assetResources.server(context).endpoints : {};
    };
}(asset, core))