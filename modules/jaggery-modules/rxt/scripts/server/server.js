var server = {};
(function(server, core) {
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
                context = core.createAssetContext(tenantId, session);
                assetResources.server(context).onUserLogin();
            }
        });
    };
}(server, core))