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
var ui = {};
(function(ui, core, asset, app, constants) {
    var log = new Log('rxt.ui');
    var DEFAULT_TITLE = "Empty";
    var genericPage = function(options) {
        return {
            rxt: {},
            cuser: {
                username: options.username,
                isAnon: options.isAnon
            },
            assets: {},
            leftNav: [],
            ribbon: {},
            assetMeta: {},
            security: {},
            features:{},
            meta: {
                pageName: options.pageName,
                currentPage: options.currentPage,
                title: options.title,
                landingPage: options.landingPage
            }
        }
    };
    var processPageName = function(suffix) {
        suffix = suffix || '';
        var comps = suffix.split('/');
        return comps[0];
    };
    var getPageName = function(request, session) {
        var server = require('store').server;
        var user = server.current(session);
        var tenantId = constants.DEFAULT_TENANT;
        if (user) {
            tenantId = user.tenantId;
        }
        var uriMatcher = new URIMatcher(request.getRequestURI());
        uriMatcher.match(constants.ASSET_PAGE_URL_PATTERN);
        var options = uriMatcher.elements() || {};
        var pageDetails = {};
        //Asset url
        if (options.suffix) {
            pageDetails.currentPage = options.pageName;
            pageDetails.pageName = processPageName(options.suffix);
            pageDetails.title = getAssetPageTitle(session, options.type, pageDetails.pageName);
            return pageDetails;
        }
        //Check if it is a app page
        uriMatcher.match(constants.APP_PAGE_URL_PATTERN);
        options = uriMatcher.elements() || {};
        if (options.suffix) {
            pageDetails.currentPage = options.pageName;
            pageDetails.pageName = processPageName(options.suffix);
            pageDetails.title = getAppPageTitle(tenantId, pageDetails.pageName);
            return pageDetails;
        }
        return pageDetails;
    };
    var getAssetPageTitle = function(session, type, pageName) {
        var pages = asset.getAssetPageEndpoints(session, type);
        var page;
        for (var index = 0; index < pages.length; index++) {
            page = pages[index];
            if (page.url == pageName) {
                return page.title;
            }
        }
        return constants.DEFAULT_TITLE;
    };
    var getAppPageTitle = function(tenantId, pageName) {
        var page = app.getPageEndpoint(tenantId, pageName);
        if (!page) {
            log.warn('Unable to locate meta information for  page: '+pageName);
            return constants.DEFAULT_TITLE;
        }
        return page.title || constants.DEFAULT_TITLE;
    };
    ui.buildPage = function(session, request) {
        var server = require('store').server;
        // var userMod = require('store').user;
        var user = server.current(session);
        if (user) {
            return buildUserPage(session, request, user);
        } else {
            return buildAnonPage(session, request);
        }
    };
    var buildUserPage = function(session, request, user) {
        var userMod = require('store').user;
        var tenantId = user.tenantId;
        var configs = userMod.configs(tenantId);
        var tenantId = user.tenantId;
        var configs = userMod.configs(tenantId);
        var pageDetails = getPageName(request, session);
        var landingPage = getLandingPage(configs);
        var page = genericPage({
            username: user.username,
            pageName: pageDetails.pageName,
            isAnon: false,
            currentPage: pageDetails.currentPage,
            landingPage: landingPage,
            title: pageDetails.title
        });
        return page;
    };
    var buildAnonPage = function(session, request) {
        var userMod = require('store').user;
        var tenantId = getTenantIdFromUrl(request);
        var configs = userMod.configs(tenantId);
        var pageDetails = getPageName(request, session);
        var landingPage = getLandingPage(configs);
        var page = genericPage({
            username: null,
            pageName: pageDetails.pageName,
            isAnon: true,
            currentPage: pageDetails.currentPage,
            landingPage: landingPage,
            title: pageDetails.title
        });
        return page;
    };
    var getLandingPage = function(configs) {
        var landingPage='/';
        if (configs) {
            if ((configs.application) && (configs.application.landingPage)) {
                landingPage = configs.application.landingPage;
            }
        }

        return landingPage;
    };
    var getTenantIdFromUrl = function(request) {
        return constants.DEFAULT_TENANT;
    };
}(ui, core, asset, app, constants));