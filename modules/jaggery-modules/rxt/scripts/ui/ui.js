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
                title: 'Empty'
            }
        }
    };
    var getPageName = function(request) {
        var uriMatcher = new URIMatcher(request.getRequestURI());
        uriMatcher.match('/{context}/asts/{type}/{pageName}');
        var options = uriMatcher.elements() || {};
        return options.pageName;
    }
    var isSingleAsset = function() {};
    ui.buildPage = function(session, request) {
        var server = require('store').server;
        var user = server.current(session);
        var pageName = getPageName(request);
        
        var page = genericPage({
            username: user.username,
            pageName: pageName
        });
        return page;
    };
}(ui, core));