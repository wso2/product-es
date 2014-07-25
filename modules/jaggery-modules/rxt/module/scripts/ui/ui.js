var ui = {};
(function(ui, core) {
    var log = new Log();
    var genericPage = function(options) {
        return {
            rxt: {},
            cuser: {
                username: options.username
            },
            assets: {},
            leftNav: [],
            ribbon: {},
            meta: {
                pageName: options.pageName,
                currentPage:options.currentPage,
                title: 'Empty',
                landingPage:options.landingPage
            }
        }
    };
    var processPageName=function(suffix){
        var comps=suffix.split('/');
        return comps[0];
    };
    var getPageName = function(request) {
        var uriMatcher = new URIMatcher(request.getRequestURI());
        uriMatcher.match('/{context}/asts/{type}/{+suffix}');
        var options = uriMatcher.elements() || {};
        var page={};
        page.currentPage=options.pageName;
        page.pageName=processPageName(options.suffix)
        return page;
    }
    ui.buildPage = function(session, request) {
        var server = require('store').server;
        var userMod=require('store').user;
        var user = server.current(session);
        var tenantId=user.tenantId;
        var configs=userMod.configs(tenantId);
        var landingPage='';

        //Determine landing page details
        if(configs){
            log.warn('Unable to locate tnenat configurations');
            if((configs.application)&&(configs.application.landingPage)){
                landingPage=configs.application.landingPage;
            }
        }
        var pageDetails = getPageName(request);

        var page = genericPage({
            username: user.username,
            pageName: pageDetails.pageName,
            currentPage:pageDetails.currentPage,
            landingPage:landingPage
        });
        return page;
    };
}(ui, core));