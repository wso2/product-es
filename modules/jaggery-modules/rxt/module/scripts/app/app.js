var app = {};
(function(app, core) {
    var log = new Log('app-core');

    function Endpoints() {
        this.endpoints = [];
    }
    Endpoints.prototype.getEndpointByUrl = function(url) {
        for (var index in this.endpoints) {
            if (this.endpoints[index].url == url) {
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
            return;
        }
        this.endpoints.push(endpoint);
    };
    Endpoints.prototype.addMultiple = function(endpoints) {
        for (var index in endpoints) {
            log.info('Adding endpoint: ' + stringify(endpoints[index]));
            this.add(endpoints[index]);
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
        loadAppExtensions(dir);
    };
    var loadAppExtensions = function(rootDir) {
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
            log.info('Located app extension ' + appExtensionName);
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
        log.info('### Starting to process the app extensions ###');
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
        log.info('### Finished processing the app extensions ###');
        log.info(app);
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
            log.info('Successfully loaded app extension: ' + appExtensionName + ' but ignoring extension due to ignoreExtension property been true.');
            return;
        }
        extensionMap[appExtensionName] = app;
        log.info('Successfully loaded app extension: ' + appExtensionName);
    };
    var init = function(tenantId) {
        load(tenantId);
    };
    app.init = function() {
        var event = require('event');
        event.on('tenantLoad', function(tenantId) {
            init(tenantId);
        });
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
    app.resolve = function(request, path, themeName, themeObj, themeResolver,session) {
        log.info('Path: ' + path);
        var resPath = path;
        path = '/' + path;
        var uriMatcher = new URIMatcher(request.getRequestURI());
        var extensionMatcher = new URIMatcher(path);
        var extensionPattern = '/{root}/extensions/app/{name}/{+suffix}';
        var uriPattern = '/{context}/pages/{+suffix}';
        extensionMatcher.match(extensionPattern);
        uriMatcher.match(uriPattern);
        var options = extensionMatcher.elements() || {};
        var uriOptions = uriMatcher.elements() || {};
        log.info('Looking for uri options: ' + stringify(uriOptions));
        log.info('Looking for extension options ' + stringify(options));
        log.info('Page name: ' + pageName);
        //Determine if the path references an extension,if not just return the path
        if (!options.name) {
            log.info('Resource not located in the app extensions: ' + resPath);
            //Determine if the uri references an extension page
            var pageName = getPage(uriOptions.suffix);
            log.info('Checking page : ' + pageName);
            var server=require('store').server;
            var user=server.current(session);
            var tenantId=-1234;
            if(user){
                tenantId=user.tenantId;
            }
            var endpoint=this.getPageEndpoint(tenantId,pageName);
            if(!endpoint){
                return themeResolver.call(themeObj, resPath);
            }

            var mod='/extensions/app/'+endpoint.owner+'/themes/'+themeName+'/'+resPath;

            //Check if the resource exists
            var file=new File(mod);
            if(file.isExists()){
                return mod;
            }
            return themeResolver.call(themeObj, resPath);;
        }
        var extensionPath = '/extensions/app/' + options.name + '/themes/' + themeName + '/' + options.root + '/' + options.suffix;
        log.info('Looking for file: ' + extensionPath);
        var extensionFile = new File(extensionPath);
        if (extensionFile.isExists()) {
            log.info('Resolved path: ' + extensionPath);
            return extensionPath;
        }
        extensionPath = options.root + '/' + options.suffix;
        var modPath = themeResolver.call(themeObj, extensionPath);
        log.info('Resolved path: ' + modPath);
        return modPath;
    };
    var getPage = function(uri) {
        var comps = uri.split('/');
        return comps.length > 0 ? comps[0] : null;
    };
}(app, core));