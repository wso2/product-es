/**
 * Description: The following script is a simple framework for structuring an app.
 * It will allow you to define your app as a set of extensions.
 * "Everything is an extension "
 */
var fiber = {};

var module = (function () {

    var log = new Log('jaggery-fiber');
    var utils = require('utils');
    var DependencyMap = require('/modules/dependency-map.js').DependencyMap;

    var EXTENSION_CONFIG_FILE = 'package.json'; //The configuration file for the directory
    var EXCEPTION_NOT_A_DIR = 'The provided path does not point to a directory';
    var EVENT_ALL = '*';

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

    function EventMap() {
        this.map = {};
        this.map[EVENT_ALL] = {};
    }

    EventMap.prototype.on = function (target, action, cb) {
        //Check if the target is not present
        if (!this.map.hasOwnProperty(target)) {
            this.map[target] = {};
        }

        //Check if the action is present
        if (!this.map[target].hasOwnProperty(action)) {
            this.map[target][action] = [];
        }

        this.map[target][action].push(cb);
    };

    /**
     * The function emits the registered events base don the provided action and target
     * @param target The target of the event
     * @param action The action to act on
     */
    EventMap.prototype.emit = function (target, action, context) {
        log.info('Executing event for: ' + target + ':' + action);
        executeEvents(target, action, context, this.map);
        executeEvents(EVENT_ALL, action, context, this.map)
        executeEvents(EVENT_ALL, EVENT_ALL, context, this.map);
    };

    var executeEvents = function (target, action, context, map) {
        if (!map.hasOwnProperty(target)) {
            return;
        }

        if (!map[target].hasOwnProperty(action)) {
            return;

        }

        var events = map[target][action];

        //Go through all the events
        for (var index in events) {
            events[index](context);
        }
    };


    function Fiber() {
        this.plugins = [];
        this.events = new EventMap();
        this.dependencyMap = new DependencyMap();
        this.packages = {}; //The map of extensions
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

        log.info('Attempting to register package: ' + path);

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
            log.info(instance.name + ' is a root level package.');
            packages[instance.name]={};
            packages[instance.name]._instance = instance;
        }
        else {
            log.info(instance.name + ' child of parent: ' + rootPackage.name);
            packages[rootPackage.name][instance.name]={};
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
        if(!instance){
            return;
        }
        log.info('Initializing package: '+instance.name);
        dm.invoke(instance.name, function (item) {

            //Execute the main js file if it is present
            executeMain(instance);

            //Perform any init operations
            //executeInit(instance);

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

    var processDependencyMap = function (extension, dm) {
        log.info('Registering extension')
        dm.add(extension.name, extension.consumes);
    };

    /**
     * The function reads the extension file provided and creates an extension object
     * @param file The extension configuration file
     */
    var processExtensionConfigFile = function (file) {
        //Read the file
        var conf = require(file);
        var extension = new Extension();
        utils.reflection.copyProps(conf, extension);

        return extension;
    };

    /**
     * The function processes the contents of an extension directory
     * @param dir
     * @param fiber
     */
    var processExtensionDirectory = function (dir, extension, fiber) {

        //Read all directories within an extension folder
        var subDirs = utils.file.getAllSubDirs(dir);
        var target;
        var action = 'init';

        for (var index in subDirs) {
            target = subDirs[index].getName();
            extension.subDirs.push(subDirs[index].getName());
            fiber.events.emit(target, action, buildContext(target, action, subDirs[index], extension));
        }
    };

    /**
     * The function is used to construct a context object which is used to construct the object that
     * is provided to plugins
     * @param target
     * @param action
     * @param dir
     * @param extension
     * @returns {{target: *, action: *, dir: *, extension: *}}
     */
    var buildContext = function (target, action, dir, extension) {
        return{
            target: target,
            action: action,
            dir: dir,
            extension: extension
        };
    }

    fiber = new Fiber();

}());