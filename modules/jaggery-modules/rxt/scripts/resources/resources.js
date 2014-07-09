var resources = {};
(function(core, resources) {
    var log = new Log();
    var getRxtExtensionPath = function(type, options) {
        return options.CONFIG_BASE_PATH + options.EXTENSION_PATH + type;
    };
    var getAssetScriptPath = function(type, options) {
        return getRxtExtensionPath(type, options) + options.ASSET_SCRIPT_PATH;
    };
    var getDefaultAssetScriptPath = function(options) {
        return options.DEFAULT_ASSET_SCRIPT;
    };
    var loadDefaultAssetScript = function(options, path, sysRegistry) {
        var defaultAssetScriptPath = getDefaultAssetScriptPath(options);
        var file = new File(defaultAssetScriptPath);
        var content;
        if (!file.isExists()) {
            throw 'The default asset script could not be found at : ' + defaultAssetScriptPath;
        }
        try {
            file.open('r');
            content = file.readAll();
            sysRegistry.put(path, {
                content: content,
                mediaType: 'application/javascript'
            });
        } catch (e) {
            log.debug('Unable to read the default asset script');
            throw 'The default asset script could not be read: ' + e;
        } finally {
            file.close();
        }
        return content;
    };
    var addToConfigs = function(tenantId, type, assetResource) {
        var configs = core.configs(tenantId);
        configs.assetResources = {};
        configs.assetResources[type] = assetResource;
    };
    var loadResources = function(options, tenantId, sysRegistry) {
        var manager = core.rxtManager(tenantId);
        var rxts = manager.listRxtTypes();
        var resourcePath;
        var type;
        var map = {};
        log.info('### Loading resources! ###');
        for (var index in rxts) {
            type = rxts[index];
            resourcePath = getAssetScriptPath(type, options);
            var content = sysRegistry.content(resourcePath);
            if (!content) {
                log.info('Asset script for ' + type + ' could not be found.The default asset script will be loaded from: ' + getDefaultAssetScriptPath(options));
                content = loadDefaultAssetScript(options, resourcePath, sysRegistry);
            }
            var module = 'function(asset,log){  '+content+' };';
            var modulePtr = eval(module);
            var asset = {};
            asset.manager = null;
            asset.ui = null;
            asset.server =null;
            asset.configure = null;
            modulePtr.call(this,asset, log);
            addToConfigs(tenantId, type,asset);
            //Perform any rxt mutations
            if (asset.configure) {
                log.info('### Applying mutations ###');
                log.info(asset.configure());
                manager.applyMutator(type, asset.configure());
            }
        }
    };
    var init = function(tenantId) {
        var server = require('store').server;
        var options = server.options();
        if (!options.rxt) {
            log.error('Unable to locate rxt configuation block in provided tenant configuration.Check the configuration file that is the registry');
            throw 'Unable to locate rxt configuation block in provided tenant configuration.Check the configuration file that is the registry';
        }
        options = options.rxt;
        var sysRegistry = server.systemRegistry(tenantId);
        loadResources(options, tenantId, sysRegistry);
    };
    resources.manager = function(tenantId) {
        init(tenantId);
    };
    resources.init = function() {
        var event = require('event');
        event.on('tenantLoad', function(tenantId) {
            init(tenantId);
        });
    };
}(core, resources));