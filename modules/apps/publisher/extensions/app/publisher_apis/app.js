app.server = function(ctx) {
    return {
        endpoints: {
            apis: [{
                url: 'assets',
                path: 'assets.jag',
                secured: true
            }, {
                url: 'lifecycles',
                path: 'lifecycles.jag',
                secured: true
            }, {
                url: 'authenticate',
                path: 'authenticate.jag'
            }, {
                url: 'logout',
                path: 'logout.jag'
            }]
        }
    };
};

app.apiHandlers = function(ctx) {
    return {
        onApiLoad: function() {
            log.info('Permission check on '+ctx.endpoint.url);
            if ((ctx.isAnonContext) && (ctx.endpoint.secured)) {
                log.info('Permission denied to access '+ctx.endpoint.url);
                //ctx.res.status='401';//sendRedirect(ctx.appContext+'/login');
                print('{ error:"Authentication error" }'); //TODO: Fix this to return a proper status code
                return false;
            }
            return true;
        }
    };
};