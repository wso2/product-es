var USER = 'server.user';

var USER_REGISTRY = 'server.user.registry';

var USER_OPTIONS = 'server.user.options';

var USER_SPACE = 'server.user.space';

var USER_ROLE_PREFIX = 'private_';

/**
 * Initializes the user environment for the specified tenant. If it is already initialized, then will be skipped.
 */
var init = function () {
    var server = require('/modules/server.js');
    server.on('tenantCreate', function (tenantId) {
        var role, roles,
            um = server.userManager(tenantId);
        roles = options.roles;
        for (role in roles) {
            if (roles.hasOwnProperty(role)) {
                if (um.roleExists(role)) {
                    um.authorizeRole(role, roles[role]);
                } else {
                    um.addRole(role, [], roles[role]);
                }
            }
        }
        /*user = um.getUser(options.user.username);
         if (!user.hasRoles(options.userRoles)) {
         user.addRoles(options.userRoles);
         }*/
        //application.put(key, options);
    });

    server.on('tenantLoad', function(tenantId) {
        application.put(tenantId + USER_OPTIONS, require('/store.js').config())
    });
};

/**
 * @deprecated
 * @return {Object}
 */
var options = function (tenantId) {
    return application.get(tenantId + USER_OPTIONS);
};

/**
 * Logs in a user to the store. Username might contains the domain part in case of MT mode.
 * @param username ruchira or ruchira@ruchira.com
 * @param password
 * @return {boolean}
 */
var login = function (username, password) {
    var user, perm, perms, actions, i, length, um, opts,
        authorized = false,
        carbon = require('carbon'),
        server = require('/modules/server.js'),
        serv = server.server(),
        usr = carbon.server.tenantUser(username);
    if (!serv.authenticate(username, password)) {
        return false;
    }
    //TODO: using same config for each tenant ??
    init(usr.tenantId, require('/store.js').config());
    opts = options(usr.tenantId);
    um = server.userManager(usr.tenantId);
    user = um.getUser(usr.username);
    perms = opts.permissions.login;
    L1:
        for (perm in perms) {
            if (perms.hasOwnProperty(perm)) {
                actions = perms[perm];
                length = actions.length;
                for (i = 0; i < length; i++) {
                    if (user.isAuthorized(perm, actions[i])) {
                        authorized = true;
                        break L1;
                    }
                }
            }
        }
    if (!authorized) {
        return false;
    }
    onLogin(usr.tenantId, usr.username);
    session.put(USER, new carbon.user.User(um, usr.username));
    session.put(USER_REGISTRY, new carbon.registry.Registry(serv, {
        username: usr.username,
        tenantId: usr.tenantId
    }));
    session.put(USER_SPACE, userSpace(usr.username));
    //TODO: ??
    if (opts.login) {
        opts.login(user, password, session);
    }
    return true;
};

/**
 * Checks whether the logged in user has permission to the specified action.
 * @param permission
 * @param action
 * @return {*}
 */
var isAuthorized = function (permission, action) {
    var user = current(),
        server = require('/modules/server.js'),
        um = server.userManager(user.tenantId);
    return um.getUser(user.username).isAuthorized(permission, action);
};

/**
 * Returns the user's registry space. This should be called once with the username,
 * then can be called without the username.
 * @param username
 * @return {*}
 */
var userSpace = function (username) {
    try {
        return session.get(USER_SPACE) || options().userSpace.store + '/' + username;
    } catch (e) {
        return null;
    }
};

/**
 * Get the registry instance belongs to logged in user.
 * @return {*}
 */
var userRegistry = function () {
    try {
        return session.get(USER_REGISTRY);
    } catch (e) {
        return null;
    }
};

/**
 * Logs out the currently logged in user.
 */
var logout = function () {
    var opts = options(),
        user = current();
    if (opts.logout) {
        opts.logout(user, session);
    }
    session.remove(USER);
    session.remove(USER_SPACE);
    session.remove(USER_REGISTRY);
};

/**
 * Checks whether the specified username already exists.
 * @param username
 * @return {*}
 */
var userExists = function (username) {
    var server = require('/modules/server.js');
    return server.userManager().userExists(username);
};

var privateRole = function (username) {
    return USER_ROLE_PREFIX + username;
};

var register = function (username, password) {
    var user, role, id, perms, r, p,
        server = require('/modules/server.js'),
        um = server.userManager(),
        opts = options();
    um.addUser(username, password, opts.userRoles);
    user = um.getUser(username);
    role = privateRole(username);
    id = userSpace(username);
    perms = {};
    perms[id] = [
        'http://www.wso2.org/projects/registry/actions/get',
        'http://www.wso2.org/projects/registry/actions/add',
        'http://www.wso2.org/projects/registry/actions/delete',
        'authorize'
    ];
    p = opts.permissions.login;
    for (r in p) {
        if (p.hasOwnProperty(r)) {
            perms[r] = p[r];
        }
    }
    um.addRole(role, [], perms);
    user.addRoles([role]);
    if (opts.register) {
        opts.register(user, password, session);
    }
    login(username, password);
};

/**
 * Returns the currently logged in user
 */
var current = function () {
    try {
        return session.get(USER);
    } catch (e) {
        return null;
    }
};

var loginWithSAML = function (username) {
    var user, perm, perms, actions, i, length,
        authorized = false,
        opts = options(),
        carbon = require('carbon'),
        server = require('/modules/server.js'),
        serv = server.server(),
        um = server.userManager();

    user = um.getUser(username);
    perms = opts.permissions.login;
    L1:
        for (perm in perms) {
            if (perms.hasOwnProperty(perm)) {
                actions = perms[perm];
                length = actions.length;
                for (i = 0; i < length; i++) {
                    if (user.isAuthorized(perm, actions[i])) {
                        authorized = true;
                        break L1;
                    }
                }
            }
        }
    if (!authorized) {
        return false;
    }
    session.put(USER, new carbon.user.User(um, username));
    session.put(USER_REGISTRY, new carbon.registry.Registry(serv, {
        username: username,
        tenantId: carbon.server.tenantId()
    }));
    session.put(USER_SPACE, userSpace(username));
    if (opts.login) {
        opts.login(user, "", session);
    }

    var permission = {};
    permission[userSpace(username)] = [
        carbon.registry.actions.GET,
        carbon.registry.actions.PUT,
        carbon.registry.actions.DELETE
    ];
    um.authorizeRole(privateRole(username), permission);

    return true;
};

//TODO: move this into a separate module
var onLogin = function (tenantId, username) {
    var carbon = require('carbon'),
        server = require('/modules/server.js'),
        loginManager = carbon.server.osgiService('org.wso2.carbon.core.services.callback.LoginSubscriptionManagerService'),
        configReg = server.systemRegistry(tenantId).registry.getChrootedRegistry("/_system/config");
    loginManager.triggerEvent(configReg, username, tenantId);
};