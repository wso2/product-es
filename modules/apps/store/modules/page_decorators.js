/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
var pageDecorators = {};
(function() {
    pageDecorators.navigationBar = function(ctx, page, utils) {
        var rxtManager = ctx.rxtManager;
        var app = require('rxt').app;
        //Obtain all of the available rxt types
        var availableTypes = app.getActivatedAssets(ctx.tenantId); //rxtManager.listRxtTypeDetails();
        var types = [];
        var type;
        var currentType = ctx.assetType;
        var log = new Log();
        page.navigationBar = {};
        for (var index in availableTypes) {
            type = availableTypes[index];
            currentType = rxtManager.getRxtTypeDetails(type); //availableTypes[index];
            currentType.selected = false;
            //currentType.listingUrl = utils.buildAssetPageUrl(availableTypes[index].shortName, '/list');
            currentType.listingUrl = utils.buildAssetPageUrl(currentType.shortName, '/list');
            if (currentType.shortName == ctx.assetType) {
                currentType.selected = true;
            }
            types.push(currentType);
        }
        page.navigationBar.types = types;
        return page;
    };
    /**
     * The function populates any text field as a search field
     * @param  {[type]} ctx  [description]
     * @param  {[type]} page [description]
     * @return {[type]}      [description]
     */
    pageDecorators.searchBar = function(ctx, page) {
        page.searchBar = {};
        page.searchBar.searchFields = [];
        var searchFields = page.assetMeta.searchFields;
        for (var index in searchFields) {
            if ((searchFields[index].type == 'text') || (searchFields[index].type == 'options')) {
                page.searchBar.searchFields.push(searchFields[index]);
            }
        }
        return page;
    };
    /**
     * The function populates the categories for the category box
     * @param  {[type]} ctx  [description]
     * @param  {[type]} page [description]
     * @return {[type]}      [description]
     */
    pageDecorators.categoryBox = function(ctx, page) {
        page.categoryBox = {};
        page.categoryBox.categories = page.assetMeta.categories;
        page.categoryBox.searchEndpoint = '/apis/assets?type=' + ctx.assetType;
        return page;
    };
    pageDecorators.authenticationDetails = function(ctx, page) {
        var authenticationMethods = ctx.tenantConfigs.authentication ? ctx.tenantConfigs.authentication : {};
        var activeMethod = authenticationMethods.activeMethod ? authenticationMethods.activeMethod : '';
        //Obtain the details for this method of authentication
        var authDetails = fetchActiveAuthDetails(activeMethod, authenticationMethods.methods || []);
        page.security.method = activeMethod;
        page.security.details = authDetails;
        return page;
    };
    pageDecorators.recentAssets = function(ctx, page) {
        var am = getAssetManager(ctx);
        var ratingApi = require('/modules/rating_api.js').api;
        var assets = am.recentAssets();
        ratingApi.addRatings(assets, am, ctx.tenantId, ctx.username);
        page.recentAssets = assets;
        return page;
    };
    pageDecorators.recentAssetsOfActivatedTypes = function(ctx, page) {
        var app = require('rxt').app;
        var asset = require('rxt').asset;
        var assets = {};
        var items = [];
        var assetsByType = [];
        var am;
        var type;
        var rxtDetails;
        var types = app.getActivatedAssets(ctx.tenantId); //ctx.rxtManager.listRxtTypeDetails();
        var typeDetails;
        var ratingApi = require('/modules/rating_api.js').api;
        var q = page.assetMeta.q;
        var query = buildRecentAssetQuery(q);
        for (var index in types) {
            typeDetails = ctx.rxtManager.getRxtTypeDetails(types[index]);
            type = typeDetails.shortName;
            if (ctx.isAnonContext) {
                am = asset.createAnonAssetManager(ctx.session, type, ctx.tenantId);
            } else {
                am = asset.createUserAssetManager(ctx.session, type);
            }
            if (query) {
                assets = am.recentAssets({
                    q: query
                });
            } else {
                assets = am.recentAssets();
            }
            if (assets.length > 0) {
                //Add subscription details if this is not an anon context
                if (!ctx.isAnonContext) {
                    addSubscriptionDetails(assets, am, ctx.session);
                }
                ratingApi.addRatings(assets, am, ctx.tenantId, ctx.username);
                items = items.concat(assets);
                assetsByType.push({
                    assets: assets,
                    rxt: typeDetails
                });
            }
        }
        page.recentAssets = items;
        page.recentAssetsByType = assetsByType;
    };
    var addSubscriptionDetails = function(assets, am, session) {
        for (var index = 0; index < assets.length; index++) {
            assets[index].isSubscribed = am.isSubscribed(assets[index].id, session);
        }
    };
    var buildRecentAssetQuery = function(q) {
        if (!q) {
            return null;
        }
        if (q === '') {
            return null;
        }
        var query = "{" + q + "}";
        var queryObj;
        try {
            queryObj = parse(query);
        } catch (e) {
            log.error('Unable to parse query string: ' + query + ' to an object.Exception: ' + e);
        }
        return queryObj;
    };
    pageDecorators.popularAssets = function(ctx, page) {
        var app = require('rxt').app;
        var asset = require('rxt').asset;
        var ratingApi = require('/modules/rating_api.js').api;
        var assets = {};
        var items = [];
        var assetsOfType;
        var am;
        var type;
        var types = app.getActivatedAssets(ctx.tenantId); //ctx.rxtManager.listRxtTypeDetails();
        for (var index in types) {
            type = types[index]; //typeDetails.shortName;
            if (ctx.isAnonContext) {
                am = asset.createAnonAssetManager(ctx.session, type, ctx.tenantId);
            } else {
                am = asset.createUserAssetManager(ctx.session, type);
            }
            assetsOfType = am.popularAssets();
            ratingApi.addRatings(assetsOfType, am, ctx.tenantId, ctx.username);
            items = items.concat(assetsOfType);
        }
        //log.info(items);
        page.popularAssets = items;
    };
    pageDecorators.tags = function(ctx, page) {
        var am = getAssetManager(ctx);
        page.tags = am.tags();
        return page;
    };
    pageDecorators.myAssets = function(ctx, page) {
        if ((!ctx.assetType) && (!ctx.isAnonContext)) {
            log.warn('Ignoring my assets decorator as the asset type was not present');
            return page;
        }
        var am = getAssetManager(ctx);
        page.myAssets = am.subscriptions(ctx.session) || [];
        return page;
    };
    pageDecorators.socialFeature = function(ctx, page) {
        var app = require('rxt').app;
        var constants = require('rxt').constants;
        if (!app.isFeatureEnabled(ctx.tenantId, constants.SOCIAL_FEATURE)) {
            log.warn('social feature has been disabled.');
            return page;
        }
        var socialFeatureDetails = app.getSocialFeatureDetails(ctx.tenantId);
        page.features[constants.SOCIAL_FEATURE] = socialFeatureDetails;
        return page;
    };
    var getAssetManager = function(ctx) {
        var asset = require('rxt').asset;
        var am;
        var tenantId = -1234;
        if (ctx.isAnonContext) {
            am = asset.createAnonAssetManager(ctx.session, ctx.assetType, tenantId);
        } else {
            am = asset.createUserAssetManager(ctx.session, ctx.assetType);
        }
        return am;
    };
    var fetchActiveAuthDetails = function(method, methods) {
        for (var key in methods) {
            if (key == method) {
                return methods[key];
            }
        }
        return null;
    };
}());