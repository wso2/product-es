var ui = {};
(function(ui, core) {
    var log = new Log();
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
            meta: {
                pageName: options.pageName,
                currentPage: options.currentPage,
                title: 'Empty',
                landingPage: options.landingPage
            }
        }
    };
    var processPageName = function(suffix) {
        var comps = suffix.split('/');
        return comps[0];
    };
    var getPageName = function(request) {
        var uriMatcher = new URIMatcher(request.getRequestURI());
        uriMatcher.match('/{context}/asts/{type}/{+suffix}');
        var options = uriMatcher.elements() || {};
        var page = {};
        page.currentPage = options.pageName;
        page.pageName = processPageName(options.suffix)
        return page;
    }
    ui.buildPage = function(session, request) {
        var server = require('store').server;
        // var userMod = require('store').user;
        var user = server.current(session);
        if (user) {
            return buildUserPage(session, request, user);
        } else {
            return buildAnonPage(session, request);
        }
        // var tenantId = user.tenantId;
        // var configs = userMod.configs(tenantId);
        // var landingPage = '';
        //Determine landing page details
        // if (configs) {
        //     if ((configs.application) && (configs.application.landingPage)) {
        //         landingPage = configs.application.landingPage;
        //     }
        // }
        //var pageDetails = getPageName(request);
        // var page = genericPage({
        //     username: user.username,
        //     pageName: pageDetails.pageName,
        //     currentPage: pageDetails.currentPage,
        //     landingPage: landingPage
        // });
        // return page;
    };
    var buildUserPage = function(session, request, user) {
        var userMod = require('store').user;
        var tenantId = user.tenantId;
        var configs = userMod.configs(tenantId);
        var tenantId = user.tenantId;
        var configs = userMod.configs(tenantId);
        var pageDetails = getPageName(request);
        var landingPage = getLandingPage(configs);
        var page = genericPage({
            username: user.username,
            pageName: pageDetails.pageName,
            isAnon: false,
            currentPage: pageDetails.currentPage,
            landingPage: landingPage
        });
        return page;
    };
    var buildAnonPage = function(session, request) {
        var userMod = require('store').user;
        var tenantId = getTenantIdFromUrl(request);
        var configs = userMod.configs(tenantId);
        var pageDetails = getPageName(request);
        var landingPage = getLandingPage(configs);
        var page = genericPage({
            username: null,
            pageName: pageDetails.pageName,
            isAnon: true,
            currentPage: pageDetails.currentPage,
            landingPage: landingPage
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
}(ui, core));