var fiber = {};
var app = {};
var component;

(function () {

    var EXTENSION_CONFIG = 'package.json';
    var utils = require('utils');
    var log = new Log();

    function Fiber() {
        this.packMap = {};
        this.components = new ServiceMap();
        this.context = {};
    }

    Fiber.prototype.init = function (cb) {
        var appConfig = require('/' + EXTENSION_CONFIG);
        var subPackage;
        var path;
        var dir;
        cb(this.context);
        for (var index in appConfig.subPackages) {
            subPackage = appConfig.subPackages[index];
            path = '/' + subPackage;
            dir = new File(path);
            recursiveRegisterPackages(dir, this.packMap, '/' + subPackage);
        }
        init(this.packMap);
    };

    Fiber.prototype.component = function () {
        return new ComponentContainer(this.components);
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

            //Go through each sub directories
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

    function ServiceMap() {
        this.services = {};
    }

    ServiceMap.prototype.register = function (serviceName, service, dependentServices) {

        if (!this.services.hasOwnProperty(serviceName)) {
            this.services[serviceName] = {};
        }

        this.services[serviceName].source = service;
        this.services[serviceName].instance = null;
    };

    ServiceMap.prototype.get = function (serviceName) {
        if (!this.services.hasOwnProperty(serviceName)) {
            return null;
        }
        return new this.services[serviceName].source();
    };

    function ComponentContainer(serviceMap) {
        this.pipe = new utils.patterns.GenericPipe();
        this.serviceMap = serviceMap;
    }

    ComponentContainer.prototype.chain = function (components) {
       // log.info('Chain: ' + components);
        var type = typeof components;
        //Component string
        if (type == 'string') {
            components = components.split(',');
            locateComponents(components, this);
        }
        else if ((type == 'function') || (type == 'object')) {
            this.pipe.plug(components);
        }
        else {
            log.warn('The component type ' + (typeof components) + ' cannot be plugged into the chain');
        }
        return this;
    };

    ComponentContainer.prototype.finally=function(cb){
        this.pipe.finally(cb);
        return this;
    }

    ComponentContainer.prototype.resolve = function (data, req, res, session) {
        this.pipe.resolve(data, req, res, session);
    }

    var locateComponent = function (componentName, container) {
        var component = container.serviceMap.get(componentName);
        if (!component) {
            log.warn('Component: ' + componentName + ' could not be found.');
            return;
        }
        else {
            log.info('Plugged in component: ' + componentName);
            container.pipe.plug(component);
        }
    };

    var locateComponents = function (componentArray, container) {
        for (var index in componentArray) {
            locateComponent(componentArray[index], container);
        }
    };

    var instance;

    var getApp = function (appName) {
        if (!instance) {
            instance = new Fiber();
        }
        return instance;
    };

    //fiber = Fiber;
    app = getApp();
    component = app.component();

}());