var core = {};
(function(core) {
    var DEFAULT_MEDIA_TYPE = 'application/vnd.wso2.registry-ext-type+xml';
    var ASSET_PATH = '/_system/governance/repository/components/org.wso2.carbon.governance/types/';
    var RXT_MAP = 'rxt.manager.map';
    var EMPTY = '';
    var GovernanceUtils = Packages.org.wso2.carbon.governance.api.util.GovernanceUtils;
    var utils = require('utils');
    var log = new Log('rxt.core');

    function RxtManager(registry) {
        this.registry = registry;
        this.rxtMap = {};
    }
    RxtManager.prototype.load = function() {
        var rxtPaths = GovernanceUtils.findGovernanceArtifacts(DEFAULT_MEDIA_TYPE, this.registry.registry);
        var content;
        var rxtDefinition;
        for (var index in rxtPaths) {
            try {
                content = this.registry.get(rxtPaths[index]);
                rxtDefinition = utils.xml.convertE4XtoJSON(createXml(content));
                this.rxtMap[rxtDefinition.shortName] = rxtDefinition;
            } catch (e) {
                log.debug('Unable to load RXT definition for : ' + rxtPaths[index] + '.The following exception occured: ' + e);
            }
        }
    };
    RxtManager.prototype.getRxtDefinition = function(rxtType) {
        if (this.rxtMap[rxtType]) {
            return this.rxtMap[rxtType];
        }
        throw "Unable to locate rxt type: " + rxtType;
    };
    RxtManager.prototype.getRxtStoragePath = function(rxtType) {
        var def = this.rxtMap[rxtType];
        if (!def) {
            log.debug('Unable to locate rxt definition for ' + rxtType);
            return '';
        }
        var pathItem = def.storagePath[0];
        if (!pathItem) {
            log.error('Unable to locate stoarge path of ' + rxtType + '.Check the rxt definition to see if the storage path is specified correctly');
            throw 'Unable to locate stoarge path of ' + rxtType;
        }
        return pathItem.storagePath;
    };
    RxtManager.prototype.listRxtTypes = function() {
        var list = [];
        for (var type in this.rxtMap) {
            list.push(type);
        }
        return list;
    };
    /*
    Creates an xml file from the contents of an Rxt file
    @rxtFile: An rxt file
    @return: An xml file
    */
    var createXml = function(rxtFile) {
        var content = rxtFile.content.toString();
        var fixedContent = content.replace('<xml version="1.0"?>', EMPTY).replace('</xml>', EMPTY);
        return new XML(fixedContent);
    }
    var createRxtManager = function(tenantId, map) {
        var server = require('store').server;
        var sysRegistry = server.systemRegistry(tenantId);
        map.rxtManager = new RxtManager(sysRegistry)
        map.rxtManager.load();
        return map.rxtManager;
    };
    var createTenantRxtMap = function(tenantId, map) {
        map[tenantId] = {};
        return map[tenantId];
    };
    core.configs = function(tenantId) {
        var rxtMap = application.get(RXT_MAP);
        var configs = rxtMap[tenantId];
        if (!configs) {
            configs=createTenantRxtMap(tenantId, map);
        }
        return configs;
    };
    core.manager = function(tenantId) {
        var map = application.get(RXT_MAP);
        if (!map) {
            throw 'rxt map was not found in the application object';
        }
        var manager = map[tenantId].rxtManager;
        if (!manager) {
            manager=createRxtManager(tenantId, map);
        }
        return manager;
    };
    core.init = function() {
        var event = require('event');
        var server = require('store').server;
        var options = server.options();
        var map = {};
        application.put(RXT_MAP, map);
        log.info('### Initializing the rxt core for the super tenant ###');
        event.on('tenantLoad', function(tenantId) {
            log.info('### Initializing the rxt core for tenant: ' + tenantId + ' ###');
            map = application.get(RXT_MAP);
            createTenantRxtMap(tenantId, map);
            createRxtManager(tenantId, map);
            log.info('### Finished initializing rxt core ###');
        });
    };
}(core));