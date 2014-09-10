var ui = {};
(function(ui, core,asset) {
    var log = new Log();
    var DEFAULT_TITLE="Empty";
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
            assetMeta:{},
            security:{},
            meta: {
                pageName: options.pageName,
                currentPage: options.currentPage,
                title: options.title,
                landingPage: options.landingPage
            }
        }
    };
    var processPageName = function(suffix) {
        var comps = suffix.split('/');
        return comps[0];
    };
    var getPageName = function(request,session) {
        var uriMatcher = new URIMatcher(request.getRequestURI());
        uriMatcher.match('/{context}/asts/{type}/{+suffix}');
        var options = uriMatcher.elements() || {};
        var page = {};
        page.currentPage = options.pageName;
        page.pageName = processPageName(options.suffix);
        page.title=getPageTitle(session, options.type, page.pageName);
        return page;
    };
    var getPageTitle=function(session,type,pageName){
        var pages=asset.getAssetPageEndpoints(session,type);
        var page;
        for(var index=0;index<pages.length;index++){
            page=pages[index];
            if(page.url==pageName){
                return page.title;
            }
        }
        return DEFAULT_TITLE;
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
        var pageDetails = getPageName(request,session);
        var landingPage = getLandingPage(configs);
        var page = genericPage({
            username: user.username,
            pageName: pageDetails.pageName,
            isAnon: false,
            currentPage: pageDetails.currentPage,
            landingPage: landingPage,
            title:pageDetails.title
        });
        return page;
    };
    var buildAnonPage = function(session, request) {
        var userMod = require('store').user;
        var tenantId = getTenantIdFromUrl(request);
        var configs = userMod.configs(tenantId);
        var pageDetails = getPageName(request,session);
        var landingPage = getLandingPage(configs);
        var page = genericPage({
            username: null,
            pageName: pageDetails.pageName,
            isAnon: true,
            currentPage: pageDetails.currentPage,
            landingPage: landingPage,
            title:pageDetails.title
        });
        return page;
    };
    var getLandingPage = function(configs) {
        if (configs) {
            if ((configs.application) && (configs.application.landingPage)) {
                landingPage = configs.application.landingPage;
            }
        }
    };
    var getTenantIdFromUrl = function(request) {
        return -1234;
    };
}(ui, core,asset));