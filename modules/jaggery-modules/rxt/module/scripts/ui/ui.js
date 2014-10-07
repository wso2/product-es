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
 * The ui namespace cobtains methods which allow a generic page object to be created.
 * @namespace
 * @example 
 *     var ui = require('rxt').ui;
 *     var page = ui.buildPage(session,request);
 * @requires store
 */
var ui = {};
(function(ui, core, asset, app, constants) {
    var log = new Log('rxt.ui');
    var DEFAULT_TITLE = "Empty";
    /**
     * Returns a generic page object
     * @param  {Object} options The set of dynamic values a page object can start off with.
     * @return {Object}         A page object
     */
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
            features: {},
            meta: {
                pageName: options.pageName,
                currentPage: options.currentPage,
                title: options.title,
                landingPage: options.landingPage
            }
        };
    };
    /**
     * Returns the page name by passing the provided url pattern.The page is assumed to
     * be the first element when breaking the url by /
     * @param  {String} url A url pattern
     * @return {String}     The name of the page
     */
    var processPageName = function(url) {
        var url = url || '';
        var comps = url.split('/');
        return comps[0];
    };
    /**
     * Returns the page name for which the provided request was made
     * @param  {Object} request Jaggery request object
     * @param  {Object} session Jaggery session object
     * @return {String}         The name of the page
     */
    var getPageName = function(request, session) {
        var server = require('store').server;
        var user = server.current(session);
        var tenantId = constants.DEFAULT_TENANT; //Assume the there is no logged in user
        if (user) {
            tenantId = user.tenantId;
        }
        var uriMatcher = new URIMatcher(request.getRequestURI());
        uriMatcher.match(constants.ASSET_PAGE_URL_PATTERN);
        var options = uriMatcher.elements() || {};
        var pageDetails = {};
        //Check if it is an asset URL
        if (options.suffix) {
            pageDetails.currentPage = options.pageName;
            pageDetails.pageName = processPageName(options.suffix);
            pageDetails.title = getAssetPageTitle(session, options.type, pageDetails.pageName);
            return pageDetails;
        }
        //Check if it is an application extension URL
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
    /**
     * Returns the title of a given asset page
     * @param  {Object} session  Jaggery session object
     * @param  {String} type      The asset type
     * @param  {String} pageName  The name of the page
     * @return {String}           The title of the provided page
     */
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
    /**
     * Returns the title of application page.This method will internally call the the getPageEndpoint method
     * to obtain details about the endpoint
     * @param  {Number} tenantId The tenant ID for which the page title must be returned
     * @param  {String} pageName The name of the page for which the  page title should be returned
     * @return {String}          If the page information is found the title is returned,else a default title 
     */
    var getAppPageTitle = function(tenantId, pageName) {
        var page = app.getPageEndpoint(tenantId, pageName);
        if (!page) {
            log.warn('Unable to locate meta information for  page: ' + pageName);
            return constants.DEFAULT_TITLE;
        }
        return page.title || constants.DEFAULT_TITLE;
    };
    /**
     * Returns a page object which can either be a user page or an annoymous page.The method will
     * use the session to determine if there is a logged in user.If  a logged in user is located 
     * then the page is constructed based on the user context,else an annoymous context based 
     * on the super tenant is used.
     * @param  {Object} session Jaggery session object
     * @param  {Object} request Jaggery request object
     * @return {Object}         A page object
     */
    ui.buildPage = function(session, request) {
        var server = require('store').server;
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
        var landingPage = app.getLandingPage(tenantId);
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
        var landingPage = app.getLandingPage(tenantId);
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
    var getTenantIdFromUrl = function(request) {
        return constants.DEFAULT_TENANT;
    };
}(ui, core, asset, app, constants));