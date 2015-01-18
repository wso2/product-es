/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
/**
 * The app namespace provides a set of utility methods to obtain application meta data related to the
 * extension model.
 * @namespace
 * @example
 *     var app =  require('rxt').app;
 *     app.init('/publisher');
 * @requires store
 * @requires event
 * @requires utils
 */
var app = {};
(function(app, core, artifacts) {
    var log = new Log('app-core');
    /**
     * Represents a set of endpoints
     * @class
     * @constructor
     */
    function Endpoints() {
        this.endpoints = [];
    }
    /**
     * Returns an endpoint given its url signature
     * @param  {string} url The url signature to be used in the search for endpoints
     * @return {Object}     If a matching endpoint is found then an object is returned containing the path,title,owner and secured property,
     *                      else null.
     */
    Endpoints.prototype.getEndpointByUrl = function(url) {
        for (var index in this.endpoints) {
            if (this.endpoints[index].url == url) {
                //If the secured property is not provided then the page is not considered to be secured
                this.endpoints[index].secured = this.endpoints[index].secured ? this.endpoints[index].secured : false;
                return this.endpoints[index];
            }
        }
        return null;
    };
    /**
     * Returns the list of all endpoints managed by this object
     * @return {Array} An array of endpoint objects containing the path,title,owner and secured properties
     */
    Endpoints.prototype.list = function() {
        return this.endpoints;
    };
    /**
     * Adds an endpoint to the list of endpoints managed by the object.If the endpoint is already managed by the object
     * then the values are overriden by the provided endpoint object.
     * @param {Object} endpoint An endpoint object containing a url signature,path to the controller, title (optional) and secured(optional)
     */
    Endpoints.prototype.add = function(endpoint) {
        //First check if the endpoint already exists
        var existingEndpoint = this.getEndpointByUrl(endpoint.url);
        if (existingEndpoint) {
            existingEndpoint.path = endpoint.path;
            existingEndpoint.title = endpoint.title;
            existingEndpoint.owner = endpoint.owner;
            existingEndpoint.secured = endpoint.secured ? endpoint.secured : false;
            return;
        }
        this.endpoints.push(endpoint);
    };
    /**
     * Adds a set of endpoints to be managed.Internally this method will call the add method for each of the endpoints in the array
     * @todo This method needs to be combined with the add method
     * @param {Array} endpoints An array of endpoint objects
     */
    Endpoints.prototype.addMultiple = function(endpoints) {
        for (var index in endpoints) {
            this.add(endpoints[index]);
            log.info('Registered endpoint: /' + endpoints[index].url + ' (secured: ' + (endpoints[index].secured || false) + ')');
        }
    };
    /**
     * Represents the an extensible application
     * @class
     *@constructor
     */
    function App() {
        this.pages = new Endpoints();
        this.apis = new Endpoints();
    }
    /**
     * Returns all of the pages exposed by the extensible application
     * @return {Array} An array of endpoint objects containing the url,path,secured and title properties
     */
    App.prototype.getPageEndpoints = function() {
        return this.pages.list();
    };
    /**
     * Returns all of the apis exposed by the extensible application
     * @return {Array} [description]
     */
    App.prototype.getApiEndpoints = function() {
        return this.apis.list();
    };
    App.prototype.getPageEndpoint = function(url) {
        return this.pages.getEndpointByUrl(url);
    };
    App.prototype.getApiEndpoint = function(url) {
        return this.apis.getEndpointByUrl(url);
    };
    App.prototype.addPageEndpoint = function(endpoint) {
        this.pages.add(endpoint);
    };
    App.prototype.addApiEndpoint = function(endpoint) {
        this.apis.add(endpoint);
    };
    App.prototype.addPageEndpoints = function(endpoints) {
        this.pages.addMultiple(endpoints);
    };
    App.prototype.addApiEndpoints = function(endpoints) {
        this.apis.addMultiple(endpoints);
    };
    /**
     * Represents the functionality of  an extensible application
     * @class
     * @constructor
     * @param {Object} appResources  The extensible application resources
     * @param {Object} ctx           The context in which the application manager is evaluated
     */
    function AppManager(appResources, ctx) {
        this.ctx = ctx;
        this.appResources = appResources;
    }
    /**
     * Returns a page url qualified by the asset extension
     * @param  {String} type     The type of asset
     * @param  {String} endpoint The endpoint name
     * @return {String}          An extension qualified path
     */
    AppManager.prototype.buildAssetPageUrl = function(type, endpoint) {
        return core.getAssetPageUrl(type, endpoint);
    };
    AppManager.prototype.buildAssetApiUrl = function(type, endpoint) {
        return core.getAssetApiUrl(type, endpoint);
    };
    AppManager.prototype.buildAppPageUrl = function(endpoint) {
        return core.getAppPageUrl(endpoint);
    };
    AppManager.prototype.buildAppApiUrl = function() {
        return core.getAppApiUrl(endpoint);
    };
    /**
     * Returns a UI page object after calling the page decorators for a given application extension.The
     * method will determine form which the application extension it is exposed.AFter which the page decorators
     * for that extension will be applied to the page object
     * @param  {Array} assets [description] //TODO: Fill description !
     * @param  {Object} page   A page object
     * @return Object A page object
     */
    AppManager.prototype.render = function(assets, page) {
        page.assets = assets;
        var pageName = page.meta ? page.meta.pageName : null;
        if (!pageName) {
            log.warn('Unable to locate the page name of the current resource.As a result the app manager cannot determine the extension from which the renderer should be loaded');
            return page;
        }
        //Deteremine the extension to which this page belongs
        var pageDetails = app.getPageEndpoint(this.ctx.tenantId, pageName);
        if (!pageDetails) {
            log.warn('Unable to locate the page details for the current resource.As a result the app manager cannot determine the extension from which the renderer should be loaded');
            return page;
        }
        //Locate the extension
        var renderer = getRenderer(this.appResources, pageDetails.owner);
        if (!renderer) {
            log.warn('Unable to locate a renderer for the extension : ' + pageDetails.owner);
            return page;
        }
        renderer = renderer(this.ctx);
        //Check if the renderer has any page decorators
        var decorators = renderer.pageDecorators;
        if (!decorators) {
            log.warn('There are no page decorators for page: ' + page.pageName);
            return page;
        }
        for (var key in decorators) {
            try {
                page = decorators[key].call(this, page) || page;
            } catch (e) {
                log.error('Failed to execute decorator: ' + key + ' for the page: ' + page + '.Exception ' + e);
            }
        }
        return page;
    };
    var getAppExtensionBasePath = function() {
        return '/extensions/app';
    };
    var getAppExtensionFileName = function() {
        return 'app.js';
    };
    var getCurrentAppExtensionFileName = function(currentDir) {
        return getAppExtensionBasePath() + '/' + currentDir.getName() + '/' + getAppExtensionFileName();
    };
    var load = function(tenantId) {
        var dir = new File(getAppExtensionBasePath());
        //Check if it is a directory and the path exists
        if (!dir.isExists()) {
            log.warn('The app extension directory ' + getAppExtensionBasePath() + ' does not exist');
            throw 'The app extension directory ' + getAppExtensionBasePath() + ' does not exist';
        }
        if (!dir.isDirectory()) {
            log.warn('The app extension path ' + getAppExtensionBasePath() + ' does not point to a directory');
            throw 'The app extension path ' + getAppExtensionBasePath() + ' does not point to a directory';
        }
        loadAppExtensions(dir, tenantId);
    };
    var loadAppExtensions = function(rootDir, tenantId) {
        //Get all of the sub directories and find the app.js file
        var files = rootDir.listFiles();
        var appExtensionName;
        if (files.length == 0) {
            log.warn('Unable to locate the app.js for directory: ' + rootDir.getName());
            return;
        }
        var appResources = {};
        for (var index in files) {
            appExtensionName = files[index].getName();
            //log.info('Located app extension ' + appExtensionName);
            var appExtensionFilePath = getCurrentAppExtensionFileName(files[index]);
            evalAppScript(appExtensionName, appExtensionFilePath, appResources);
        }
        var app = processAppExtensions(appResources, tenantId);
        //Save the app object and appResources 
        var configs = core.configs(tenantId);
        configs.appResources = appResources;
        configs.appConfig = app;
    };
    var setExtensionName = function(items, extName) {
        for (var index in items) {
            items[index].owner = extName;
        }
    };
    var getAppExtensionPath = function(extName) {
        return getAppExtensionBasePath() + '/' + extName;
    };
    var loadAppExtensionArtifacts = function(extName) {
        var path = getAppExtensionPath(extName);
        artifacts.loadDirectory(path, -1234); //TODO:We only support loading artifacts for super tenant
    };
    var loadServerConfigs = function(tenantId, serverConfigs,serverCb) {
        var userMod = require('store').user; //Obtain the configurations for the tenant
        var configs = userMod.configs(tenantId)||{};
        var landingPage = configs.landingPage || '/';
        var disabledAssets = configs.disabledAssets || [];
        if (serverConfigs.landingPage) {
            configs.application.landingPage = serverConfigs.landingPage;
            log.info('Landing page changed to : ' + configs.application.landingPage);
        }
        if ((serverConfigs.disabledAssets) && (serverConfigs.disabledAssets.length > 0)) {
            //Overriding extensions will completely replace the disabled assets list, this is required
            //in order to allow app extensions to enable types that are already disabled by a previous extensions
            configs.disabledAssets = serverConfigs.disabledAssets; 
            //configs.disabledAssets = disabledAssets.concat(serverConfigs.disabledAssets);
            log.info('Disabled assets: ' + stringify(configs.disabledAssets));
        }
        //Invoke the server configs loaded
        if(serverCb.onLoadedServerConfigs){
            serverCb.onLoadedServerConfigs(configs);
        }
    };
    var processExtension = function(extName, map, app, tenantId) {
        if (!map[extName]) {
            log.warn('The app extension ' + extName + ' does not exist.Aborting loading of extensions');
            throw 'The app extension ' + extName + ' does not exist.Aborting loading of extensions';
        }
        if (map[extName].loaded) {
            log.warn('The extension ' + extName + 'has already been loaded');
            return;
        }
        //Load all of the endpoints
        var serverCb = map[extName].server ? map[extName].server : null;
        if (!serverCb) {
            log.warn('The app extension ' + extName + ' does not have a server callback');
            return;
        }
        var serverCbResult = serverCb();
        var endpoints = serverCbResult.endpoints||{};
        // if (!endpoints) {
        //     log.warn('The app extension ' + extName + ' has not defined any endpoints.');
        //     return;
        // }
        var apiEndpoints = endpoints.apis || [];
        var pageEndpoints = endpoints.pages || [];
        setExtensionName(apiEndpoints, extName);
        setExtensionName(pageEndpoints, extName);
        app.addApiEndpoints(apiEndpoints);
        app.addPageEndpoints(pageEndpoints);
        map[extName].loaded = true;
        //Load artifacts
        loadAppExtensionArtifacts(extName);
        //Load the server configurations
        loadServerConfigs(tenantId, serverCbResult.configs || {},serverCbResult);
        log.info('Finished processing app extension: ' + extName);
    };
    var processAppExtensions = function(appExtensions, tenantId) {
        var appExtension;
        var extensionsCb;
        var stack;
        var endpoints;
        var app = new App();
        log.info('Starting to process the app extensions');
        for (var key in appExtensions) {
            log.info('Processing app extension: ' + key);
            //Look for the dependencies
            appExtension = appExtensions[key];
            if (!appExtension.dependencies) {
                //Get all of the endpoints
                processExtension(key, appExtensions, app,tenantId);
            } else {
                //Determine the load order
                stack = [];
                stack = recursiveProcess(key, appExtensions, stack);
                log.info('Loading dependencies: ' + stack);
                for (var index in stack) {
                    processExtension(stack[index], appExtensions, app, tenantId);
                }
            }
        }
        log.info('Finished processing the app extensions');
        return app;
    };
    var recursiveProcess = function(extName, map, stack) {
        if (!map[extName]) {
            log.warn('Extension: ' + extName + ' does not exist');
            return stack;
        }
        var dependencies = map[extName].dependencies;
        if (!dependencies) {
            stack.push(extName);
            return stack;
        } else {
            for (var index in dependencies) {
                var temp = recursiveProcess(dependencies[index], map, stack);
            }
            stack.push(extName);
        }
        return stack;
    };
    var getScriptContent = function(scriptFile, appExtensionFilePath) {
        var content = null;
        try {
            scriptFile.open('r');
            content = scriptFile.readAll();
        } catch (e) {
            log.warn('Unable to read the contents of the ' + appExtensionFilePath);
            return content;
        } finally {
            scriptFile.close();
        }
        return content;
    };
    var evalScriptContent = function(scriptModule, path) {
        var ptr = null;
        try {
            ptr = eval(scriptModule);
        } catch (e) {
            log.warn('Unable to evaluate script ' + path + ' as it has syntax errors');
        }
        return ptr;
    };
    var execScriptContent = function(modulePtr, path, app, appLog) {
        try {
            modulePtr.call(this, app, appLog);
        } catch (e) {
            log.warn('Unable to execute the script content at ' + path + ' due to: ' + e);
            app = null;
        }
        return app;
    };
    var evalAppScript = function(appExtensionName, appExtensionFilePath, extensionMap) {
        var appExtensionFile = new File(appExtensionFilePath);
        if (!appExtensionFile.isExists()) {
            log.warn('The app extension file: ' + appExtensionFilePath + ' does not exist.The extension will not be loaded');
            return;
        }
        var app = {};
        app.extension = null;
        app.server = null;
        app.extensionName = appExtensionName;
        app.process = false;
        app.ignoreExtension = false;
        app.renderer = null;
        app.pageHandlers = null;
        app.apiHandlers = null;
        var content = getScriptContent(appExtensionFile, appExtensionFilePath);
        if (!content) {
            log.warn('The app extension file: ' + appExtensionFilePath + ' does not contain any content.The extension will not be loaded.');
            return;
        }
        var module = 'function(app,log){' + content + '}';
        var modulePtr = evalScriptContent(module, appExtensionFilePath);
        if (!modulePtr) {
            log.warn('The app extension file: ' + appExtensionFilePath + ' has syntax errors.The extension ' + appExtensionName + ' will not be loaded.');
            return;
        }
        var appLog = new Log(appExtensionName);
        execScriptContent(modulePtr, appExtensionFilePath, app, appLog);
        if (!app) {
            log.warn('Unable to load app extension ' + appExtensionName + ' as evaluation exceptions have occured when loading app.js');
            return;
        }
        if (app.ignoreExtension) {
            log.warn('Successfully loaded app extension: ' + appExtensionName + ' but ignoring extension due to ignoreExtension property been true.');
            return;
        }
        extensionMap[appExtensionName] = app;
        log.info('Successfully loaded app extension: ' + appExtensionName);
    };
    var init = function(tenantId, context) {
        application.put(constants.APP_CONTEXT, context)
        load(tenantId);
    };
    var getPage = function(uri) {
        var comps = uri.split('/');
        return comps.length > 0 ? comps[0] : null;
    };
    var getRenderer = function(appResources, extensionName) {
        if (!appResources[extensionName]) {
            log.warn('Unable to load extension details for ' + extensionName + '.A renderer could not be created');
            return null;
        }
        var renderer = appResources[extensionName].renderer;
        if (!renderer) {
            log.warn('A renderer has not been defined in the app.js file.');
            return null;
        }
        return renderer;
    };
    /**
     * Initializes the app setup logic
     * @param  {String} context The context of the application
     */
    app.init = function(context) {
        var event = require('event');
        event.on('tenantLoad', function(tenantId) {
            init(tenantId, context);
        });
    };
    /**
     * Returns the context of the application
     * @return {String} The context of the application (e.g. publisher or store)
     */
    app.getContext = function() {
        var context = application.get(constants.APP_CONTEXT);
        return context;
    };
    /**
     * Returns an object containing all of the app level extension scripts
     * @param  {Number} tenantId  The tenant id  for which the application resources must be obtained
     * @return {Object} An object containing all of the app extension callbacks
     * @throws Unable to locate configuration of tenant.Cannot locate api endpoint
     * @throws The app configuration details could not be loaded for tenant
     */
    app.getAppResources = function(tenantId) {
        var configs = core.configs(tenantId);
        if (!configs) {
            log.warn('Unable to locate configuration of tenant ' + tenantId + '.Cannot locate api endpoint');
            throw 'Unable to locate configuration of tenant ' + tenantId + '.Cannot locate api endpoint';
        }
        var appResources = configs.appResources;
        if (!appResources) {
            log.warn('The app configuration details could not be loaded for tenant: ' + tenantId);
            throw 'The app configuration details could not be loaded for tenant: ' + tenantId;
        }
        return appResources;
    };
    /**
     * Returns an application manager object that has user specific details.This method will check if there is a logged in
     * user and obtain the tenantId from the user.If a user is not found then the tenantId will be set to super tenant
     * @param  {Object} session A Jaggery session
     * @return {Object}         An instance of AppManager with a user context
     */
    app.createUserAppManager = function(session) {
        var server = require('store').server;
        var user = server.current(session);
        //Assume that there is no logged in user
        var tenantId = -1234; //TODO: Replace this with constants.DEFAULT_TENANT
        if (user) {
            tenantId = user.tenantId;
        }
        var appResources = this.getAppResources(tenantId);
        var ctx = core.createAppContext(session);
        return new AppManager(appResources, ctx);
    };
    /**
     * Returns an application manager object that can be used in pages and endpoints that are
     * publically visible
     * @param  {Object} session  A Jaggery session
     * @param  {Number} tenantId The tenant ID of tenant for which the annoymous application manager should be created
     * @return {Object}          An instance of the AppManager with a annymous context
     */
    app.createAnonAppManager = function(session, tenantId) {
        var server = require('store').server;
        var user = server.current(session); //TODO: Replace this as it is not required
        //TODO: Check if the tenant ID is provided
        var appResources = this.getAppResources(tenantId);
        var ctx = core.createAppContext(session);
        return new AppManager(appResources, ctx);
    };
    /**
     * Returns an array of all asset types that are activated in the ES.This method first obtains the set of
     * all available asset types and then intersects them with assets which are disabled in the
     * x-tenant.json file.
     * @param  {Number} tenantId The tenant ID for which the activated assets must be returned
     * @return {Array}          An array of strings indicating the asset types
     *                          (The values returned reflect the shortName property in the rxt files)
     * @throws Unable to locate tenant configurations in order to retrieve activated assets
     */
    app.getActivatedAssets = function(tenantId) {
        var user = require('store').user;
        var configs = user.configs(tenantId); //TODO: Check if a tenant ID is provided
        if (!configs) {
            log.warn('Unable to locate tenant configurations in order to retrieve activated assets');
            throw 'Unable to locate tenant configurations in order to retrieve activated assets';
        }
        //var assets = configs.assets;
        var disabledAssets = configs.disabledAssets || [];
        var rxtManager = core.rxtManager(tenantId);
        var availableAssets = rxtManager.listRxtTypes();
        var enabledAssets = availableAssets;
        if (disabledAssets.length > 0) {
            //The enabled assets are an intersection of the disabledAssets with the available assets
            enabledAssets = availableAssets.filter(function(item) {
                return (disabledAssets.indexOf(item) < 0);
            });
        }
        return enabledAssets;
    };
    /**
     * Returns the landing page for the application for a given tenant.The method will check the application.landingPage property in the
     * tenant-x.json file.If no landing page is defined the root will be the landing page
     * @param  {Number} tenantId  The tenant ID for which the landing page must be returned
     * @return {String}          The landing page url
     */
    app.getLandingPage = function(tenantId) {
        var landingPage = '/'; //Assume the landing page property has not been defined 
        var userMod = require('store').user; //Obtain the configurations for the tenant
        var configs = userMod.configs(tenantId);
        if (!configs) {
            log.warn('Unable to locate landing page of tenant: ' + tenantId);
            return landingPage;
        }
        if ((configs.application) && (configs.application.landingPage)) {
            landingPage = configs.application.landingPage;
        }
        return landingPage;
    };
    /**
     * Executes the provided page handler.If the page cannot be located or if the handler cannot be located then
     * the method will return True.
     * @param  {String} handler  The name of the handler to be executed
     * @param  {Object} req      Jaggery request object
     * @param  {Object} res      Jaggery response object
     * @param  {Object} session  Jaggery session object
     * @param  {Object} pageName The page on which the handler should be invoked
     * @return {Boolean}          True if the handler is executed without an error , False if the handler has failed to execute
     */
    app.execPageHandlers = function(handler, req, res, session, pageName) {
        var ctx = core.createAppContext(session);
        var appResources = app.getAppResources(ctx.tenantId);
        //Determine the extension from the pageName
        var endpoint = app.getPageEndpoint(ctx.tenantId, pageName);
        if (!endpoint) {
            log.warn('Unable to obtain endpoint information for page: ' + pageName);
            return true;
        }
        var extensionResource = appResources[endpoint.owner];
        if (!extensionResource) {
            log.warn('Unable to retrieve extension resources for ' + endpoint.owner);
            return true;
        }
        var pageHandlers = extensionResource.pageHandlers;
        if (!pageHandlers) {
            log.warn('There are no pageHandlers defined for tenant ' + ctx.tenanId);
            return true;
        }
        ctx.req = req;
        ctx.res = res;
        ctx.endpoint = endpoint;
        ctx.appContext = app.getContext();
        pageHandlers = pageHandlers(ctx);
        if (!pageHandlers[handler]) {
            log.warn('Unable to locate page handler: ' + handler);
            return true;
        }
        return pageHandlers[handler]();
    };
    /**
     * Executes the provided api endpoint handler.If the api endpoint cannot be located or if the handler cannot be located then
     * the method will return True.
     * @param  {String} handler  The name of the handler to be executed
     * @param  {Object} req      Jaggery request object
     * @param  {Object} res      Jaggery response object
     * @param  {Object} session  Jaggery session object
     * @param  {String} pageName The page on which the handler should be invoked
     * @return {Boolean}          True if the handler is executed without an error , False if the handler has failed to execute
     */
    app.execApiHandlers = function(handler, req, res, session, pageName) {
        var ctx = core.createAppContext(session);
        var appResources = app.getAppResources(ctx.tenantId);
        //Determine the extension from the pageName
        var endpoint = app.getApiEndpoint(ctx.tenantId, pageName);
        if (!endpoint) {
            log.warn('Unable to obtain endpoint information for api: ' + pageName);
            return true;
        }
        var extensionResource = appResources[endpoint.owner];
        if (!extensionResource) {
            log.warn('Unable to retrieve extension resources for ' + endpoint.owner);
            return true;
        }
        var apiHandlers = extensionResource.apiHandlers;
        if (!apiHandlers) {
            log.warn('There are no apiHandlers defined for tenant ' + tenanId);
            return true;
        }
        ctx.req = req;
        ctx.res = res;
        ctx.endpoint = endpoint;
        ctx.appContext = app.getContext();
        ctx.session = session;
        apiHandlers = apiHandlers(ctx);
        if (!apiHandlers[handler]) {
            log.warn('Unable to locate page handler: ' + handler);
            return true;
        }
        return apiHandlers[handler]();
    };
    /**
     * @todo: Remove this method
     */
    app.force = function() {
        init(-1234);
    }
    /**
     * Returns details about a page endpoint registered for the extensible application
     * @param  {Number} tenantId The tenant ID for which the page endpoint must be returned
     * @param  {Number} url      The url signature of the endpoint
     * @return {Object}          An endpoint object containing url,path and secured
     * @throws Unable to locate configuration of tenant.Cannot locate page endpoint
     * @throws The app configuration details could not be loaded for tenant
     */
    app.getPageEndpoint = function(tenantId, url) {
        var configs = core.configs(tenantId);
        if (!configs) {
            log.warn('Unable to locate configuration of tenant ' + tenantId + '.Cannot locate page endpoint');
            throw 'Unable to locate configuration of tenant ' + tenantId + '.Cannot locate page endpoint';
        }
        var appConfig = configs.appConfig;
        if (!appConfig) {
            log.warn('The app configuration details could not be loaded for tenant: ' + tenantId);
            throw 'The app configuration details could not be loaded for tenant: ' + tenantId;
        }
        return appConfig.getPageEndpoint(url);
    };
    /**
     * Returns details on the set of page endpoints registered for the extensible application
     * @param  {Number} tenantId The tenant ID for which the page endpoints should be returned
     * @return {Array}           An array of endpoint objects
     */
    app.getPageEndpoints = function(tenantId) {
        var configs = core.configs(tenantId);
        if (!configs) {
            log.warn('Unable to locate the tenant configuration');
            throw 'Unable to locate tenant configuration';
        }
        var appConfig = configs.appConfig;
        if (!appConfig) {
            log.warn('The app configuration details could not be loaded for tenant: ' + tenantId);
            throw 'Unable to locate app configuration';
        }
        return appConfig.getPageEndpoints();
    };
    /**
     * Returns the path to the controller handling a page endpoint.The method will first locate
     * the endpoint using the provided url signature and then attempt to build the path.
     * to the controller
     * @param  {Number} tenantId The tenant ID for which the endpoint path must be returned
     * @param  {String} url      The url signature for which the controller must be returned
     * @return {String}          The path to the controller or null if the endpoint is not found
     */
    app.getPageEndpointPath = function(tenantId, url) {
        var endpoint = this.getPageEndpoint(tenantId, url);
        if (!endpoint) {
            log.warn('Could not locate the endpoint ' + url);
            return null;
        }
        return getAppExtensionBasePath() + '/' + endpoint.owner + '/pages/' + endpoint.path;
    };
    /**
     * Returns endpoint details of an api registered with the extensible application
     * @param  {Number} tenantId The tenant ID for which the endpoint details must be returned
     * @param  {String} url      The url signature of the api for which the endpoint details must be returned
     * @return {Object}          An endpoint object containing the url,path and secured
     * @throws Unable to locate configuration for tnenat.Cannot locate api endpoint
     * @throws The app configuration details could not be loaded for tenant
     */
    app.getApiEndpoint = function(tenantId, url) {
        var configs = core.configs(tenantId);
        if (!configs) {
            log.warn('Unable to locate configuration of tenant ' + tenantId + '.Cannot locate api endpoint');
            throw 'Unable to locate configuration of tenant ' + tenantId + '.Cannot locate api endpoint';
        }
        var appConfig = configs.appConfig;
        if (!appConfig) {
            log.warn('The app configuration details could not be loaded for tenant: ' + tenantId);
            throw 'The app configuration details could not be loaded for tenant: ' + tenantId;
        }
        return appConfig.getApiEndpoint(url);
    };
    /**
     * Returns details on the set of api endpoints registered with the extensible application
     * @param  {Number} tenantId The tenant ID for which the api endpoints must be returned
     * @return {Array}          An array of endpoint objects
     * @throws Unable to locate tenant configuration
     * @throws Unable to locate app configuration
     */
    app.getApiEndpoints = function(tenantId) {
        //Obtain the app object
        var configs = core.configs(tenantId);
        if (!configs) {
            log.warn('Unable to locate the tenant configuration');
            throw 'Unable to locate tenant configuration';
        }
        var appConfig = configs.appConfig;
        if (!appConfig) {
            log.warn('The app configuration details could not be loaded for tenant: ' + tenantId);
            throw 'Unable to locate app configuration';
        }
        return appConfig.getApiEndpoints();
    };
    /**
     * Returns the path to controller handling api endpoint of the provided url signature.The method will first call
     * the getApiEndpoint to obtain the endpoint details.
     * @param  {Number} tenantId  The tenant ID for which the endpoint path must be returned
     * @param  {String} url       The url signature of the api endpoint for which the api controller path must be returned
     * @return {String}           The path to the controller endpoint or NULL if the endpoint is not located
     */
    app.getApiEndpointPath = function(tenantId, url) {
        var endpoint = this.getApiEndpoint(tenantId, url);
        if (!endpoint) {
            log.warn('Could not locate the endpoint ' + url);
            return null;
        }
        return getAppExtensionBasePath() + '/' + endpoint.owner + '/apis/' + endpoint.path;
    };
    /**
     * Returns details on a particular feature of the extensible application
     * @param  {Number} tenantId    The tenant ID for which the feature details must be returned
     * @param  {String} featureName The name of the feature
     * @return {Object}             An object defining a feature.If the feature cannot be located NULL will be returned
     */
    app.getFeatureDetails = function(tenantId, featureName) {
        var user = require('store').user;
        var configs = user.configs(tenantId);
        if (!configs) {
            log.error('Unable to locate configurations for the tenant: ' + tenantId + '.Cannot retrieve feature details of : ' + featureName);
            return null;
        }
        if (!configs.features) {
            log.error('Unable to locate features block in configuration.Cannot retrieve feature details of : ' + featureName);
            return null;
        }
        if (!configs.features[featureName]) {
            log.error('Cannot retrieve feature  details of ' + featureName + ' as it does not exist in the feature block.');
            return null;
        }
        var urlUtils = require('utils').url;
        //Populate any urls which are present with the correct server details - fill in port and host names
        var keys = urlUtils.popServerDetails(configs.features[featureName].keys || {});
        configs.features[featureName].keys = keys;
        return configs.features[featureName];
    };
    /**
     * Returns whether a particular extensible application feature is enabled.If the feature is not located then the method will
     * always return false.This method will first internally call the getFeatureDetails method to locate the feature
     * @param  {Number}  tenantId     The tenant ID for which the feature details must be returned
     * @param  {String}  featureName  The name of the feature
     * @return {Boolean}             True if the feature is located and is enabled,False if the feature is not located or has been disabled
     */
    app.isFeatureEnabled = function(tenantId, featureName) {
        var details = this.getFeatureDetails(tenantId, featureName);
        if (!details) {
            log.warn('Could not locate feature details of : ' + featureName + '.The feature will be assumed to be disabled.');
            return false;
        }
        return details.enabled ? details.enabled : false;
    };
    /**
     * Returns  the social feature is enabled.This method will internally call the getFeatureDetails method
     * to locate the details about the social component.If the social feature is not found then NULL is returned
     * @param  {Number} tenantId [description]
     * @return {Object}          [description]
     */
    app.getSocialFeatureDetails = function(tenantId) {
        var details = this.getFeatureDetails(tenantId, constants.SOCIAL_FEATURE);
        if (!details) {
            log.error('Unable to locate social feature details as it was not located in the features configuration block.');
            return null;
        }
        return details;
    };
    /**
     * Returns authentication details for the application.If a method is not provided
     * then the active method is picked up from the tenant configurations
     * @param  {Number} tenanId The tenant ID for which the authenticatiin details must be retuend
     * @param  {String} method   The authentication method for which the details must be retuend
     * @return {Object}         An object containing details about the authentication method.Any host name and ports are auto populated
     * @throws Unable to configurations for the tenant.Cannot retrieve authentication feature details
     * @throws Unable to locate configurations for the tenant.Cannot retrieve authentication feature details
     * @throws Unable to locate authentication feature details for tentna t.Cannot retrieve authentication feature block
     * @throws Unable to locate authentication methods for tenant.Cannot retireve authentication feature details
     */
    app.getAuthenticationDetails = function(tenanId, method) {
        var server = require('store').server;
        var configs = server.configs(tenanId);
        var method = method || null;
        if (!configs) {
            log.error('Unable to locate configurations for the tenant: ' + tenanId + '.Cannot retrieve authentication feature details.');
            throw 'Unable to locate configurations for the tenant: ' + tenanId + '.Cannot retrieve authentication feature details.';
        }
        configs = configs['server.user.options'];
        if (!configs) {
            log.error('Unable to locate configurations for the tenant: ' + tenanId + '.Cannot retrieve authentication feature details.');
            throw 'Unable to locate configurations for the tenant: ' + tenanId + '.Cannot retrieve authentication feature details.';
        }
        if (!configs.authentication) {
            log.error('Unable to locate authentication feature details for tenant: ' + tenanId + '.Cannot retrieve authentication feature block');
            throw 'Unable to locate authentication feature details for tenant: ' + tenanId + '.Cannot retrieve authentication feature block';
        }
        var authentication = configs.authentication;
        if (!authentication.methods) {
            log.error('Unable to locate authentication methods for tenant: ' + tenanId + '.Cannot retrieve authentication feature details.');
            throw 'Unable to locate authentication methods for tenant: ' + tenanId + '.Cannot retrieve authentication feature details.';
        }
        var methods = authentication.methods;
        if (!method) {
            method = authentication.activeMethod || null;
        }
        if (!method) {
            log.error('No authentication method was provided ');
            throw 'No authentication method was provided';
        }
        if ((!methods[method]) && (!methods[method].attributes)) {
            log.error('Unable to locate authentication details for method: ' + method);
            throw 'Unable to locate authentication details for method: ' + method;
        }
        var attributes = methods[method].attributes;
        var utils = require('utils').url;
        return utils.popServerDetails(attributes);
    };
    /**
     * Returns the active authetnicaton method for the application
     * for the given tenant
     * @param  {Number} tenantId The tenant ID for which the authentication details must be returned
     * @return {String}          The authenticaiton method
     * @throws Unable to locate configurations for the tenant.Cannot retrieve authentication feature details
     * @throws Unable to locate configurations for the tenant.Cannot retrieve authentication feature details
     * @throws Unable to  locate authentication feature details  for tenant.Cannot retriee authentication feature block
     * @throws Unable to locate authentication methods for tenant.Cannot retrieve authentication feature details
     */
    app.getAuthenticationMethod = function(tenantId) {
        var server = require('store').server;
        var configs = server.configs(tenanId);
        var method = method || null;
        if (!configs) {
            log.error('Unable to locate configurations for the tenant: ' + tenanId + '.Cannot retrieve authentication feature details.');
            throw 'Unable to locate configurations for the tenant: ' + tenanId + '.Cannot retrieve authentication feature details.';
        }
        configs = configs['server.user.options'];
        if (!configs) {
            log.error('Unable to locate configurations for the tenant: ' + tenanId + '.Cannot retrieve authentication feature details.');
            throw 'Unable to locate configurations for the tenant: ' + tenanId + '.Cannot retrieve authentication feature details.';
        }
        if (!configs.authentication) {
            log.error('Unable to locate authentication feature details for tenant: ' + tenanId + '.Cannot retrieve authentication feature block');
            throw 'Unable to locate authentication feature details for tenant: ' + tenanId + '.Cannot retrieve authentication feature block';
        }
        var authentication = configs.authentication;
        if (!authentication.activeMethod) {
            log.error('Unable to locate authentication methods for tenant: ' + tenanId + '.Cannot retrieve active authentication method.');
            throw 'Unable to locate authentication methods for tenant: ' + tenanId + '.Cannot retrieve authentication feature details.';
        }
        return authentication.activeMethod;
    };
    /**
     * Returns a path to a caramel resource that is qualified by application extensions.The method will check  if a given resource
     * is provided by any application extensions.It is assumed that each extension will contain a theme with the provided themeName.If
     * a resource is located in a extension theme directory the path will be changed to it
     * @param  {Object} request       Jaggery request object
     * @param  {String} path          The resource path to be resolved
     * @param  {String} themeName     The name of the active caramel theme
     * @param  {Object} themeObj      The caramel theme context
     * @param  {Function} themeResolver The caramel theme resolver function
     * @param  {Object} session       Jaggery session object
     * @return {String}               A path which is qualified by an application extensions
     */
    app.resolve = function(request, path, themeName, themeObj, themeResolver, session) {
        var resPath = path;
        path = '/' + path;
        var uriMatcher = new URIMatcher(request.getRequestURI());
        var extensionMatcher = new URIMatcher(path);
        var extensionPattern = '/{root}/extensions/app/{name}/{+suffix}';
        var uriPattern = '/{context}/pages/{+suffix}';
        extensionMatcher.match(extensionPattern);
        uriMatcher.match(uriPattern);
        var extensionOptions = extensionMatcher.elements() || {};
        var uriOptions = uriMatcher.elements() || {};
        //Determine if the path does not reference an app extension
        if (!extensionOptions.name) {
            //Determine if the uri references an extension page even though the resource does not reference one
            //This will allow resources to be overridden by the extension
            var pageName = getPage(uriOptions.suffix || '');
            var server = require('store').server;
            var user = server.current(session);
            var tenantId = -1234; //TODO: Replace this with constants.DEFAULT_TENANT
            if (user) {
                tenantId = user.tenantId;
            }
            var endpoint = this.getPageEndpoint(tenantId, pageName);
            //If the uri does not point to an app extension endpoint then do nothing
            if (!endpoint) {
                return null;
            }
            var extensionResourcePath = '/extensions/app/' + endpoint.owner + '/themes/' + themeName + '/' + resPath;
            //Check if the resource exists
            var extensionResource = new File(extensionResourcePath);
            if (extensionResource.isExists()) {
                return extensionResourcePath;
            }
            //If the resource does not exist then do nothing
            return null;
        }
        //The resource path references an app extension 
        var extensionPath = '/extensions/app/' + extensionOptions.name + '/themes/' + themeName + '/' + extensionOptions.root + '/' + extensionOptions.suffix;
        var extensionFile = new File(extensionPath);
        if (extensionFile.isExists()) {
            return extensionPath;
        }
        //There is no matching resource in the app extension so look for one in the themes directory
        extensionPath = extensionOptions.root + '/' + extensionOptions.suffix;
        var themeContextPath = themeResolver.call(themeObj, extensionPath);
        return themeContextPath;
    };
}(app, core, artifacts));