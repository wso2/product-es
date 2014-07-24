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
                title: 'Empty'
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
        var user = server.current(session);
        var pageDetails = getPageName(request);

        var page = genericPage({
            username: user.username,
            pageName: pageDetails.pageName,
            currentPage:pageDetails.currentPage
        });
        return page;
    };
}(ui, core));