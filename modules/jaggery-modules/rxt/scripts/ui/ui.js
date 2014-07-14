var ui = {};
(function(ui, core) {
    var log=new Log();
    var genericPage = function(options) {
        return {
            rxt: {},
            cuser: options.username,
            assets: {},
            leftNav: [],
            ribbon: {},
            meta:{
                pageName:options.pageName,
                title:'Empty'
            }
        }
    };
    var getPageName=function(request){
        log.info('URI: '+request.getRequestURI());
        var uriMatcher=new URIMatcher(request.getRequestURI());
        uriMatcher.match('/{context}/asts/{type}/{pageName}');
        var options=uriMatcher.elements()||{};
        log.info('options: '+stringify(options));
        return options.pageName;
    }
    var isSingleAsset=function(){

    };
    ui.buildPage = function(session,request) {
        var server = require('store').server;
        var user = server.current(session);
        var pageName=getPageName(request);
        var page= genericPage({
        	username:user.username,
            pageName:pageName
        });
        log.info('Page: '+stringify(page));
        return page;
    };
}(ui, core));