var appCore = {};
(function(appCore, core) {
    var log = new Log('app-core');
    var getAppExtensionBasePath = function() {
        return '/extensions/app';
    };
    var getAppExtensionFileName = function() {
        return 'app.js';
    };
    var getCurrentAppExtensionFileName = function(currentDir) {
        return getAppExtensionBasePath() + '/' + currentDir.getName() + '/' + getAppExtensionFileName();
    };
    var addToConfigs = function(tenantId, appResources) {
        var configs = core.configs(tenantId);
        configs.appResources = appResources;
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
        extensionMap[appExtensionName] = app;
        log.info('Successfully loaded app extension: ' + appExtensionName);
    };
    var init = function(tenantId) {
        load(tenantId);
    };
    appCore.init = function() {
        var event = require('event');
        event.on('tenantLoad', function(tenantId) {
            init(tenantId);
        });
    };
    appCore.force = function() {
        init(-1234);
    }
}(appCore, core));