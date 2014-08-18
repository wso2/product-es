var core = {};
(function(core) {
    var CommonUtil = Packages.org.wso2.carbon.governance.lcm.util.CommonUtil;
    var LC_MAP = 'lc.map';
    var EMPTY = '';
    var log = new Log('lifecycle');
    var addRawLifecycle = function(lifecycleName, content, tenantId) {
        var lcMap = core.configs(tenantId);
        if (!lcMap.raw) {
            lcMap.raw = {};
        }
        lcMap.raw[lifecycleName] = new String(content);
    };
    var addJsonLifecycle = function(lifecycleName, definition, tenantId) {
        var lcMap = core.configs(tenantId);
        if (!lcMap.json) {
            lcMap.json = {};
        }
        lcMap.json[lifecycleName] = definition;
    };
    var getConfigRegistry = function(registry) {
        var rootReg = registry.registry;
        var configReg = rootReg.getChrootedRegistry('/_system/config');
        return configReg;
    };
    /**
     * The JSON conversion produces some properties which need to be accessed
     * using array indexes.This method converts these types of array references
     * to properties
     * @param  {[type]} obj [description]
     * @return {[type]}     [description]
     */
    var transformJSONLifecycle = function(obj) {
        obj.configuration = obj.configuration[0];
        obj.configuration.lifecycle = obj.configuration.lifecycle[0];
        obj.configuration.lifecycle.scxml = obj.configuration.lifecycle.scxml[0];
        var states = obj.configuration.lifecycle.scxml.state;
        var stateObj = {};
        var state;
        for (var index = 0; index < states.length; index++) {
            state = states[index];
            stateObj[state.id.toLowerCase()] = state;
            if (stateObj[state.id.toLowerCase()].datamodel) {
                stateObj[state.id.toLowerCase()].datamodel = stateObj[state.id.toLowerCase()].datamodel[0];
            }
        }
        obj.configuration.lifecycle.scxml.state = stateObj;
        return obj;
    };
    /*
    Creates an xml file from the contents of an Rxt file
    @rxtFile: An rxt file
    @return: An xml file
    */
    var createXml = function(content) {
        var fixedContent = content.replace('<xml version="1.0"?>', EMPTY).replace('</xml>', EMPTY);
        return new XML(fixedContent);
    };
    var parseLifeycle = function(content) {
        var ref = require('utils').xml;
        var obj = ref.convertE4XtoJSON(createXml(content));
        return obj;
    };
    var loadLifecycles = function(sysRegistry, tenantId) {
        var configReg = getConfigRegistry(sysRegistry);
        var lifecycleList = CommonUtil.getLifecycleList(configReg);
        var lifecycle;
        var content;
        for (var index in lifecycleList) {
            lifecycle = lifecycleList[index];
            log.debug('About to process raw lifecycle definition and convert to json for ' + lifecycle);
            //Obtain the definition 
            content = CommonUtil.getLifecycleConfiguration(lifecycle, configReg);
            //Store the raw lifecycle
            addRawLifecycle(lifecycle, content, tenantId);
            //Parse the raw lifecycle definition into a json
            var jsonLifecycle = parseLifeycle(new String(content));
            //Correct any array references
            jsonLifecycle = transformJSONLifecycle(jsonLifecycle);
            //Store the json lifecycle definition
            addJsonLifecycle(lifecycle, jsonLifecycle, tenantId);
            log.info('Found lifecycle: ' + jsonLifecycle.name + ' tenant: ' + tenantId);
        }
    };
    var init = function(tenantId) {
        var server = require('store').server;
        var sysRegistry = server.systemRegistry(tenantId);
        loadLifecycles(sysRegistry, tenantId);
    };
    core.force = function(tenantId) {
        init(tenantId);
    };
    core.init = function() {
        var event = require('event');
        event.on('tenantLoad', function(tenantId) {
            init(tenantId);
        });
    };
    core.configs = function(tenantId) {
        var lcMap = application.get(LC_MAP);
        if (!lcMap) {
            log.debug('Creating lcMap in the application context');
            lcMap = {};
            application.put(LC_MAP, lcMap);
        }
        if (!lcMap[tenantId]) {
            log.debug('Creating lcMap for the tenant: ' + tenantId + ' in application context');
            lcMap[tenantId] = {};
        }
        return lcMap[tenantId];
    };
    core.getRawDef = function(lifecycleName, tenantId) {
        var lcMap = core.configs(tenantId);
        if (!lcMap) {
            throw 'There is no lifecycle information for the tenant: ' + tenantId;
        }
        if (!lcMap.raw) {
            throw 'There is no raw lifecycle information for the lifecycle: ' + lifecycleName + ' of tenant: ' + tenantId;
        }
        if (!lcMap.raw[lifecycleName]) {
            throw 'There is no lifecycle information for ' + lifecycleName;
        }
        return lcMap.raw[lifecycleName];
    };
    core.getJSONDef = function(lifecycleName, tenantId) {
        var lcMap = core.configs(tenantId);
        if (!lcMap) {
            throw 'There is no lifecycle information for the tenant: ' + tenantId;
        }
        if (!lcMap.json) {
            throw 'There is no json lifecycle information for the lifecycle: ' + lifecycleName + ' of tenant: ' + tenantId;
        }
        if (!lcMap.json[lifecycleName]) {
            throw 'There is no lifecycle information for ' + lifecycleName;
        }
        return lcMap.json[lifecycleName];
    };
    core.getLifecycleList = function(tenantId){
        var lcMap = core.configs(tenantId);
        if (!lcMap) {
            throw 'There is no lifecycle information for the tenant: ' + tenantId;
        }
        if (!lcMap.json) {
            throw 'There is no json lifecycle information' + tenantId;
        }
        var map = lcMap.json;
        var list = [];
        for(var i in map){
            list.push(i);
        }
        return list;
    }

}(core));