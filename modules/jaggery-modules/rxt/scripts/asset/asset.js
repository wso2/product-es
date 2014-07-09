var asset = {};
(function(asset, core) {
    function AssetManager(registry, type) {
        this.registry = registry;
        this.type = type;
    }
    AssetManager.prototype.init = function() {};
    AssetManager.prototype.create = function(options) {};
    AssetManager.prototype.update = function(options) {};
    AssetManager.prototype.remove = function(options) {};
    AssetManager.prototype.search = function(options) {};
    var createContext=function(tenantId,type){
    	return{
    		tenantId:tenantId,
    		type:type
    	}
    };
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
        var context=createContext(tenantId, type);
       	var assetResources=assetResourcesTemplate.manager?assetResourcesTemplate.manager(context):{};
        reflection.override(assetManager, assetResources);
        return assetManager;
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
}(asset, core))