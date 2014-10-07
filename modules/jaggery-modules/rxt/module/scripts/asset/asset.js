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
/**
 * The asset namespace contains methods and classes that are used to work with the asset extension model
 * @namespace
 * @example
 *     var asset = require('rxt').asset;
 *     asset.init();
 * @requires event
 * @requires utils
 * @requires store
 * @requires Packages.org.wso2.carbon.governance.api.util.GovernanceUtils
 */
var asset = {};
(function(asset, core) {
    var log = new Log('rxt.asset');
    var DEFAULT_TIME_STAMP_FIELD = 'overview_createdtime'; //TODO:Move to constants
    var DEFAULT_RECENT_ASSET_COUNT = 5; //TODO: Move to constants
    var GovernanceUtils = Packages.org.wso2.carbon.governance.api.util.GovernanceUtils;
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
    /**
     * Represents an interface for performing CRUD operations
     * on assets.Theis class uses the GovernanceArtifactManager
     * to perform asset operations.This class should not be instantiated directly
     * @example
     *     var am=core.createUserAssetManager()
     *     var am=core.createAnonAssetManager();
     * @class AssetManager
     * @constructor
     * @param {Object} registry   carbon Registry instance
     * @param {String} type       The asset type  for which the CRUD interface will be created
     * @param {Object} rxtManager An instance of RxtManager class
     * @param {Object} renderer   An instance of the AssetRenderer class
     * @memberOf asset
     */
    function AssetManager(registry, type, rxtManager, renderer) {
        this.registry = registry;
        this.rxtManager = rxtManager;
        this.rxtTemplate = rxtManager.getRxtDefinition(type);
        this.type = type;
        this.r = renderer;
        this.am = null;
        //TODO: Move to constansts
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
    /**
     * Creates a new asset instance by calling the underlying artifact manager on
     * a JSON object representing the asset.If the asset is added successfully the
     * an id property is added to the options object and is updated with the id
     * of the created asset
     * @example
     *     var asset={};
     *     var attributes=asset.attributes={};
     *     attributes.overview_name='a';
     *     attributes.overview_version='1.0.0';
     *     am.create(asset);
     * @param  {Object} options A JSON object representing the asset to be created
     * @memberOf AssetManager
     * @instance
     * @function create
     * @lends AssetManager.prototype
     */
    AssetManager.prototype.create = function(options) {
        var id = this.am.add(options);
        if (!id) {
            log.warn('Unable to set the id of the newly created asset.The following asset may not have been created :' + stringify(asset));
            return;
        }
        options.id = id;
    };
    /**
     * Updates an existing asset instance.The provided asset instance must have the id property as well as all
     * asset attributes
     * @param  {Object} options A JSON object of the asset instance to be updated
     */
    AssetManager.prototype.update = function(options) {
        this.am.update(options);
    };
    /**
     * Removes the asset instance with the provided id
     * @param  {String} id  A UUID representing an asset instance to be removed
     * @throws The asset manager delete method requires an id to be provided
     * @throws An artifact manager instance has not been set for this asset manager.Make sure init method is called prior to invoking
     * CRUD operations
     */
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
     * Updates the provided asset with the latest values in the registry.If the asset is not succsessfully
     * synched with the registrycounterpart a false value is returned.It gurantees that all properties in
     * the provided asset are returned
     * @param  {Object} asset An JSON object representing the asset to be updated
     * @return {Boolean}      Returns true if the asset is successfully updated to the values of the registry
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
    /**
     * Returns a set of assets restricted by the paging object.If a paging object is not provided then a default
     * paging object is used
     * @param  {Object} paging A paging object (Optional)
     * @return {Array}         An array of asset instance of the type instance for the asset manager
     * @throws An artifact manager instance manager has not been set for this asset manager.Make sure init method is called prior to invoking other operations.
     */
    AssetManager.prototype.list = function(paging) {
        var paging = paging || this.defaultPaging;
        if (!this.am) {
            throw 'An artifact manager instance manager has not been set for this asset manager.Make sure init method is called prior to invoking other operations.';
        }
        var assets = this.am.list(paging);
        addAssetsMetaData(assets, this);
        return assets;
    };
    /**
     * Returns the asset with the provided id
     * @param  {String} id A UUID representing an asset instance
     * @return {Object}    An asset instance
     */
    AssetManager.prototype.get = function(id) {
        if (!id) {
            throw 'The asset manager get method requires an id to be provided.';
        }
        if (!this.am) {
            throw 'An artifact manager instance manager has not been set for this asset manager.Make sure init method is called prior to invoking other operations.';
        }
        var asset = this.am.get(id);
        addAssetsMetaData(asset, this);
        return asset;
    };
    /**
     * Executes a query to retrieve a set of assets bounded by a paging object.If a paging object is not provided
     * then a default paging object is used
     * @example
     *     var query={};
     *     var attributes = query.attributes={};
     *     attributes.overview_name='wso2';
     *     //Invocation without a paging object
     *     var assets = am.search(query);
     *     //Invocation with a paging object
     *     var assets = am.search(query,paging);
     * @param  {Object} query  A JSON object representing a query to be executed
     * @param  {Object} paging A paging object
     * @return {Array}        An array of asset instances filtered by the query object
     */
    AssetManager.prototype.search = function(query, paging) {
        var paging = paging || this.defaultPaging;
        if (!this.am) {
            throw 'An artifact manager instance manager has not been set for this asset manager.Make sure init method is called prior to invoking other operations.';
        }
        var assets = this.am.search(query, paging);
        addAssetsMetaData(assets, this);
        return assets;
    };
    /**
     * Returns the assets that have been recently added.A user can optionally define a query and the count of assets to be returned.The default count
     * is set to 5
     * The temporal nature of an asset is determined by a stamp attribute that can be defined in an asset configuration callback.By default
     * the temporal nature of an asset will be checked based on the overview_createdtime property.
     * @example
     *     //option 1
     *     var assets = am.recentAssets();
     *
     *     //option 2
     *     var query={};
     *     var attributes=query.attributes={};
     *     var count=100;   //The number of recent assets to be returned
     *     var assets =  am.recentAssets({
     *         q:query,
     *         count:count
     *     });
     *
     * @param  {Object} opts An optional object that may contain a query
     * @return {Array}       An array of recent assets
     */
    AssetManager.prototype.recentAssets = function(opts) {
        var opts = opts || {};
        var count = opts.count || constants.RECENT_ASSET_COUNT;
        var q = opts.q || {};
        var items = [];
        var timeStampField = this.rxtManager.getTimeStampAttribute(this.type);
        if (!timeStampField) {
            log.warn('There is no time stamp field defined for type: ' + this.type + '.A default time stamp field : ' + constants.DEFAULT_TIME_STAMP_FIELD + ' will be used.Add a timestampField property to the configuration to change this.');
            timeStampField = constants.DEFAULT_TIME_STAMP_FIELD;
        }
        var paging = constants.DEFAULT_RECENT_ASSET_PAGIN;
        paging.count = count || paging.count;
        paging.sortBy = timeStampField;
        var items = this.search(q, paging);
        //addAssetsMetaData(items, this);
        return items;
    };
    AssetManager.prototype.popularAssets = function(opts) {
        var opts = opts || {};
        var count = opts.count || constants.POPULAR_ASSET_COUNT;
        var q = opts.q || {};
        var items = [];
        var nameField = this.rxtManager.getNameAttribute(this.type);
        if (!nameField) {
            log.warn('There is no name field defined for type: ' + this.type + '.Unable to retrieve popular assets.');
            return items;
        }
        var paging = constants.DEFAULT_POPULAR_ASSET_PAGIN;
        paging.count = count || paging.count;
        paging.sortBy = nameField;
        var items = this.search(q, paging);
        return items;
    };
    /**
     * Returns the set of tags for a given asset instance or asset type
     * If an id is provided then the tags of that particular asset is returned,else
     * all of the tags applied to the current asset type are returned
     * @example
     *     var tags = am.tags(); //This will return all tags of the am's type
     *
     *     var tagName=tags[0].name;
     *     var tagAppliedCount=tags[0].count;
     *
     * @param  {String} id A UUID representing an asset instance
     * @return {Array}     An array of tag name value pairs
     */
    AssetManager.prototype.tags = function() {
        var tag, tags, assetType, i, length, count;
        var tagz = [];
        var tz = {};
        tags = this.registry.query(constants.TAGS_QUERY_PATH);
        length = tags.length;
        for (i = 0; i < length; i++) {
            assetType = tags[i].split(';')[0].split('/')[3];
            if (assetType != undefined) {
                if (assetType.contains(this.type)) {
                    tag = tags[i].split(';')[1].split(':')[1];
                    count = tz[tag];
                    count = count ? count + 1 : 1;
                    tz[tag] = count;
                }
            }
        }
        //api setter
        for (tag in tz) {
            if (tz.hasOwnProperty(tag)) {
                tagz.push({
                    name: String(tag),
                    count: tz[tag]
                });
            }
        }
        return tagz;
    };
    /**
     * Returns the list of assets that have the provided tag
     * attached to it.If a paging value is provided then it is used,else
     * a default set of paging values are used
     * @param  {String} tagName The tag name for which the applied assets must be returned
     * @param  {Object} paging  A paging object
     * @return {Array}          An array of assets that have the tag applied to them
     */
    AssetManager.prototype.tagged = function(tagName, paging) {
        var assets = [];
        var paging = paging || constants.DEFAULT_TAG_PAGIN;
        var q = {};
        q.tag = tagName;
        assets = this.search(q, paging);
        return assets;
    };
    /**
     * Adds a tag to a given asset
     * @param {String} id  A UUID representing an asset instance
     * @param {String} tag  The name of the tag
     */
    AssetManager.prototype.addTag = function(id, tag) {};
    AssetManager.prototype.untag = function(id, tag) {};
    /**
     * The method returns the rating value of a given asset
     * @param  {String} id A UUID representing an asset instance
     * @return {Object}    A rating object containing the average and user rating values
     */
    AssetManager.prototype.rating = function(id, username) {
        var rating = {};
        rating.average = 0;
        rating.user = 0;
        if (!id) {
            log.error('Unable to locate rating of asset: ' + id);
            return rating;
        }
        if (!username) {
            log.error('Unable to locate rating of asset: ' + id + ' since a username was not provided.');
            return rating;
        }
        try {
            rating = this.registry.rating(id, username);
        } catch (e) {
            log.error('Unable to obtain the rating value of asset: ' + id + ' for user: ' + username + '.Exception: ' + e);
        }
        return rating;
    };
    /**
     * Adds a rating value to an asset
     * @param {String} id     A UUID representing an asset instance
     * @param {Number} rating A value indicating the rating for the asset
     * @return {Boolean}    True if the rating is applied to the asset,else false
     */
    AssetManager.prototype.rate = function(id, rating) {
        var success = false;
        try {
            this.registry.rate(id, rating);
            success = true;
        } catch (e) {
            log.error('Could not rate the asset: ' + id + ' type: ' + this.type + '.Exception: ' + e);
            throw e;
        }
        return success;
    };
    /**
     * Subscribes a user to a given asset
     * @param  {String} id A UUID representing an asset instance
     * @return {Boolean}   True if the asset is successfully subscribed,else false
     */
    AssetManager.prototype.subscribe = function(id, session) {
        var path = this.getSubscriptionSpace(session);
        var success = false;
        if (!path) {
            log.warn('Unable to subscribe to ' + id + ' as the user space path was not located.');
            return success;
        }
        path += '/' + id;
        if (!this.registry.exists(path)) {
            this.registry.put(path, {
                name: id,
                content: ''
            });
            success = true;
        } else {
            log.debug('The user has already subscribed to asset : ' + id + ' or the path is invalid.');
        }
        return success;
    };
    /**
     * Unsubscribes a given user to an asset
     * @param  {String} id A UUID representing an asset instance
     * @return {Boolean}   True if the asset is successfully unsubscribed,else false
     */
    AssetManager.prototype.unsubscribe = function(id, session) {
        var path = this.getSubscriptionSpace(session);
        var success = false;
        if (!path) {
            log.warn('Unable to unsubscribe from ' + id + ' as the user space path was not located.');
            return success;
        }
        path += '/' + id;
        try {
            this.registry.remove(path);
            success = true;
        } catch (e) {
            log.error('Unable to unsubscribe from ' + id);
        }
        return success;
    };
    /**
     * Checks if the currently logged in user is subscribed to the provided asset.This method will internally
     * call the subscription method
     * @param  {String}  id      A UUID representing an asset instance
     * @param  {Object}  session Jaggery session object
     * @return {Boolean}          True if the user is subscribed to the asset,else false
     */
    AssetManager.prototype.isSubscribed = function(id, session) {
        //Obtain the list of all subscribptions of the user to this asset type
        var subscriptions = this.subscriptions(session);
        for (var index = 0; index < subscriptions.length; index++) {
            if (subscriptions[index].id === id) {
                return true;
            }
        }
        return false;
    };
    /**
     * Returns the list of asset subscriptions of the currently logged in user
     * @param  {Object} session Jaggery session object
     * @return {Array}          An array of asset instances for which the user has a subscription
     */
    AssetManager.prototype.subscriptions = function(session) {
        var userSpace = this.getSubscriptionSpace(session);
        var subscriptions = [];
        if (!userSpace) {
            log.error('Unable to retrieve subscriptions to type: ' + this.type + ' as the  subscription path could not be obtained');
            return subscriptions;
        }
        subscriptions = obtainSubscriptions(userSpace, this, this.registry, this.type);
        //Add the meta information of each asset
        addAssetsMetaData(subscriptions, this);
        return subscriptions;
    };
    AssetManager.prototype.getSubscriptionSpace = function(session) {
        var server = require('store').server;
        var modUser = require('store').user;
        var user = server.current(session);
        if (!user) {
            log.warn('Unable to obtain user space as there is no logged in user.Cannot retrieve the subscription space');
            return null;
        }
        var space = modUser.userSpace(user);
        return space + core.getAssetSubscriptionSpace(this.type);
    };
    var obtainSubscriptions = function(path, am, registry, type) {
        var items = [];
        var obj = registry.content(path);
        if (!obj) {
            log.debug('There is no content in the subscription path ' + path);
            return items;
        }
        obj.forEach(function(path) {
            try {
                var iteamOut = am.get(path.substr(path.lastIndexOf('/') + 1));
                if (iteamOut.lifecycleState == 'Published') {
                    iteamOut.isPublished = true;
                } else {
                    iteamOut.isPublished = false;
                }
                items.push(iteamOut);
            } catch (e) {
                log.warn('asset for path="' + path + '" could not be retrieved, try reverting it form registry.');
            }
        });
        return items;
    };
    /**
     * Attachs a lifecycle to the provided asset.The type of
     * lifecycle will be read from the configuration if a lifecycle is not provided.If a lifecycle cannot be found
     * then then method will return false.
     * @param {Object} asset An asset instance to which the lifecycle must be attached
     * @param  {String} lifecycle An optional string that defines a lifecycle name
     * @return {Boolean} True if the lifecycle is successfully attached
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
     * Invokes the provided lifecycle action
     * @param  {Object} asset  The asset for which a lifecycle action must be completed
     * @param  {String} action The lifecycle action to be performed
     * @return {Boolean}        True if the action is invoked,else false
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
     * Sets the check item at the provided index to the given state
     * @param  {Object} asset          The asset instance for which the lifecycle check list item must be changed
     * @param  {Number} checkItemIndex The index of the check list item to be invoked
     * @param  {Boolean} checkItemState A boolean value which indicates the state of the check item
     *                                 (checked=true and unchecked=false)
     * @return {Boolean}                A boolean value indicating whether the check item state was changed
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
     * Returns all of the check list items for the asset
     * @param  {Object} asset An asset instance
     * @return {Array}       An array of check items along with the checked state
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
     * Returns an array of all fields that represent resources of the asset
     * such as thumbnails,banners and content
     * @return {Array} An array of attribute fields
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
     * @param {Object} asset The asset to be  enriched
     * @param {Object} am    An asset manager instance
     */
    var addAssetsMetaData = function(asset, am) {
        if (!asset) {
            log.error('Unable to add meta data to an empty asset');
            return;
        }
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
        if ((!asset) || (!asset.attributes)) {
            log.warn('Could not populate asset details of  type: ' + am.type);
            return;
        }
        asset.name = am.getName(asset);
        asset.thumbnail = am.getThumbnail(asset);
        asset.banner = am.getBanner(asset);
        asset.rating = 0;
    };
    /**
     * Combines page details with the asset details combined with the rxt template.If an array of assets
     * is provided then the assets are not merged with the rxt template
     * @param  {Object} assets A asset instance
     * @param  {Object} page   A UI Page object
     * @return {Object}        processed page object
     */
    AssetManager.prototype.render = function(assets, page) {
        //Only process assets if both assets and pages are provided
        if (arguments.length == 2) {
            var refUtil = require('utils').reflection;
            //Combine with the rxt template only when dealing with a single asset
            if (refUtil.isArray(assets)) {
                page = renderMultipleAssetsPage(page, assets, this);
            } else {
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
     * Combines the assets details with the rxt template.
     * it will return the asset as a simple JSON object containing no extra meta data
     * @param  {Object} assets A asset instance
     * @param  {Object} page   A UI Page object
     * @return {Object}        processed page object
     */
    AssetManager.prototype.renderBasic = function(assets, page) {
        //Only process assets if both assets and pages are provided
        if (arguments.length == 2) {
            var refUtil = require('utils').reflection;
            //Combine with the rxt template only when dealing with a single asset
            if (refUtil.isArray(assets)) {
                page = renderMultipleAssetsPage(page, assets, this);
            } else {
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
    /**
     * Returns the set of categories for an asset type.If a category field is not present then an
     * empty array will be returned
     * @return {Array} An array of strings representing the categories of the asset type
     */
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
            log.debug('All of the ' + this.type + ' fields can be searched.');
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
    /**
     * Represents the rendering interface of an asset
     * @class
     * @constructor
     */
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
    AssetRenderer.prototype.buildAssetPageUrl = function(type, endpoint) {
        return core.getAssetPageUrl(type, endpoint);
    };
    AssetRenderer.prototype.buildAssetApiUrl = function(type, endpoint) {
        return core.getAssetApiUrl(type, endpoint);
    };
    AssetRenderer.prototype.buildAppPageUrl = function(endpoint) {
        return core.getAppPageUrl(endpoint);
    };
    AssetRenderer.prototype.buildAppApiUrl = function(endpoint) {
        return core.getAppApiUrl(endpoint);
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
    AssetRenderer.prototype.leftNav = function(page) {};
    AssetRenderer.prototype.ribbon = function(page) {};
    /**
     * Apply decorators to a given page.If a user passes in an array of decorators to use
     * then only those decorators are applied.If an array is not provided then all registered decorators are applied
     * to the page
     * @param  {Object} page
     * @param  {Array} decoratorsToUse
     * @return {Object}
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
    };
    /**
     * Creates an asset manage given a registry instance,type and tenantId
     * @param  {Number} tenantId The id of the tenant
     * @param  {Object} registry The registry instance used to create the underlying artifact manager
     * @param  {String} type     The type of the assets managed by the asset manager
     * @return {Object}
     */
    var createAssetManager = function(session, tenantId, registry, type) {
        var reflection = require('utils').reflection;
        var rxtManager = core.rxtManager(tenantId);
        var assetManager = new AssetManager(registry, type, rxtManager);
        var assetResourcesTemplate = core.assetResources(tenantId, type);
        var context = core.createAssetContext(session, type);
        var assetResources = {}; //Assume there will not be any asset managers to override the default implementations
        //Check if there are any asset managers defined at the type level
        if (!assetResourcesTemplate.manager) {
            //Check if a default manager exists
            if (assetResourcesTemplate._default.manager) {
                assetResources = assetResourcesTemplate._default.manager(context);
            }
        } else {
            assetResources = assetResourcesTemplate.manager(context);
        }
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
        return renderer;
    };
    /**
     * The function will combine two arrays of endpoints together.If a common endpoint is found then
     * the information in the otherEndpoints array will be used to update the endpoints array.
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
     * @param  {Object} session [Jaggery session object
     * @param  {String} type    The type of asset
     * @return {Object} A server callback
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
     * Creates an Asset Manager instance using the registry of the currently
     * logged in user.This asset manager can be used to perform CRUD operations on pages
     * that are only accessible by logging.
     * This method should NOT be used to create asset manager instances in pages that
     * can be accessed by annonymous users
     * @param {Object} session Jaggery session object
     * @param {String} type The asset type for which the asset manager must be created
     * @return {object} An Asset Manager instance which will store assets in the currently logged in users registry
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
     * Create san Asset Manager using the system registry of the provided tenant
     * @param {String} tenantId The tenant ID
     * @param {type}    type    The asset type for which the asset manager must be created
     * @return {Object} An asset manager instance
     */
    asset.createSystemAssetManager = function(tenantId, type) {
        var server = require('store').server;
        var sysRegistry = server.systemRegistry(tenantId);
        return createAssetManager(tenantId, sysRegistry, type);
    };
    /**
     * Creates an asset manager instance using the annoymous registry.
     * This method should be used pages that can be accessed by annoymous users.
     * @param  {Object} session  Jaggery session object
     * @param  {String} type      The asset type for which the asset manager must be created
     * @param  {Number} tenantId  Tenant ID
     * @return {Object}          An asset manager instance
     */
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
     * Returns a list of all endpoints available to currently
     * logged in user for the provided asset type
     * @param  {Object} session Jaggery session
     * @param  {String} type     The asset type for which the endpoints must be returned
     * @return {Object}         A JSON object containing a api and page endpoints
     */
    asset.getAssetEndpoints = function(session, type) {
        var serverCb = createServer(session, type);
        return serverCb ? serverCb.endpoints : {};
    };
    /**
     * Returns the API endpoints accessible to the currently logged in user for the provided
     * asset type.This method internally calls the getAssetEndpoints
     * @param  {Object} session Jaggery session object
     * @param  {String} type    The asset type for which the API endpoints should be returned
     * @return {Array}          An array of API endpoints
     */
    asset.getAssetApiEndpoints = function(session, type) {
        var endpoints = this.getAssetEndpoints(session, type);
        return endpoints['apis'] || [];
    };
    /**
     * Returns the page endpoints accessible to the currently logged in user for the provided
     * asset type.This method internally calls the getAssetEndpoints
     * @param  {Object} session Jaggery session object
     * @param  {String} type    The asset type for which the API endpoints should be returned
     * @return {Array}          An array of page endpoints
     */
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
    /**
     * Returns the details of a specific API endpoint matched by the provided endpoint name
     * @param  {String} type         The asset type for which the endpoint must be matched
     * @param  {String} endpointName An endpoint name
     * @return {String}              The path to the controller
     */
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
    /**
     * Returns the details of a specific page endpoint matched by the provided endpoint name
     * @param  {String} type         The asset type for which the endpoint must be matched
     * @param  {String} endpointName An endpoint name
     * @return {String}              The path to the controller
     */
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
        var resPath = path;
        path = '/' + path;
        //Determine the type of the asset
        var uriMatcher = new URIMatcher(request.getRequestURI());
        var extensionMatcher = new URIMatcher(path);
        //TODO: Use the constants
        var uriPattern = '/{context}/asts/{type}/{+options}';
        var extensionPattern = '/{root}/extensions/assets/{type}/{+suffix}';
        uriMatcher.match(uriPattern);
        extensionMatcher.match(extensionPattern);
        var pathOptions = extensionMatcher.elements() || {};
        var uriOptions = uriMatcher.elements() || {};
        //If the type is not metioned then return the path
        if (!pathOptions.type) {
            //Determine if the paths occur within the extensions directory
            var extensionResPath = '/extensions/assets/' + uriOptions.type + '/themes/' + themeName + '/' + resPath;
            var resFile = new File(extensionResPath);
            if (resFile.isExists()) {
                return extensionResPath;
            }
            //log.info('Resource not present in extensions directory, using : ' + themeResolver.call(themeObj, path));
            return themeResolver.call(themeObj, path);
        }
        //Check if type has a similar path in its extension directory
        var extensionPath = '/extensions/assets/' + uriOptions.type + '/themes/' + themeName + '/' + pathOptions.root + '/' + pathOptions.suffix;
        var file = new File(extensionPath);
        if (file.isExists()) {
            return extensionPath;
        }
        //If an extension directory does not exist then use theme directory
        extensionPath = pathOptions.root + '/' + pathOptions.suffix;
        var modPath = themeResolver.call(themeObj, extensionPath);
        return modPath;
    };
}(asset, core))