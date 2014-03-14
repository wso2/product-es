/**
 * Description: The following script is a simple framework for structuring an app.
 * It will allow you to define your app as a set of extensions.
 * "Everything is an extension "
 */
var createApp = {};
var getApp = {};

var module = (function () {

    var log = new Log('jaggery-fiber');
    var utils = require('utils');
    var DependencyMap = require('/modules/dependency-map.js').DependencyMap;

    var EXTENSION_CONFIG_FILE = 'package.json'; //The configuration file for the directory


    function Extension(options) {
        this.name = '';
        this.version = '';
        this.main = '';
        this.consumes = [];
        this.children = [];
        this.provides = [];
        this.init = [];      //The list of all files that should be required automatically when this extension is loaded
        this.path = '';
        this.subDirs = []; //The map of all sub directories

        var options = options || {};
        utils.reflection.copyProps(options, this);
    }

    function Fiber(fnGetAppContext) {
        this.plugins = [];
        this.dependencyMap = new DependencyMap();
        this.packages = {}; //The map of extensions
        this.app = {};

        fnGetAppContext(this.app);

        this.services = new ServiceMap();
        this.components = new ServiceMap();
        this.genericPipe = new utils.patterns.GenericPipe();
    }

    var setServices = function (fiber) {
        fiber.app.services = new ServiceMap();
        fiber.app.components = new ServiceMap();
    }

    /**
     * The functionality of fiber are provided by plug-ins that
     * listen to specific events
     */
    Fiber.prototype.plugin = function (plugin, options) {
        plugin(this, options);
    };

    Fiber.prototype.readConfig = function (configJSON) {
        //Go through all packages
        var packages = configJSON.packages || [];

        for (var index in packages) {

            this.register(packages[index]);
        }
    };

    /**
     * The function is used to register an extension
     * @param extension
     */
    Fiber.prototype.register = function (extensionPath) {
        registerPackage(extensionPath, this.dependencyMap, this.packages, null);
    };

    Fiber.prototype.list = function (extensionName) {
        var list = this.dependencyMap.list(extensionName);
    };

    /**
     * The function goes through all the extensions that are registered  and then starts them up.
     *
     */
    Fiber.prototype.init = function (context) {
        //log.info(this.packages);
        for (var key in this.packages) {
            initPackage(this.dependencyMap, this.packages[key]._instance, this.packages);
        }
    };

    /**
     * The function is used to plugin a plugin to a chain
     * @param components
     */
    Fiber.prototype.chain = function (components) {
        //Component string
        if (components instanceof String) {
            locateComponent(component, this);
        }
        else if (components instanceof Array) {
            locateComponents(components, this);
        }
        else if ((components instanceof Function) || (component instanceof Object)) {
            this.genericPipe.plug(component);
        }
        else {
            log.warn('The component type ' + (typeof components) + ' cannot be plugged into the chain');
        }

    };

    Fiber.prototype.resolve = function (data, req, res, session) {

        this.genericPipe.resolve(data, req, res, session);
        //Reset the pipe to a new chain
        this.genericPipe = new utils.patterns.GenericPipe();
    };


    var locateComponent = function (componentName, fiber) {
        var component = fiber.components.get(componentName);
        if (!component) {
            log.warn('Component: ' + componentName + ' could not be found.');
            return;
        }
        else {
            fiber.genericPipe.plug(component);
        }
    };

    var locateComponents = function (componentArray, fiber) {
        for (var index in componentArray) {
            locateComponent(componentArray[index], fiber);
        }
    };

    var executeChain = function (data, components, req, res, session, fiber) {
        var genericPipe = new utils.patterns.GenericPipe();
        //Get the list of components
        var compList = components.split(',');
        var compName;
        var component;
        for (var index in compList) {
            compName = compList[index];
            //Get the component
            component = fiber.components.get(compName);
            if (!component) {
                log.warn('Component: ' + compName + ' could not be found.');
            }
            else {
                //omponent.handle(data);
                genericPipe.plug(component);
            }
        }

        genericPipe.resolve(data, req, res, session);
    };

    /**
     * The function will read the contents of the provided path and then
     * load the location as an extension
     * @param path
     */
    var registerPackage = function (path, dm, packages, rootPackage) {
        var dir = new File(path);

        log.info('Scanning path: ' + path);

        if (!dir.isDirectory()) {
            log.warn('Dir: ' + dir.getName() + ' not a directory.Path: ' + path);
            return;
            //throw EXCEPTION_NOT_A_DIR;
        }


        //Check if the directory contains a package json
        var packageConfig = utils.file.getFileInDir(dir, EXTENSION_CONFIG_FILE);

        //Do nothing if this is not a configuration file
        if (!packageConfig) {
            log.warn('Directory not a package and will be skipped. Directory:' + dir.getName());
            return;
        }

        //Create a package instance
        var instance = getPackage(packageConfig, path);

        //Set the path to the package
        instance.path = path;//getRelativePath(dir, path);

        //Load the dependencies
        dm.add(instance.name, (instance.consumes || []));

        if (!rootPackage) {
            //log.info(instance.name + ' is a root level package.');
            packages[instance.name] = {};
            packages[instance.name]._instance = instance;
        }
        else {
            //log.info(instance.name + ' child of parent: ' + rootPackage.name);
            packages[rootPackage.name][instance.name] = {};
            packages[rootPackage.name][instance.name]._instance = instance;
        }

        //Initialize the child packages
        initChildren(instance, dm, packages);
    };

    /**
     * The function will recursively initialize a given package instance
     * and its children
     * @param instance
     */
    var initPackage = function (dm, instance, packages) {
        if (!instance) {
            return;
        }
        //log.info('Initializing package: ' + instance.name);
        dm.invoke(instance.name, function (item) {

            //Execute the main js file if it is present
            executeMain(instance);

            //Perform any init operations
            executeInit(instance);

            //Initialize any children
            var childPackages = packages[instance.name];
            // log.info(packages[instance.name]);
            for (var key in childPackages) {
                //log.info('Child package: '+key);
                initPackage(dm, childPackages[key]._instance, packages);
            }

        });
    };

    /**
     * The main.js file in a package will be executed if one is present
     * @param packageInstance The package to be initialized
     */
    var executeMain = function (packageInstance) {
        if (packageInstance.main) {
            log.info('Requiring main ' + packageInstance.path + '/' + packageInstance.main);
            require(packageInstance.path + '/' + packageInstance.main);
        }
    };

    var executeInit = function (packageInstance) {
        var current;

        for (var index in packageInstance.init) {
            current = packageInstance.init[index];
            require(packageInstance.path + '/' + current);
        }
    };

    /**
     * The package will load any child packages
     * @param packageInstance
     */
    var initChildren = function (packageInstance, dm, packages) {
        var child;
        for (var index in packageInstance.children) {
            child = packageInstance.children[index];
            registerPackage('/' + packageInstance.path + '/' + child, dm, packages, packageInstance);
        }
    };

    var getPackage = function (file, path) {
        var configPath = getRelativePath(file, path);
        var configJSON = require(configPath);
        return new Extension(configJSON);
    };

    var getRelativePath = function (dir, path) {
        return path + '/' + dir.getName();
    };

    function ServiceMap() {
        this.services = {};
        this.dm = new DependencyMap();
    }

    ServiceMap.prototype.register = function (serviceName, service, dependentServices) {

        if (!this.services.hasOwnProperty(serviceName)) {
            this.services[serviceName] = {};
        }

        this.services[serviceName].source = service;
        this.services[serviceName].instance = null;

        //Record the dependency
        this.dm.add(serviceName, dependentServices || []);
    };

    ServiceMap.prototype.get = function (serviceName) {

        //this.dm.invoke(serviceName, function (item) {
        //   log.info(stringify(item));
        //});
        log.info('Returning instance of ' + serviceName);
        if (!this.services.hasOwnProperty(serviceName)) {
            return null;
        }

        return new this.services[serviceName].source();
    };

    var instance;
    var createAppLogic = function (config, cb) {
        instance;
        // instance = session.get('FIBER_APP');
        //if (!instance) {
        //Check if an app is in the session and return it
        instance = new Fiber(cb);
        instance.readConfig(config);
        //session.put('FIBER_APP', instance);
        //  }

        return instance;
    };

    var getAppLogic = function () {

        //var instance = session.get('FIBER_APP');

        if (!instance) {
            throw 'There is no app';
        }

        return instance;
    };

    createApp = createAppLogic;
    getApp = getAppLogic;

}());