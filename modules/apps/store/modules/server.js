var SERVER = 'server';

var SYSTEM_REGISTRY = 'system.registry';

var ANONYMOUS_REGISTRY = 'anonymous.registry';

var USER_MANAGER = 'user.manager';

var SERVER_OPTIONS = 'server.options';

var SERVER_EVENTS = 'server.events';

var TENANT_CONFIGS = 'tenant.configs';

/**
 * Initializes the server for the first time. This should be called when app is being deployed.
 * @param options
 */
var init = function (options) {
    var carbon = require('carbon'),
        event = require('/modules/event.js'),
        srv = new carbon.server.Server({
            tenanted: options.tenanted,
            url: options.server.https
        });
    application.put(SERVER, srv);
    application.put(SERVER_OPTIONS, options);

    application.put(TENANT_CONFIGS, {});

    event.on('tenantLoad', function (tenantId) {
        var carbon = require('carbon'),
            config = configs(tenantId);
        //log.info('tenantLoad : ' + tenantId);
        //loads tenant's system registry
        config[SYSTEM_REGISTRY] = new carbon.registry.Registry(server(), {
            system: true,
            tenantId: tenantId
        });

        //loads tenant's anon registry
        config[ANONYMOUS_REGISTRY] = new carbon.registry.Registry(server(), {
            tenantId: tenantId
        });

        //loads tenant's user manager
        config[USER_MANAGER] = new carbon.user.UserManager(server(), tenantId);
    });

    event.on('tenantUnload', function (tenantId) {
        var config = configs(tenantId);
        delete config[tenantId];
    });

    event.on('tenantCreate', function (tenantId) {
        var config = configs();
        //log.info('tenantCreate : ' + tenantId);
        config[tenantId] = {};
    });

    event.on('login', function (tenantId, user) {
        //we check the existence of user manager in the application ctx and
        //decide whether tenant has been already loaded.
        /*log.info('login : ' + tenantId + ' User : ' + JSON.stringify(user));
         if (application.get(tenantId + USER_MANAGER)) {
         //return;
         }
         event.emit('tenantCreate', tenantId);
         event.emit('tenantLoad', tenantId);*/
        var carbon = require('carbon'),
            server = require('/modules/server.js'),
            loginManager = carbon.server.osgiService('org.wso2.carbon.core.services.callback.LoginSubscriptionManagerService'),
            configReg = server.systemRegistry(tenantId).registry.getChrootedRegistry("/_system/config");
        loginManager.triggerEvent(configReg, user.username, tenantId);
    });
};

/**
 * Returns server options object.
 * @return {Object}
 */
var options = function () {
    return application.get(SERVER_OPTIONS);
};

/**
 * Returns the server instance.
 * @return {Object}
 */
var server = function () {
    return application.get(SERVER);
};

/**
 * Checks whether server runs on multi-tenanted mode.
 * @return {*}
 */
var tenanted = function () {
    return options().tenanted;
};

/**
 * Loads the tenant configs object or the tenant config of the given tenant.
 * @param tenantId
 * @return {*}
 */
var configs = function (tenantId) {
    var config = application.get(TENANT_CONFIGS);
    if (!tenantId) {
        return config;
    }
    return config[tenantId];
};

/**
 * Returns the system registry of the given tenant.
 * @param tenantId
 * @return {Object}
 */
var systemRegistry = function (tenantId) {
    var carbon = require('carbon');
    return configs(tenantId)[SYSTEM_REGISTRY] || new carbon.registry.Registry(server(), {
        system: true,
        tenantId: tenantId
    });
};

/**
 * Returns the anonymous registry of the given tenant.
 * @param tenantId
 * @return {Object}
 */
var anonRegistry = function (tenantId) {
    var carbon = require('carbon');
    return configs(tenantId)[ANONYMOUS_REGISTRY] || new carbon.registry.Registry(server(), {
        tenantId: tenantId
    });
};

/**
 * Returns the user manager of the given tenant.
 * @param tenantId
 * @return {*}
 */
var userManager = function (tenantId) {
    var carbon = require('carbon');
    return configs(tenantId)[USER_MANAGER] || new carbon.user.UserManager(server(), tenantId);
};
