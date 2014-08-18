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
    var getOptionTextField = function(attributes, tableName, fieldName, field, table) {
        //Determine the number of headings 
        var subHeading = table.subheading ? table.subheading[0].heading : null;
        if (!subHeading) {
            return '';
        }
        var expression = tableName + '_' + fieldName;
        //Get the number of subheadings
        if (subHeading.length > 0) {
            expression = tableName + '_entry';
        }
        var value = attributes[expression];
        return value;
    };
    var resolveField = function(attributes, tableName, fieldName, field, table) {
        var value;
        switch (field.type) {
            case 'option-text':
                value = getOptionTextField(attributes, tableName, fieldName, field, table);
                break;
            default:
                value = getField(attributes, tableName, fieldName);
                break;
        }
        return parse(stringify(value));
    };
    var processOptionTextList = function(list) {
        //If there is a list then  it can be either an array or a string(If it is an array it sends it back as a Java array which is not detected)
        list = parse(stringify(list));
        var ref = require('utils').reflection;
        //Determine if the list is provided as a string 
        if (!ref.isArray(list)) {
            list = list.split(',');
        }
        var result = [];
        //Squash the array by 2 as the data sent in the post request will be a single array
        for (var index = 0; index <= (list.length - 2); index += 2) {
            result.push(list[index] + ':' + list[index + 1]);
        }
        return result;
    };
    var setField = function(field, attrName, data, attributes, table) {
        if (field.type == 'option-text') {
            var optionsSet = [];
            var textSet = [];
            var indexc;
            var length;
            var splitName;
            for (var dataField in data) {
                if (dataField.indexOf(attrName + '_option') == 0) {
                    splitName = dataField.split("_");
                    length = splitName.length;
                    indexc = splitName[length - 1];
                    optionsSet[indexc] = data[dataField];
                }
                if (dataField.indexOf(attrName + '_text') == 0) {
                    splitName = dataField.split("_");
                    length = splitName.length;
                    indexc = splitName[length - 1];
                    textSet[indexc] = data[dataField];
                }
            }
            var fullIndex = 0;
            var list = [];
            for (var singleIndex = 0; singleIndex < optionsSet.length; singleIndex++) {
                list[fullIndex] = optionsSet[singleIndex];
                fullIndex++;
                list[fullIndex] = textSet[singleIndex];
                fullIndex++;
            }
            //The options text fields need to be sent in with the name of table and entry postfix
            attrName = table.name + '_entry';
            var items = processOptionTextList(list);
            attributes[attrName] = items;
        } else {
            if (data[attrName]) {
                attributes[attrName] = data[attrName];
            } else {
                log.debug(attrName + ' will not be saved.');
            }
        }
        return attributes;
    };
    var dropEmptyFields = function(asset) {
        for (var key in asset.attributes) {
            if (asset.attributes[key] == '') {
                delete asset.attributes[key];
            }
        }
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
    AssetManager.prototype.create = function(options) {
        var id = this.am.add(options);
        if (!id) {
            log.warn('Unable to set the id of the newly created asset.The following asset may not have been created :' + stringify(asset));
            return;
        }
        options.id = id;
    };
    AssetManager.prototype.update = function(options) {
        this.am.update(options);
    };
    AssetManager.prototype.remove = function(options) {};
    /**
     * The method is responsible for updating the provided asset with the latest
     * values in the registry.If the asset is not succsessfully synched with the registry
     * counterpart a false value is returned.It gurantees that all properties in the provided
     * asset are returned
     * @param  {[type]} asset An asset object
     * @return {[type]}       True if the asset is succsessfully synched
     */
    AssetManager.prototype.synchAsset = function(asset) {
        var locatedAsset = false;
        var ref = require('utils').reflection;
        //If the asset id is provided then we can use the get method to retrieve the asset
        if (asset.id) {
            var regCopy = this.get(asset.id);
            if (regCopy) {
                locatedAsset = true;
                //Drop any hiddent methods since this is a Java object
                regCopy = parse(stringify(regCopy));
                ref.copyAllPropValues(regCopy, asset);
            }
            return locatedAsset;
        }
        log.warn('Switching to registry search to synch the provided asset as an id was not found in the provided asset: ' + stringify(asset));
        //Construct a query which mimics the attributes in the asset
        if (!asset.attributes) {
            log.warn('Unable to locate the asset in the registry as the provided asset does not have attributes.');
            return locatedAsset;
        }
        dropEmptyFields(asset);
        var clone = parse(stringify(asset.attributes));
        var result = this.am.find(function(instance) {
            for (var key in clone) {
                //Do not compare arrays.We assume that attribute properties are sufficient
                //to guarantee uniqueness
                if ((!ref.isArray(clone[key])) && (instance.attributes[key] != clone[key])) {
                    return false;
                }
            }
            return true;
        });
        if (result.length > 1) {
            log.warn('Too many assets matched the query.Unable to determine which asset to pick in order to synch: ' + stringify(asset));
            return locatedAsset;
        }
        //Update the provided asset
        ref.copyAllPropValues(result[0], asset);
        locatedAsset = true;
        return locatedAsset;
    };
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
        return this.am.search(query, paging);
    };
    /**
     * The function will attach a lifecycle to the provided asset.The type of
     * lifecycle will be read from the configuration if a lifecycle is not provided.If a lifecycle cannot be found
     * then then method return false.
     * @param  {[type]} lifecycle An optional string that defines a lifecycle present for the tenant
     * @return {[type]}
     */
    AssetManager.prototype.attachLifecycle = function(asset, lifecycle) {
        var lifecycle = lifecycle || '';
        var success = false;
        if (!asset) {
            log.error('Failed to attach a lifecycle as an asset object was not provided.');
            return success;
        }
        //Check if a lifecycle was provided,if not check if it is provided in the 
        //configuration
        if (lifecycle == '') {
            lifecycle = this.rxtManager.getLifecycleName(this.type);
        }
        //if the lifecycle is not present, then abort the operation
        if (lifecycle == '') {
            return success;
        }
        try {
            this.am.attachLifecycle(lifecycle, asset);
            success = true;
        } catch (e) {
            log.error('Failed to attach lifecycle: ' + lifecycle + ' to the asset: ' + stringify(asset) + '.The following exception was throw: ' + e);
        }
        return success;
    };
    AssetManager.prototype.invokeDefaultLcAction = function(asset) {
        var success = false;
        if (!asset) {
            log.error('Failed to invoke default  lifecycle action as an asset object was not provided.');
            return success;
        }
        var defaultAction = this.rxtManager.getDefaultLcAction(this.type);
        if (defaultAction == '') {
            log.warn('Failed to invoke default action of lifecycle as one was not provided');
            return success;
        }
        success = this.invokeLcAction(asset, defaultAction);
        return success;
    };
    /**
     * The function will invoke a provided lifecycle action
     * @param  {[type]} asset  The asset for which a lifecycle action must be completed
     * @param  {[type]} action The lifecycle action to be performed
     * @return {[type]}        True if the action is invoked,else false
     */
    AssetManager.prototype.invokeLcAction = function(asset, action) {
        var success = false;
        if (!action) {
            log.error('Failed to invokeAction as an action was not provided for asset: ' + stringify(asset));
            return success;
        }
        if (!asset) {
            log.error('Failed to invokeAction as an asset was not provided.');
            return success;
        }
        try {
            this.am.promoteLifecycleState(action, asset);
            success = true;
        } catch (e) {
            log.error('Failed to invoke action: ' + action + ' for the asset: ' + stringify(asset) + '.The following exception was thrown: ' + e);
        }
        return success;
    };
    /**
     * The function sets the check item at the provided index to the given state
     * @param  {[type]} asset          [description]
     * @param  {[type]} checkItemIndex The index of the check list item to be invoked
     * @param  {[type]} checkItemState A boolean value which indicates the state of the check item
     *                                 (checked=true and unchecked=false)
     * @return {[type]}                A boolean value indicating whether the check item state was changed
     */
    AssetManager.prototype.invokeLifecycleCheckItem = function(asset, checkItemIndex, checkItemState) {
        var success = false;
        if (!asset) {
            log.warn('Unable to locate asset details in order to invoke check item state change');
            return success;
        }
        //Check if a check item state has been provided
        if (checkItemState == null) {
            log.warn('The check item at index ' + checkItemIndex + ' cannot be changed as the check item state is not provided.');
            return success;
        }
        //Obtain the number of check items for this state
        var checkItems = this.getLifecycleCheckItems(asset);
        //Check if the check item index is valid
        if ((checkItemIndex < 0) || (checkItemIndex > checkItems.length)) {
            log.error('The provided check item index ' + checkItemIndex + ' is not valid.It must be between 0 and ' + checkItems.length);
            throw 'The provided check item index ' + checkItemIndex + ' is not valid.It must be between 0 and ' + checkItems.length;
        }
        success = true; //Assume the check item invocation will succeed
        try {
            if (checkItemState == true) {
                this.am.checkItem(checkItemIndex, asset);
            } else {
                this.am.checkItem(checkItemIndex, asset);
            }
        } catch (e) {
            log.error(e);
            success = false;
        }
        return success;
    };
    /**
     * The function returns all of the check items for the current state in which the provided
     * asset is in
     * @param  {[type]} asset [description]
     * @return {[type]}       [description]
     */
    AssetManager.prototype.getLifecycleCheckItems = function(asset) {
        return this.am.getCheckListItemNames(asset);
    };
    AssetManager.prototype.createVersion = function(options, newVersion) {};
    AssetManager.prototype.getName = function(asset) {
        var nameAttribute = this.rxtManager.getNameAttribute(this.type);
        if (asset.attributes) {
            var name = asset.attributes[nameAttribute];
            if (!name) {
                log.warn('Unable to locate nameAttribute: ' + nameAttribute + ' in asset: ' + stringify(asset));
                return '';
            }
            return asset.attributes[nameAttribute];
        }
        return '';
    };
    AssetManager.prototype.getThumbnail = function(asset) {
        var thumbnailAttribute = this.rxtManager.getThumbnailAttribute(this.type);
        if (asset.attributes) {
            var thumb = asset.attributes[thumbnailAttribute];
            if (!thumb) {
                log.warn('Unable to locate thumbnailAttribute ' + thumbnailAttribute + ' in asset ' + asset.id);
                return '';
            }
            return asset.attributes[thumbnailAttribute];
        }
        return '';
    };
    /**
     * The function provides an array of all fields that represent resources of the asset
     * such as thumbnails,banners and content
     * @return {[type]} An array of attribute fields
     */
    AssetManager.prototype.getAssetResources = function() {
        return this.rxtManager.listRxtFieldsOfType(this.type, 'file');
    };
    AssetManager.prototype.importAssetFromHttpRequest = function(options) {
        var tables = this.rxtManager.listRxtTypeTables(this.type);
        var asset = {};
        var attributes = {};
        var tables = this.rxtManager.listRxtTypeTables(this.type);
        var table;
        var fields;
        var field;
        if (options.id) {
            asset.id = options.id;
        }
        //Go through each table and obtain the value of each field
        for (var tableIndex in tables) {
            table = tables[tableIndex];
            fields = table.fields;
            for (var fieldName in fields) {
                field = fields[fieldName];
                var key = table.name + '_' + fieldName;
                attributes = setField(field, key, options, attributes, table);
            }
        }
        asset.attributes = attributes;
        asset.name = this.getName(asset);
        return asset;
    };
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
        var field;
        var attrFieldValue;
        //Go through each table in the template
        for (var tableIndex in tables) {
            table = tables[tableIndex];
            fields = table.fields;
            //Go through each field in the table
            for (var fieldName in fields) {
                field = fields[fieldName];
                field.name.tableQualifiedName = table.name + '_' + fieldName;
                //log.info(stringify(field));
                //Check if the field exists in the attributes list
                attrFieldValue = resolveField(asset.attributes || {}, table.name, fieldName, field, table);
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
            page.assets.name = this.getName(assets);
            page.assets.thumbnail = this.getThumbnail(assets);
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
    AssetRenderer.prototype.thumbnail = function(page) {
        return '';
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