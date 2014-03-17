var DependencyMap = {};

(function () {
    var utils = require('utils');
    var log=new Log('dependency-map');

    function Dependency(options) {
        this.ref = null;
        this.name = '';
        this.dependants = [];//An array containing the names of all dependants
        utils.reflection.copyProps(options, this);
    }

    Dependency.prototype.isResolved = function () {
        if (this.ref) {
            return true;
        }

        return false;
    };

    function GenericDependencyMap() {
        this.map = {};
    }

    GenericDependencyMap.prototype.add = function (target, dependencies) {

        if (!this.map.hasOwnProperty(target)) {
            this.map[target] = new Dependency({name: target, dependants: dependencies});
        }

        this.map[target].dependants = dependencies;
    };

    /*
     The function returns the Dependant object
     */
    GenericDependencyMap.prototype.get = function (target) {
        return this.map[target];
    };

    GenericDependencyMap.prototype.resolve = function (target, cb) {
        recursiveCallback(target, cb, this);
    };

    GenericDependencyMap.prototype.list = function (target) {
        log.info('Listing dependencies for ' + target);
        var dependency = this.get(target);
        var list = [];


        for (var index in dependency.dependants) {
            log.info('Target: ' + target + ' depends on ' + dependency.dependants[index]);
            recursiveRecord(dependency.dependants[index], this, list);
        }

        return list;
    };

    GenericDependencyMap.prototype.invoke = function (target, cb) {
        var dependency = this.get(target);

        if(!dependency){
            log.warn('Could locate dependency: '+target);
            return;
        }

        for (var index in dependency.dependants) {

            recursiveCallback(dependency.dependants[index], cb, this);
        }

        //Attempt to resolve the current dependency if it has not already been resolved
        if (!dependency.isResolved()) {
            cb(dependency);
        }

    };

    var recursiveCallback = function (target, cb, dm) {

        //Get the current dependency object
        var dependency = dm.get(target);

        if (!dependency) {
            throw 'Cannot find dependency: ' + target;
        }

        if (dependency.isResolved()) {
            log.info('Dependency has already been resolved');
            return;
        }

        cb(dependency);

        //If it has no dependencies do nothing
        if (dependency.dependants.length == 0) {
            return;
        }
        else {
            var dependant;

            for (var index in dependency.dependants) {
                dependant = dependency.dependants[index];
                recursiveCallback(dependant, cb, dm);
            }

            return;
        }

    };

    /**
     * The function returns an array of all the dependencies for
     * a given target
     * @param target
     * @param dm
     * @param list
     * @returns An array of all dependents
     */
    var recursiveRecord = function (target, dm, list) {

        var dependency = dm.get(target);

        if (!dependency) {
            throw 'Cannot find dependency: ' + target;
        }

        addToList(list, target);

        if (dependency.dependants.length == 0) {
            return list;
        }
        else {

            var dependant;

            for (var index in dependency.dependants) {
                dependant = dependency.dependants[index];
                log.info('Recursive Target ' + target + ' depends on ' + dependant);
                recursiveRecord(dependant, dm, list);
            }

            return list;
        }
    };

    var addToList = function (list, item) {
        if (list.indexOf(item) > -1) {
            return;
        }

        list.push(item);
    };


    DependencyMap = GenericDependencyMap;

}());