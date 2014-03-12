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

    function Extension() {
        this.name = '';
        this.version = '';
        this.main = '';
        this.consumes = [];
        this.provides = [];
        this.subDirs = []; //The map of all sub directories
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
        log.info('Executing event for: '+target+':'+action );
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
        this.extensions = {}; //The map of extensions
    }

    /**
     * The functionality of fiber are provided by plug-ins that
     * listen to specific events
     */
    Fiber.prototype.plugin = function (plugin, options) {
        plugin(this, options);
    };

    /**
     * The function is used to register an extension
     * @param extension
     */
    Fiber.prototype.register = function (extensionPath) {
        var dir = new File(extensionPath);

        //Check if the provided path points to a directory
        if (!dir.isDirectory()) {
            throw EXCEPTION_NOT_A_DIR;
        }

        //Check for an extension.json file
        var configFile = utils.file.getFileInDir(dir, EXTENSION_CONFIG_FILE);

        //Do nothing if the extension file is not present in the directory
        if (!configFile) {
            log.warn('Extension configuration file missing in directory: ' + dir.getName());
            return;
        }

        var extension = processExtensionConfigFile(extensionPath + '/' + configFile.getName());

        this.extensions[extension.name] = extension;

        processExtensionDirectory(dir, extension, this);

        processDependencyMap(extension, this.dependencyMap);
    };

    Fiber.prototype.list = function (extensionName) {
        var list = this.dependencyMap.list(extensionName);
    };

    /**
     * The function goes through all the extensions that are activated and then starts them up
     */
    Fiber.prototype.init = function (context) {
        var extension;

        for (var index in this.extensions) {
            extension = this.extensions[index];
            this.dependencyMap.invoke(extension.name, function (item) {
                log.info('Invoking main.js of ' + item.name);
                item.ref={};
            });

        }
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