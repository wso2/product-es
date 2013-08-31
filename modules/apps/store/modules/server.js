var SERVER = 'server';

var SYSTEM_REGISTRY = 'system.registry';

var ANONYMOUS_REGISTRY = 'anonymous.registry';

var SERVER_USER_MANAGER = 'server.usermanager';

var SERVER_OPTIONS = 'server.options';

var SERVER_EVENTS = 'server.events';

var init = function (options) {
    var carbon = require('carbon'),
        server = new carbon.server.Server({
            tenanted: options.tenanted,
            url: options.server.https + '/admin'
        });
    application.put(SERVER, server);
    application.put(SERVER_OPTIONS, options);
};

var tenanted = function () {
    return options().tenanted;
};


/**
 * Fetches callback object of this module from the application context.
 */
var callbacks = function() {
    var cbs = application.get(SERVER_EVENTS);
    if(cbs) {
        return cbs;
    }
    cbs = {};
    application.put(SERVER_EVENTS, cbs);
    return cbs;
};

/**
 * Fetches specified event object from the application context.
 * @param event
 * @return {*|Array}
 */
var events = function(event) {
    var cbs = callbacks();
    return cbs[event] || (cbs[event] = []);
};

/**
 * Registers an event listener in the server.
 * @param event
 * @param fn
 * @return {*}
 */
var on = function (event, fn) {
    var group = events(event);
    group.push(fn);
    return fn;
};

/**
 * Removes specified event callback from the listeners.
 * If this is called without fn, then all events will be removed.
 * @param event
 * @param fn callback function used during the on() method
 */
var off = function (event, fn) {
    var index, cbs,
        group = events(event);
    if (fn) {
        index = group.indexOf(fn);
        group.splice(index, 1);
        return;
    }
    cbs = callbacks();
    delete cbs[event];
};

/**
 * Executes event callbacks of the specified event by passing data.
 * @param event
 * @param data
 */
var emit = function (event, data) {
    var group = events(event);
    group.forEach(function (fn) {
        fn(data);
    });
};

var options = function () {
    return application.get(SERVER_OPTIONS);
};

var systemRegistry = function (tenantId) {
    var carbon,
        key = tenantId + SYSTEM_REGISTRY,
        registry = application.get(key);
    if (registry) {
        return registry;
    }
    carbon = require('carbon');
    registry = new carbon.registry.Registry(server(), {
        system: true,
        tenantId: tenantId
    });
    application.put(key, registry);
    return registry;
};

var anonRegistry = function (tenantId) {
    var carbon,
        key = tenantId + ANONYMOUS_REGISTRY,
        registry = application.get(key);
    if (registry) {
        return registry;
    }
    carbon = require('carbon');
    registry = new carbon.registry.Registry(server(), {
        tenantId: tenantId
    });
    application.put(key, registry);
    return registry;
};

var server = function () {
    return application.get(SERVER);
};

var userManager = function (tenantId) {
    var um, carbon, key;
    if (!tenantId) {
        return application.get(SERVER_USER_MANAGER);
    }
    key = tenantId + SERVER_USER_MANAGER;
    um = application.get(key);
    if (um) {
        //return um;
    }
    carbon = require('carbon');
    um = new carbon.user.UserManager(server(), tenantId);
    application.put(key, um);
    return um;
};

