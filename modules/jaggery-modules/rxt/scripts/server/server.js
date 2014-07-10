var server = {};
(function(server, core) {
    var createContext = function(tenantId, session) {
        var user = require('store').user;
        var server = require('server').server;
        var sysRegistry=server.systemRegistry(session);
        var userManager=server.userManager(tenantId);
        var tenatOptions=server.configs(tenantId);
        var username = server.current(session).username;
        return {
            username: username,
            userManager: userManager,
            configs: tenatOptions,
            username: username,
            tenantId:tenantId,
            systemRegistry:sysRegistry
        };
    };
    server.init = function(options) {
        var event = require('event');
        event.on('login', function(tenantId, user, session) {
            var rxtManager = core.rxtManager(tenantId);
            var rxts = rxtManager.listRxtTypes();
            var assetResources;
            var rxt;
            var context;
            for (var index in rxts) {
                type = rxts[index];
                assetResources = core.assetResources(tenantId, type);
                context = createContext(tenantId,session);
                assetResources.server(context).onUserLogin();
            }
        });
    };
}(server, core))