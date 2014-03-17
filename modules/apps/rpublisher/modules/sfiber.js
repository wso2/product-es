/**
 * Description: This script allows an application to be defined as a set of packages that will be automatically
 *              initialized based on a package.json file found in the root of the app.
 *              It also allows the definition of components
 *              A page which is visible to the user consists of a collection of components
 * Created Date: 17/3/2014
 */
var fiber = {};
var app = {};

(function () {

    var EXTENSION_CONFIG = 'package.json';
    var utils = require('utils');
    var log = new Log();
    var TYPE_STRING='string';
    var TYPE_OBJECT='object';
    var TYPE_FUNCTION='function';

    /**
     * The class defines the structure of the application
     * @constructor
     */
    function Fiber() {
        this.packMap = {};
        this.componentsMap = new ServiceMap();
        this.context = {};
        this.components = new ComponentContainer(this.componentsMap);
    }

    /**
     * The function initializes the app based on a package.json file found in the root directory
     * @param cb A callback function that can be used to initialize the app context with some values
     */
    Fiber.prototype.init = function (cb) {
        var appConfig = require('/' + EXTENSION_CONFIG);
        var subPackage;
        var path;
        var dir;

        //Check if an app package.json file exists
        if (!checkForAppPackage()) {
            log.warn(EXTENSION_CONFIG + ' not found in the root directory of the application.');
            return;
        }

        cb(this.context);
        for (var index in appConfig.subPackages) {
            subPackage = appConfig.subPackages[index];
            path = '/' + subPackage;
            dir = new File(path);
            recursiveRegisterPackages(dir, this.packMap, '/' + subPackage);
        }
        init(this.packMap);
    };

    /**
     * The function checks if the package.json file is present in the root
     * directory of the application
     */
    var checkForAppPackage = function () {
        var dir = new File('/' + EXTENSION_CONFIG);

        return dir.isExists();
    };

    /**
     * The function is used to register a component used by the application
     * @param componentName  The name of the component, this is unique across the application
     *                       It is possible to override a component by specifying a new component with the
     *                       same name in the extension directory
     * @param componentCallback The logic of the component
     */
    Fiber.prototype.component = function (componentName, componentCallback) {
        this.componentsMap.register(componentName, componentCallback);
    };

    /**
     * The function is used to recursively register packages
     * @param rootDir
     * @param packMap
     */
    var recursiveRegisterPackages = function (rootDir, packMap, path) {
        var dirs = utils.file.getAllSubDirs(rootDir);


        if (isPackage(rootDir)) {
            //Record the package
            packMap[rootDir.getName()] = {};
            packMap[rootDir.getName()]._path = path;
            packMap[rootDir.getName()]._meta = require(path + '/' + EXTENSION_CONFIG);
        }


        //Stop traversal if this directory does not have sub directories
        if (!hasSubDirs(dirs)) {
            return;
        }
        else {
            var dir;

            //Go through each sub directory
            for (var index in dirs) {
                dir = dirs[index];
                recursiveRegisterPackages(dir, packMap[rootDir.getName()], path + '/' + dir.getName());
            }
        }
    };

    /**
     * The function initializes a package map
     * @param packMap
     */
    var init = function (packMap) {

        for (var key in packMap) {

            recursiveInitPackages(packMap[key]);
        }
    };

    /**
     * The function is used to recursively initialize packages
     * @param pack
     * @param packMap
     */
    var recursiveInitPackages = function (pack) {
        //log.info('Pack: ' + stringify(pack));
        requireFiles(pack);
        if (!pack) {
            return;
        }
        else {
            //Go through each key
            for (var key in pack) {

                //Ignore hidden properties
                if (!utils.reflection.isHiddenProp(key)) {

                    recursiveInitPackages(pack[key]);
                }

            }
        }
    };

    /**
     * The function determines if the directory contains any sub directories
     * @param dirs
     * @returns {boolean}
     */
    var hasSubDirs = function (dirs) {
        return(dirs.length == 0) ? false : true;
    };

    /**
     * The function determines if a directory is a package.A
     * directory is a package if;
     * -It has a package.json file
     * @param dir
     */
    var isPackage = function (dir) {
        //Check if there is a package.json
        var packageConfig = utils.file.getFileInDir(dir, EXTENSION_CONFIG);

        return (packageConfig) ? true : false;
    };

    /**
     * The function is used to require any files defined in the package.json
     * file
     * @param pack The package to be processed
     */
    var requireFiles = function (pack) {
        var file;
        for (var index in pack._meta.require) {
            file = pack._meta.require[index];
            require(pack._path + '/' + file);
            log.info('requiring: ' + pack._path + '/' + file);
        }

    };

    /**
     * The class defines a collection of services
     * @constructor
     */
    function ServiceMap() {
        this.services = {};
    }

    /**
     * The function allows a service to be registered with a given service name
     * @param serviceName
     * @param service
     */
    ServiceMap.prototype.register = function (serviceName, service) {
        //Determine if the service has already been registered
        if (!this.services.hasOwnProperty(serviceName)) {
            this.services[serviceName] = {};
        }

        this.services[serviceName].source = service;
        this.services[serviceName].instance = null;
    };

    /**
     * The function returns an instance of the requested services
     * after initializing it with the request,response and session
     * @param serviceName The name of the service to be instantiated
     * @returns The service function
     */
    ServiceMap.prototype.get = function (serviceName) {
        //Check if the service exists
        if (!this.services.hasOwnProperty(serviceName)) {
            return null;
        }

        return this.services[serviceName].source;
    };


    /**
     * The component contain class is used to allow chaining of components.
     * @param serviceMap The list of components registered for the application
     * @constructor
     */
    function ComponentContainer(serviceMap) {
        this.pipe = new utils.patterns.GenericPipe();
        this.serviceMap = serviceMap;
    }

    /**
     * This is a helper method which allows users to define a chain of components as chained function calls
     * within a GenericPipe instance.
     * @param components A string or function that defines a component.
     *                   If a user provides a string it will be first split by , to look for multiple component
     *                   definitions.This list of components are then referenced in the component map and plugged
     *                   into a generic pipe object
     *                   If a function is provided it is directly plugged into the pipe chain
     * @returns A reference to this object to allow the user to chain function calls
     */
    ComponentContainer.prototype.chain = function (components) {
        var type = typeof components;
        //Component string
        if (type == TYPE_STRING) {
            components = components.split(',');
            locateComponents(components, this);
        }
        else if ((type == TYPE_FUNCTION) || (type == TYPE_OBJECT)) {
            this.pipe.plug(components);
        }
        else {
            log.warn('The component type ' + type + ' cannot be plugged into the chain');
        }
        return this;
    };

    /**
     * The function wraps the finally method of the GenericPipe so that it can be
     * accessed through the chained function
     * @param cb
     * @returns {*}
     */
    ComponentContainer.prototype.finally = function (cb) {
        this.pipe.finally(cb);
        return this;
    };

    /**
     * The function allows the chain of components to be resolved by invoking the GenericPipe resolve
     * method (This is just a wrapper and is only present to allow chaining of function calls)
     * @param data The data to be manipulated by the component chain
     * @param req Request object
     * @param res Response object
     * @param session
     */
    ComponentContainer.prototype.resolve = function (data, req, res, session) {
        this.pipe.resolve(data, req, res, session);
    };

    /**
     * The function locates a component by checking the registered components of the fiber application
     * @param componentName The name of the component to be located
     * @param container  The fiber application
     */
    var locateComponent = function (componentName, container) {

        var component = container.serviceMap.get(componentName);

        if (!component) {
            log.warn('Component: ' + componentName + ' could not be found.');
            return null;
        }
        else {
            container.pipe.plug(component);
        }
    };

    /**
     * The function is used to locate an array of components provided as strings
     * @param componentArray The array of component names to be used in the search
     * @param container  The fiber app
     */
    var locateComponents = function (componentArray, container) {
        for (var index in componentArray) {
            locateComponent(componentArray[index], container);
        }
    };

    //TODO: Save this instance in the application context
    var instance;

    //TODO: This needs to accept a tenant Id . The app should be cached per tenant
    var getApp = function (appName) {
        if (!instance) {
            instance = new Fiber();
        }
        return instance;
    };

    app = getApp();

}());