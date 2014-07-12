var ui = {};
(function(ui, core) {
    var genericPage = function(user) {
        return {
            rxt: {},
            cuser: user,
            assets: {},
            leftNav: {},
            ribbon: {}
        }
    };
    ui.buildPage = function(session) {
        var server = require('store').server;
        var user = server.current(session);
        return genericPage({
        	username:user.username
        });
    };
}(ui, core));