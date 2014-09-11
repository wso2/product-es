var asset = {};
(function(asset, core) {
    var log = new Log('rxt.asset');
    var DEFAULT_TIME_STAMP_FIELD = 'overview_createdtime';
    var DEFAULT_RECENT_ASSET_COUNT = 5;
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
    AssetManager.prototype.remove = function(id) {
        if (!id) {
            throw 'The asset manager delete method requires an id to be provided.';
        }
        if (!this.am) {
            throw 'An artifact manager instance manager has not been set for this asset manager.Make sure init method is called prior to invoking other operations.';
        }
        this.am.remove(id);
    };
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
        //These methods do not return a boolean value indicating if the item was checked or unchecked
        //TODO: We could invoke getCheckLifecycleCheckItems and check the item index to see if the operation was successfull.
        try {
            if (checkItemState == true) {
                this.am.checkItem(checkItemIndex, asset);
            } else {
                this.am.uncheckItem(checkItemIndex, asset);
            }
        } catch (e) {
            log.error(e);
            success = false;
        }
        return success;
    };
    /**
     * The function obtains all recent assets with an optional query object to filter the results
     * @param  {[type]} query An optional query object
     * @return {[type]}       [description]
     */
    AssetManager.prototype.recentAssets = function() {
        var timeStampField = this.rxtManager.getTimeStampAttribute(this.type);
        var ref = require('utils').reflection;
        var results = [];
        if (!timeStampField) {
            log.warn('A timestamp field has not been defined for type: ' + this.type + '. Default time stamp field ' + DEFAULT_TIME_STAMP_FIELD + ' will be used.');
            timeStampField = DEFAULT_TIME_STAMP_FIELD;
        }
        var query = {};
        var options = {
            start: 0,
            count: DEFAULT_RECENT_ASSET_COUNT,
            sortBy: timeStampField,
            sort: 'older'
        };
        //Options object provided
        if (arguments.length == 1) {
            ref.copyAllPropValues(arguments[0], options);
        }
        //Options and Query object provided
        else if (arguments.length == 2) {
            ref.copyAllPropValues(arguments[0], options);
            query = arguments[1];
        }
        results = this.am.search(query, options);
        addAssetsMetaData(results, this);
        return results;
    };
    /**
     * The function returns all of the check items for the current state in which the provided
     * asset is in
     * @param  {[type]} asset [description]
     * @return {[type]}       An array of check items along with the checked state
     */
    AssetManager.prototype.getLifecycleCheckItems = function(asset) {
        var checkItems = [];
        try {
            checkItems = this.am.getCheckListItemNames(asset);
        } catch (e) {
            log.error(e);
        }
        return checkItems;
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
    AssetManager.prototype.getBanner = function(asset) {
        var bannerAttribute = this.rxtManager.getBannerAttribute(this.type);
        if (asset.attributes) {
            var banner = asset.attributes[bannerAttribute];
            if (!banner) {
                log.warn('Unable to locate bannerAttribute ' + bannerAttribute + ' in asset ' + asset.id);
                return '';
            }
            return asset.attributes[bannerAttribute];
        }
        return '';
    };
    AssetManager.prototype.getTimeStamp = function(asset) {
        var timestampAttribute = this.rxtManager.getTimeStampAttribute(this.type);
        if (asset.attributes) {
            var banner = asset.attributes[timestampAttribute];
            if (!banner) {
                log.warn('Unable to locate bannerAttribute ' + timestampAttribute + ' in asset ' + asset.id);
                return '';
            }
            return asset.attributes[timestampAttribute];
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
    var renderSingleAssetPage = function(page, assets, am) {
        page.assets.name = am.getName(assets);
        page.assets.thumbnail = am.getThumbnail(assets);
        page.assets.banner = am.getBanner(assets);
        page.assetMeta.categories = am.getCategories();
        page.assetMeta.searchFields = am.getSearchableFields();
        return page;
    };
    var renderSingleAssetPageCombined = function(page, assets, am) {
        page.assets = am.combineWithRxt(assets);
        renderSingleAssetPage(page, assets, am);
        return page;
    };
    var renderSingleAssetPageBasic = function(page, assets, am) {
        page.assets = assets;
        renderSingleAssetPage(page, assets, am);
        return page;
    };
    var renderMultipleAssetsPage = function(page, assets, am) {
        page.assets = assets;
        addAssetsMetaData(page.assets, am);
        page.assetMeta.categories = am.getCategories();
        page.assetMeta.searchFields = am.getSearchableFields();
        return page;
    };
    /**
     * The function is used to add meta data of assets such as the name , thumbnail and banner attributes
     * to an asset
     * @param {[type]} asset [description]
     * @param {[type]} am    [description]
     */
    var addAssetsMetaData = function(asset, am) {
        var ref = require('utils').reflection;
        if (ref.isArray(asset)) {
            var assets = asset;
            for (var index in assets) {
                addAssetMetaData(assets[index], am);
            }
        } else {
            addAssetMetaData(asset, am);
        }
    };
    var addAssetMetaData = function(asset, am) {
        asset.name = am.getName(asset);
        asset.thumbnail = am.getThumbnail(asset);
        asset.banner = am.getBanner(asset);
        //log.info(asset);
        log.info('Name: ' + am.getName(asset));
        log.info('Thumbnail: ' + am.getThumbnail(asset));
    };
    /**
     * The function will a render page with the asset details combined with the rxt template.If an array of assets
     * is provided then the assets are not merged with the rxt template
     * @param  {[type]} assets [description]
     * @param  {[type]} page   [description]
     * @return {[type]}        [description]
     */
    AssetManager.prototype.render = function(assets, page) {
        //Only process assets if both assets and pages are provided
        if (arguments.length == 2) {
            var refUtil = require('utils').reflection;
            //Combine with the rxt template only when dealing with a single asset
            if (refUtil.isArray(assets)) {
                // page.assets = assets;
                // page.assetMeta.categories = this.getCategories();
                // page.assetMeta.searchFields = this.getSearchableFields();
                page = renderMultipleAssetsPage(page, assets, this);
            } else {
                // page.assets = this.combineWithRxt(assets);
                // page.assets.name = this.getName(assets);
                // page.assets.thumbnail = this.getThumbnail(assets);
                // page.assets.categories = this.getCategories();
                // page.assets.searchFields = this.getSearchableFields();
                page = renderSingleAssetPageCombined(page, assets, this);
            }
        } else if (arguments.length == 1) {
            page = arguments[0];
        }
        page.rxt = this.rxtTemplate;
        var that = this;
        return {
            create: function() {
                page = that.r.create(page) || page;
                page = that.r.applyPageDecorators(page) || page;
                return page;
            },
            update: function() {
                page = that.r.update(page) || page;
                page = that.r.applyPageDecorators(page) || page;
                return page;
            },
            list: function() {
                page = that.r.list(page) || page;
                page = that.r.applyPageDecorators(page) || page;
                return page;
            },
            details: function() {
                page = that.r.details(page) || page;
                page = that.r.applyPageDecorators(page) || page;
                return page;
            },
            lifecycle: function() {
                page = that.r.lifecycle(page) || page;
                page = that.r.applyPageDecorators(page) || page;
                return page;
            },
            _custom: function() {
                page = that.r.applyPageDecorators(page) || page;
                return page;
            }
        };
    };
    /**
     * The function will not combine the assets details with the rxt template.Instead
     * it will return the asset as a simple JSON object containing no extra meta data
     * @param  {[type]} assets [description]
     * @param  {[type]} page   [description]
     * @return {[type]}        [description]
     */
    AssetManager.prototype.renderBasic = function(assets, page) {
        //Only process assets if both assets and pages are provided
        if (arguments.length == 2) {
            var refUtil = require('utils').reflection;
            //Combine with the rxt template only when dealing with a single asset
            if (refUtil.isArray(assets)) {
                // page.assets = assets;
                // page.assetMeta.categories = this.getCategories();
                // page.assetMeta.searchFields = this.getSearchableFields();
                page = renderMultipleAssetsPage(page, assets, this);
            } else {
                // page.assets = assets;
                // page.assets.name = this.getName(assets);
                // page.assets.thumbnail = this.getThumbnail(assets);
                // page.assets.categories = this.getCategories();
                // page.assets.searchFields = this.getSearchableFields();
                page = renderSingleAssetPageBasic(page, assets, this);
            }
        } else if (arguments.length == 1) {
            page = arguments[0];
        }
        page.rxt = this.rxtTemplate;
        var that = this;
        return {
            create: function() {
                page = that.r.create(page) || page;
                page = that.r.applyPageDecorators(page) || page;
                return page;
            },
            update: function() {
                page = that.r.update(page) || page;
                page = that.r.applyPageDecorators(page) || page;
                return page;
            },
            list: function() {
                page = that.r.list(page) || page;
                page = that.r.applyPageDecorators(page) || page;
                return page;
            },
            details: function() {
                page = that.r.details(page) || page;
                page = that.r.applyPageDecorators(page) || page;
                return page;
            },
            lifecycle: function() {
                page = that.r.lifecycle(page) || page;
                page = that.r.applyPageDecorators(page) || page;
                return page;
            },
            _custom: function() {
                page = that.r.applyPageDecorators(page) || page;
                return page;
            }
        };
    };
    AssetManager.prototype.getCategories = function() {
        var categoryField = this.rxtManager.getCategoryField(this.type);
        var categories = [];
        if (!categoryField) {
            log.warn('Unable to locate a categories field.Make sure a categories section has been provided in configuration callback ');
            return categories;
        }
        categories = this.rxtManager.getRxtFieldValue(this.type, categoryField);
        return categories;
    };
    AssetManager.prototype.getSearchableFields = function() {
        var searchFields = [];
        var fieldName;
        var field;
        var definedFields = this.rxtManager.getSearchableFields(this.type);
        //Deteremine if the user has specified keyword all.if so then all
        //fields can be searched
        if ((definedFields.length == 1) && (definedFields[0] == 'all')) {
            log.warn('All of the ' + this.type + ' fields can be searched.');
            searchFields = this.rxtManager.listRxtFields(this.type);
            return searchFields;
        }
        //Obtain the field definitions for each of the fields
        for (var index in definedFields) {
            fieldName = definedFields[index];
            field = this.rxtManager.getRxtField(this.type, fieldName);
            if (field) {
                searchFields.push(field);
            }
        }
        return searchFields;
    };

    function NavList() {
        this.items = [];
    }
    NavList.prototype.push = function(label, icon, url) {
        this.items.push({
            name: label,
            iconClass: icon,
            url: url
        });
    };
    NavList.prototype.list = function() {
        return this.items;
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
    AssetRenderer.prototype.navList = function() {
        return new NavList();
    };
    AssetRenderer.prototype.create = function(page) {};
    AssetRenderer.prototype.update = function(page) {};
    AssetRenderer.prototype.details = function(page) {};
    AssetRenderer.prototype.list = function(page) {};
    AssetRenderer.prototype.lifecycle = function(page) {};
    AssetRenderer.prototype.leftNav = function(page) {
        //var log = new Log();
        //log.info('Default leftnav');
    };
    AssetRenderer.prototype.ribbon = function(page) {};
    /**
     * The function will apply decorators to a given page.If a user passes in an array of decorators to use
     * then only those decorators are applied.If an array is not provided then all registered decorators are applied
     * to the page
     * @param  {[type]} page            [description]
     * @param  {[type]} decoratorsToUse [description]
     * @return {[type]}                 [description]
     */
    AssetRenderer.prototype.applyPageDecorators = function(page, decoratorsToUse) {
        var pageDecorators = this.pageDecorators || {};
        for (var key in pageDecorators) {
            page = pageDecorators[key].call(this, page) || page;
        }
        return page;
    };
    var isSelectedDecorator = function(decorator, decoratorsToUse) {
            if (decoratorsToUse.indexOf(decorator) > -1) {
                return true;
            }
            return false;
        }
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
    var overridePageDecorators = function(to, from) {
        var fromPageDecorators = from.pageDecorators || {};
        var toPageDecorators = to.pageDecorators || {};
        if (!to.pageDecorators) {
            to.pageDecorators = {};
        }
        for (var key in fromPageDecorators) {
            to.pageDecorators[key] = fromPageDecorators[key];
        }
    };
    var createRenderer = function(session, tenantId, type) {
        var reflection = require('utils').reflection;
        var context = core.createAssetContext(session, type);
        var assetResources = core.assetResources(tenantId, type);
        var customRenderer = (assetResources.renderer) ? assetResources.renderer(context) : {};
        var renderer = new AssetRenderer(asset.getAssetPageUrl(type), asset.getBaseUrl());
        var defaultRenderer = assetResources._default.renderer ? assetResources._default.renderer(context) : {};
        reflection.override(renderer, defaultRenderer);
        reflection.override(renderer, customRenderer);
        //Override the page decorators
        overridePageDecorators(renderer, defaultRenderer);
        overridePageDecorators(renderer, customRenderer);
        //reflection.override(renderer, defaultRenderer);
        //reflection.override(renderer, customRenderer);
        //log.info(assetResources.renderer.toSource());
        //log.info(renderer.toSource());
        //log.info('defaultRenderer: '+renderer.toSource());
        return renderer;
    };
    /**
     * The function will combine two arrays of endpoints together.If a common endpoint is found then
     * the information in the otherEndpoints array will be used to update the endpoints array.
     * @param  {[type]} endpoints      [description]
     * @param  {[type]} otherEndpoints [description]
     */
    var combineEndpoints = function(endpoints, otherEndpoints) {
        for (var index in otherEndpoints) {
            var found = false; //Assume the endpoint will not be located
            for (var endpointIndex = 0;
                ((endpointIndex < endpoints.length) && (!found)); endpointIndex++) {
                //Check if there is a similar endpoint and override the title and path
                if (otherEndpoints[index].url == endpoints[endpointIndex].url) {
                    endpoints[endpointIndex].url = otherEndpoints[index].url;
                    endpoints[endpointIndex].path = otherEndpoints[index].path;
                    found = true; //break the loop since we have already located the endpoint
                    log.debug('Overriding existing endpoint ' + otherEndpoints[index].url);
                }
            }
            //Only add the endpoint if it has not already been defined
            if (!found) {
                log.debug('Adding new endpoint ' + otherEndpoints[index].url);
                endpoints.push(otherEndpoints[index]);
            }
        }
    };
    /**
     * The method is used to build a server object that has knowledge about the available endpoints of an
     * asset type.It will first check if the asset type has defined a server callback in an asset.js.If one is present
     * then it will used to override the default server call back defined in the default asset.js.In the case of the
     * endpoint property it will combine the endpoints defined in the default asset.js.
     * @param  {[type]} session [description]
     * @param  {[type]} type    The type of asset
     * @return {[type]}
     */
    var createServer = function(session, type) {
        var context = core.createAssetContext(session, type);
        var assetResources = core.assetResources(context.tenantId, type);
        var reflection = require('utils').reflection;
        var serverCb = assetResources.server;
        var defaultCb = assetResources._default.server;
        if (!assetResources._default) {
            log.warn('A default object has not been defined for the type: ' + type + ' for tenant: ' + context.tenantId);
            throw 'A default object has not been defined for the type: ' + type + ' for tenant: ' + context.tenantId + '.Check if a default folder is present';
        }
        //Check if there is a type level server callback
        if (!serverCb) {
            defaultCb = defaultCb(context);
            serverCb = defaultCb;
        } else {
            defaultCb = defaultCb(context);
            serverCb = serverCb(context);
            //Combine the endpoints 
            var defaultApiEndpoints = ((defaultCb.endpoints) && (defaultCb.endpoints.apis)) ? defaultCb.endpoints.apis : [];
            var defaultPageEndpoints = ((defaultCb.endpoints) && (defaultCb.endpoints.pages)) ? defaultCb.endpoints.pages : [];
            var serverApiEndpoints = ((serverCb.endpoints) && (serverCb.endpoints.apis)) ? serverCb.endpoints.apis : [];
            var serverPageEndpoints = ((serverCb.endpoints) && (serverCb.endpoints.pages)) ? serverCb.endpoints.pages : [];
            combineEndpoints(defaultApiEndpoints, serverApiEndpoints);
            combineEndpoints(defaultPageEndpoints, serverPageEndpoints);
            if (!defaultCb.endpoints) {
                throw 'No endpoints found for the type: ' + type;
            }
            if (!serverCb.endpoints) {
                serverCb.endpoints = {};
                log.warn('Creating endpoints object for type: ' + type);
            }
            defaultCb.endpoints.apis = defaultApiEndpoints;
            serverCb.endpoints.apis = defaultApiEndpoints;
            defaultCb.endpoints.pages = defaultPageEndpoints;
            serverCb.endpoints.pages = defaultPageEndpoints;
            reflection.override(defaultCb, serverCb);
        }
        return defaultCb;
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
    asset.createAnonAssetManager = function(session, type, tenantId) {
        var server = require('store').server;
        var anonRegistry = server.anonRegistry(tenantId);
        var am = createAssetManager(session, tenantId, anonRegistry, type);
        am.r = createRenderer(session, tenantId, type);
        return am;
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
        //var assetResources = core.assetResources(context.tenantId, type);
        var serverCb = createServer(session, type);
        return serverCb ? serverCb.endpoints : {};
        //return assetResources.server ? assetResources.server(context).endpoints : {};
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