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
var app = {};
(function(app, core) {
    var log = new Log('app-core');

    function Endpoints() {
        this.endpoints = [];
    }
    Endpoints.prototype.getEndpointByUrl = function(url) {
        for (var index in this.endpoints) {
            if (this.endpoints[index].url == url) {
                //If the secured property is not provided then the page is not
                this.endpoints[index].secured = this.endpoints[index].secured ? this.endpoints[index].secured : false;
                return this.endpoints[index];
            }
        }
        return null;
    };
    Endpoints.prototype.list = function() {
        return this.endpoints;
    };
    Endpoints.prototype.add = function(endpoint) {
        //First check if the endpoint already exists
        var existingEndpoint = this.getEndpointByUrl(endpoint.url);
        if (existingEndpoint) {
            existingEndpoint.path = endpoint.path;
            existingEndpoint.title = endpoint.title;
            existingEndpoint.owner = endpoint.owner;
            existingEndpoint.secured = this.endpoints[index].secured ? this.endpoints[index].secured : false;
            return;
        }
        this.endpoints.push(endpoint);
    };
    Endpoints.prototype.addMultiple = function(endpoints) {
        for (var index in endpoints) {
            this.add(endpoints[index]);
            log.info('Registered endpoint: ' + endpoints[index].url + ' (secured: ' + (endpoints[index].secured || false) + ')');
        }
    };

    function App() {
        this.pages = new Endpoints();
        this.apis = new Endpoints();
    }
    App.prototype.getPageEndpoints = function() {
        return this.pages.list();
    };
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

    function AppManager(appResources, ctx) {
        this.ctx = ctx;
        this.appResources = appResources;
    }
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
        var app = processAppExtensions(appResources);
        //Save the app object and appResources 
        var configs = core.configs(tenantId);
        configs.appResources = appResources;
        configs.appConfig = app;
    };
    var setExtensionName = function(items, extName) {
        for (var index in items) {
            items[index].owner = extName;
        }
    }
    var processExtension = function(extName, map, app) {
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
        var endpoints = serverCb().endpoints;
        if (!endpoints) {
            log.warn('The app extension ' + extName + ' has not defined any endpoints.');
            return;
        }
        var apiEndpoints = endpoints.apis || [];
        var pageEndpoints = endpoints.pages || [];
        setExtensionName(apiEndpoints, extName);
        setExtensionName(pageEndpoints, extName);
        app.addApiEndpoints(apiEndpoints);
        app.addPageEndpoints(pageEndpoints);
        map[extName].loaded = true;
        log.info('Finished processing app extension: ' + extName);
    };
    var processAppExtensions = function(appExtensions) {
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
                processExtension(key, appExtensions, app);
            } else {
                //Determine the load order
                stack = [];
                stack = recursiveProcess(key, appExtensions, stack);
                log.info('Loading dependencies: ' + stack);
                for (var index in stack) {
                    processExtension(stack[index], appExtensions, app);
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
                //stack = temp.concat(stack);
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
        // var configs = core.configs(tenantId);
        // if (!configs) {
        //     log.warn('Unable to locate configuration of tenant ' + tenantId + '.Cannot locate api endpoint');
        //     throw 'Unable to locate configuration of tenant ' + tenantId + '.Cannot locate api endpoint';
        // }
        // var appResources = configs.appResources;
        // if (!appResources) {
        //     log.warn('The app configuration details could not be loaded for tenant: ' + tenantId);
        //     throw 'The app configuration details could not be loaded for tenant: ' + tenantId;
        // }
        if (!appResources[extensionName]) {
            log.warn('Unable to load extension details for ' + extensionName + '.A renderer could not be created');
            return null;
        }
        var renderer = appResources[extensionName].renderer;
        if (!renderer) {
            log.warn('A renderer has not been defined in the app.js file.');
            return null;
        }
        //var ctx = core.createAppContext(session);
        //renderer = renderer(ctx);
        return renderer;
    };
    app.init = function(context) {
        var event = require('event');
        event.on('tenantLoad', function(tenantId) {
            init(tenantId, context);
        });
    };
    app.getContext = function() {
        var context = application.get(constants.APP_CONTEXT);
        return context;
    };
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
    app.createUserAppManager = function(session) {
        var server = require('store').server;
        var user = server.current(session);
        var tenantId = -1234;
        if (user) {
            tenantId = user.tenantId;
        }
        var appResources = this.getAppResources(tenantId);
        var ctx = core.createAppContext(session);
        return new AppManager(appResources, ctx);
    };
    app.createAnonAppManager = function(session, tenantId) {
        var server = require('store').server;
        var user = server.current(session);
        var appResources = this.getAppResources(tenantId);
        var ctx = core.createAppContext(session);
        return new AppManager(appResources, ctx);
    };
    app.getActivatedAssets = function(tenantId) {
        var user = require('store').user;
        var configs = user.configs(tenantId);
        if (!configs) {
            log.warn('Unable to locate tenant configurations in order to retrieve activated assets');
            throw 'Unable to locate tenant configurations in order to retrieve activated assets';
        }
        var assets = configs.assets;
        if (!assets) {
            log.warn('No activated assets');
            return [];
        }
        return assets;
    };
    app.getLandingPage = function(tenantId) {
        var landingPage = '/';
        //Obtain the configurations for the tenant
        var userMod = require('store').user;
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
    app.force = function() {
        init(-1234);
    }
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
    app.getPageEndpoints = function(tenantId) {
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
        return appConfig.getPageEndpoints();
    };
    app.getPageEndpointPath = function(tenantId, url) {
        var endpoint = this.getPageEndpoint(tenantId, url);
        if (!endpoint) {
            log.warn('Could not locate the endpoint ' + url);
            return null;
        }
        return getAppExtensionBasePath() + '/' + endpoint.owner + '/pages/' + endpoint.path;
    };
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
    app.getApiEndpointPath = function(tenantId, url) {
        var endpoint = this.getApiEndpoint(tenantId, url);
        if (!endpoint) {
            log.warn('Could not locate the endpoint ' + url);
            return null;
        }
        return getAppExtensionBasePath() + '/' + endpoint.owner + '/apis/' + endpoint.path;
    };
    /**
     * The function returns details about a given feature
     * @param  {[type]} tenantId [description]
     * @param  {[type]} url      [description]
     * @return {[type]}          [description]
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
        //Populate any urls which are present with the correct server details
        var keys = urlUtils.popServerDetails(configs.features[featureName].keys || {});
        configs.features[featureName].keys = keys;
        return configs.features[featureName];
    };
    app.isFeatureEnabled = function(tenantId, featureName) {
        var details = this.getFeatureDetails(tenantId, featureName);
        if (!details) {
            log.warn('Could not locate feature details of : ' + featureName + '.The feature will be assumed to be disabled.');
            return false;
        }
        return details.enabled ? details.enabled : false;
    };
    app.getSocialFeatureDetails = function(tenantId) {
        var details = this.getFeatureDetails(tenantId, constants.SOCIAL_FEATURE);
        if (!details) {
            log.error('Unable to locate social feature details as it was not located in the features configuration block.');
            return null;
        }
        return details;
    };
    /**
     * The method returns authentication details for the application.If a method is not provided
     * then the active method is picked up from the tenant configurations
     * @param  {[type]} tenanId [description]
     * @param  {[type]} method  [description]
     * @return {[type]}         [description]
     */
    app.getAuthenticationDetails = function(tenanId, method) {
        var server = require('store').server;
        var configs = server.configs(tenanId);
        var method = method || null;

        if (!configs) {
            log.error('Unable to locate configurations for the tenant: ' + tenanId + '.Cannot retrieve authentication feature details.');
            throw 'Unable to locate configurations for the tenant: ' + tenanId + '.Cannot retrieve authentication feature details.';
        }
        configs=configs['server.user.options'];
        if(!configs){
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
     * The function returns the active authetnicaton method for the application
     * for the given tenant
     * @param  {[type]} tenantId [description]
     * @return {[type]}          [description]
     */
    app.getAuthenticationMethod = function(tenantId) {
        var server = require('store').server;
        var configs = server.configs(tenanId);
        var method = method || null;
        if (!configs) {
            log.error('Unable to locate configurations for the tenant: ' + tenanId + '.Cannot retrieve authentication feature details.');
            throw 'Unable to locate configurations for the tenant: ' + tenanId + '.Cannot retrieve authentication feature details.';
        }
        configs=configs['server.user.options'];
        if(!configs){
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
     * The function is responsible for routing requests for resources to appropriate path
     * @param  {[type]} request       [description]
     * @param  {[type]} path          [description]
     * @param  {[type]} themeName     [description]
     * @param  {[type]} themeObj      [description]
     * @param  {[type]} themeResolver [description]
     * @param  {[type]} session       [description]
     * @return {[type]}               [description]
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
            var tenantId = -1234;
            if (user) {
                tenantId = user.tenantId;
            }
            var endpoint = this.getPageEndpoint(tenantId, pageName);
            //If the uri does not point to an app extension endpoint then do nothing
            if (!endpoint) {
                return null; //themeResolver.call(themeObj, resPath);
            }
            var extensionResourcePath = '/extensions/app/' + endpoint.owner + '/themes/' + themeName + '/' + resPath;
            //Check if the resource exists
            var extensionResource = new File(extensionResourcePath);
            if (extensionResource.isExists()) {
                return extensionResourcePath;
            }
            //If the resource does not exist then do nothing
            return null; //themeResolver.call(themeObj, resPath);;
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
}(app, core));