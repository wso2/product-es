var asset = {};
(function(asset, core) {
    var log = new Log('asset');
    var GovernanceUtils = Packages.org.wso2.carbon.governance.api.util.GovernanceUtils;
    /**
     * The function locates the provided field and table name within an attributes
     * object
     * @param  {[type]} attributes [description]
     * @param  {[type]} fieldName  [description]
     * @param  {[type]} tableName  [description]
     * @return {[type]}            [description]
     */
    var getField = function(attributes, tableName, fieldName) {
        var expression = tableName + '_' + fieldName;
        var result = attributes[expression];
        return result;
    };

    function AssetManager(registry, type, rxtManager, renderer) {
        this.registry = registry;
        this.rxtManager = rxtManager;
        this.rxtTemplate = rxtManager.getRxtDefinition(type);
        this.type = type;
        this.r = renderer;
        this.am = null;
        this.defaultPaging = {
            'start': 0,
            'count': 1000,
            'sortOrder': 'desc',
            'sortBy': 'overview_createdtime',
            'paginationLimit': 1000
        };
    }
    AssetManager.prototype.init = function() {
        var carbon = require('carbon');
        GovernanceUtils.loadGovernanceArtifacts(this.registry.registry);
        this.am = new carbon.registry.ArtifactManager(this.registry, this.type);
    };
    AssetManager.prototype.create = function(options) {};
    AssetManager.prototype.update = function(options) {};
    AssetManager.prototype.remove = function(options) {};
    AssetManager.prototype.list = function(paging) {
        var paging = paging || this.defaultPaging;
        if (!this.am) {
            throw 'An artifact manager instance manager has not been set for this asset manager.Make sure init method is called prior to invoking other operations.';
        }
        return this.am.list(paging);
    };
    AssetManager.prototype.get = function(id) {
        if (!id) {
            throw 'The asset manager get method requires an id to be provided.';
        }
        if (!this.am) {
            throw 'An artifact manager instance manager has not been set for this asset manager.Make sure init method is called prior to invoking other operations.';
        }
        return this.am.get(id);
    };
    AssetManager.prototype.search = function(query, paging) {
        var paging = paging || this.defaultPaging;
        if (!this.am) {
            throw 'An artifact manager instance manager has not been set for this asset manager.Make sure init method is called prior to invoking other operations.';
        }
        return this.am.search(query, options);
    };
    AssetManager.prototype.invokeAction = function(options) {};
    AssetManager.prototype.createVersion = function(options, newVersion) {};
    AssetManager.prototype.combineWithRxt = function(asset) {
        var modAsset = {};
        modAsset.tables = [];
        modAsset.id = asset.id;
        modAsset.lifecycle = asset.lifecycle;
        modAsset.lifecycleState = asset.lifecycleState;
        modAsset.mediaType = asset.mediaType;
        modAsset.type = asset.type;
        modAsset.path = asset.path;
        var tables = this.rxtManager.listRxtTypeTables(this.type);
        var table;
        var fields;
        var attrFieldValue;
        //Go through each table in the template
        for (var tableIndex in tables) {
            table = tables[tableIndex];
            fields = table.fields;
            //Go through each field in the table
            for (var fieldName in fields) {
                //Check if the field exists in the attributes list
                attrFieldValue = getField(asset.attributes, table.name, fieldName);
                //If the field exists then update the value 
                if (attrFieldValue) {
                    fields[fieldName].value = attrFieldValue;
                }
            }
        }
        modAsset.tables = tables;
        return modAsset;
    };
    AssetManager.prototype.render = function(assets, page) {
        var refUtil = require('utils').reflection;
        //Combine with the rxt template only when dealing with a single asset
        if (refUtil.isArray(assets)) {
            page.assets = assets;
        } else {
            page.assets = this.combineWithRxt(assets);
        }
        page.rxt = this.rxtTemplate;
        var that = this;
        return {
            create: function() {
                page = that.r.create(page) || page;
                page = that.r.leftNav(page) || page;
                page = that.r.ribbon(page) || page;
                return page;
            },
            update: function() {
                page = that.r.update(page) || page;
                page = that.r.leftNav(page) || page;
                page = that.r.ribbon(page) || page;
                return page;
            },
            list: function() {
                page = that.r.list(page) || page;
                page = that.r.leftNav(page) || page;
                page = that.r.ribbon(page) || page;
                return page;
            },
            details: function() {
                page = that.r.details(page) || page;
                page = that.r.leftNav(page) || page;
                page = that.r.ribbon(page) || page;
                return page;
            },
            lifecycle: function() {
                page = that.r.lifecycle(page) || page;
                page = that.r.leftNav(page) || page;
                page = that.r.ribbon(page) || page;
                return page;
            }
        };
    };

    function AssetRenderer(pagesRoot, assetsRoot) {
        this.assetPagesRoot = pagesRoot;
        this.assetsPagesRoot = assetsRoot;
    }
    AssetRenderer.prototype.buildUrl = function(pageName) {
        return this.assetPagesRoot + '/' + pageName;
    };
    AssetRenderer.prototype.buildBaseUrl = function(type) {
        return this.assetsPagesRoot + type;
    };
    AssetRenderer.prototype.create = function(page) {};
    AssetRenderer.prototype.update = function(page) {};
    AssetRenderer.prototype.details = function(page) {};
    AssetRenderer.prototype.list = function(page) {};
    AssetRenderer.prototype.lifecycle = function(page) {};
    AssetRenderer.prototype.leftNav = function(page) {
        var log = new Log();
        log.info('Default leftnav');
    };
    AssetRenderer.prototype.ribbon = function(page) {};
    /**
     * The function create an asset manage given a registry instance,type and tenantId
     * @param  {[type]} tenantId The id of the tenant
     * @param  {[type]} registry The registry instance used to create the underlying artifact manager
     * @param  {[type]} type     The type of the assets managed by the asset manager
     * @return An asset manager instance
     */
    var createAssetManager = function(session, tenantId, registry, type) {
        var reflection = require('utils').reflection;
        var rxtManager = core.rxtManager(tenantId);
        var assetManager = new AssetManager(registry, type, rxtManager);
        var assetResourcesTemplate = core.assetResources(tenantId, type);
        var context = core.createAssetContext(session, type);
        var assetResources = assetResourcesTemplate.manager ? assetResourcesTemplate.manager(context) : {};
        reflection.override(assetManager, assetResources);
        //Initialize the asset manager
        assetManager.init();
        return assetManager;
    };
    var createRenderer = function(session, tenantId, type) {
        var reflection = require('utils').reflection;
        var context = core.createAssetContext(session, type);
        var assetResources = core.assetResources(tenantId, type);
        var customRenderer = (assetResources.renderer) ? assetResources.renderer(context) : {};
        var renderer = new AssetRenderer(asset.getAssetPageUrl(type), asset.getBaseUrl());
        reflection.override(renderer, customRenderer);
        return renderer;
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
        var am = createAssetManager(session, userDetails.tenantId, userRegistry, type);
        am.r = createRenderer(session, userDetails.tenantId, type);
        return am;
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
        var context = core.createAssetContext(session, type);
        var assetResources = core.assetResources(context.tenantId, type);
        return assetResources.server ? assetResources.server(context).endpoints : {};
    };
    asset.getAssetApiEndpoints = function(session, type) {
        var endpoints = this.getAssetEndpoints(session, type);
        return endpoints['apis'] || [];
    };
    asset.getAssetPageEndpoints = function(session, type) {
        var endpoints = this.getAssetEndpoints(session, type);
        return endpoints['pages'] || [];
    };
    asset.getAssetExtensionPath = function(type) {
        return '/extensions/assets/' + type;
    };
    asset.getAssetDefaultPath = function() {
        return '/extensions/assets/default';
    };
    asset.getAssetApiDirPath = function(type) {
        return asset.getAssetExtensionPath(type) + '/apis';
    };
    asset.getAssetPageDirPath = function(type) {
        return asset.getAssetExtensionPath(type) + '/pages';
    };
    asset.getAssetPageUrl = function(type) {
        return asset.getBaseUrl() + type;
    };
    asset.getBaseUrl = function() {
        return '/asts/';
    };
    asset.getAssetApiEndpoint = function(type, endpointName) {
        //Check if the path exists within the asset extension path
        var endpointPath = asset.getAssetApiDirPath(type) + '/' + endpointName;
        var endpoint = new File(endpointPath);
        if (!endpoint.isExists()) {
            endpointPath = asset.getAssetDefaultPath() + '/apis/' + endpointName;
            endpoint = new File(endpointPath);
            if (!endpoint.isExists()) {
                endpointPath = '';
            }
        }
        return endpointPath;
    };
    asset.getAssetPageEndpoint = function(type, endpointName) {
        //Check if the path exists within the asset extension path
        var endpointPath = asset.getAssetPageDirPath(type) + '/' + endpointName;
        var endpoint = new File(endpointPath);
        if (!endpoint.isExists()) {
            endpointPath = asset.getAssetDefaultPath() + '/pages/' + endpointName;
            endpoint = new File(endpointPath);
            if (!endpoint.isExists()) {
                endpointPath = '';
            }
        }
        return endpointPath;
    };
    asset.resolve = function(request, path, themeName, themeObj, themeResolver) {
        var log = new Log();
        //log.info('Path: ' + path);
        //log.info('Request: ' + request.getRequestURI());
        var resPath = path;
        path = '/' + path;
        //Determine the type of the asset
        var uriMatcher = new URIMatcher(request.getRequestURI());
        var extensionMatcher = new URIMatcher(path);
        var uriPattern = '/{context}/asts/{type}/{+options}';
        var extensionPattern = '/{root}/extensions/assets/{type}/{+suffix}';
        uriMatcher.match(uriPattern);
        extensionMatcher.match(extensionPattern);
        var pathOptions = extensionMatcher.elements() || {};
        var uriOptions = uriMatcher.elements() || {};
        //log.info('URI details: ' + stringify(uriMatcher.elements()));
        //log.info('Extension details: ' + stringify(extensionMatcher.elements()));
        //If the type is not metioned then return the path
        if (!pathOptions.type) {
            //Determine if the paths occur within the extensions directory
            var extensionResPath = '/extensions/assets/' + uriOptions.type + '/themes/' + themeName + '/' + resPath;
            var resFile = new File(extensionResPath);
            //log.info('Checking if resource exists: ' + extensionResPath);
            if (resFile.isExists()) {
                return extensionResPath;
            }
            //log.info('Resource not present in extensions directory, using : ' + themeResolver.call(themeObj, path));
            return themeResolver.call(themeObj, path);
        }
        //Check if type has a similar path in its extension directory
        var extensionPath = '/extensions/assets/' + uriOptions.type + '/themes/' + themeName + '/' + pathOptions.root + '/' + pathOptions.suffix;
        var file = new File(extensionPath);
        //log.info('Extension path: ' + extensionPath);
        if (file.isExists()) {
            //log.info('Final path: ' + extensionPath);
            return extensionPath;
        }
        //If an extension directory does not exist then use theme directory
        extensionPath = pathOptions.root + '/' + pathOptions.suffix;
        var modPath = themeResolver.call(themeObj, extensionPath);
        //log.info('Final path: ' + extensionPath);
        //log.info('Mod path: ' + modPath);
        return modPath;
    };
}(asset, core))