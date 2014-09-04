var core = {};
(function(core) {
    var DEFAULT_MEDIA_TYPE = 'application/vnd.wso2.registry-ext-type+xml';
    var ASSET_PATH = '/_system/governance/repository/components/org.wso2.carbon.governance/types/';
    var RXT_MAP = 'rxt.manager.map';
    var EMPTY = '';
    var GovernanceUtils = Packages.org.wso2.carbon.governance.api.util.GovernanceUtils;
    var DEFAULT_TENANT = -1234;
    var utils = require('utils');
    var log = new Log('rxt.core');
    var applyDefinitionMutation = function(rxtDefinition, rxtMutation) {
        var mutatedTables = rxtMutation.table || {};
        var rxtTables = rxtDefinition.content.table;
        for (var tableName in mutatedTables) {
            //Check if the rxt table has a table name similar to the 
            if (rxtTables[tableName]) {
                rxtDefinition.content.table[tableName] = applyTableMutation(rxtTables[tableName], mutatedTables[tableName]);
            }
        }
        //Copy any non table mutations over to the rxtDefinition
        for (var key in rxtMutation) {
            if (key != 'table') {
                rxtDefinition[key] = rxtMutation[key];
            }
        }
        return rxtDefinition;
    };
    var applyTableMutation = function(rxtTable, rxtTableMutation) {
        var rxtFields = rxtTable.fields;
        var mutatedFields = rxtTableMutation.fields;
        for (var fieldName in mutatedFields) {
            if (rxtFields[fieldName]) {
                rxtTable.fields[fieldName] = applyFieldPropMutation(rxtFields, mutatedFields, fieldName);
            }
        }
        return rxtTable;
    };
    var applyFieldPropMutation = function(rxtField, mutatedField, fieldName) {
        rxtField = rxtField[fieldName];
        mutatedField = mutatedField[fieldName];
        if (!rxtField.validations) {
            rxtField.validations = [];
        }
        if (mutatedField.validation) {
            rxtField.validations.push(mutatedField.validation);
        }
        for (var propName in mutatedField) {
            if (propName != 'validation') {
                rxtField[propName] = mutatedField[propName];
            }
        }
        return rxtField;
    };
    var makeWordUpperCase = function(word) {
        if (word.length > 1) {
            return word[0].toUpperCase() + word.substring(1);
        }
        return word;
    };
    var createCamelCaseName = function(fieldName) {
        var comps = fieldName.split(' ');
        for (var index in comps) {
            comps[index] = comps[index].toLowerCase();
        }
        //Check if there is more than one element in the name
        if (comps.length > 1) {
            for (var index = 1; index < comps.length; index++) {
                //Get the first letter of the word and convert it to Uppercase
                comps[index] = makeWordUpperCase(comps[index]);
            }
        }
        return comps.join('');
    };
    var transformDefinition = function(rxtDefinition) {
        rxtDefinition.storagePath = rxtDefinition.storagePath[0].storagePath;
        rxtDefinition.content = rxtDefinition.content[0];
        var table;
        var tableName;
        var tableBlock = rxtDefinition.content.table;
        rxtDefinition.content.table = {};
        for (var index in tableBlock) {
            table = tableBlock[index];
            tableName = createCamelCaseName(table.name);
            rxtDefinition.content.table[tableName] = {};
            rxtDefinition.content.table[tableName] = table;
            transformTable(rxtDefinition.content.table[table.name], table);
        }
    };
    var transformTable = function(rxtDefinition, rxtTable) {
        var fields = rxtTable.field;
        var field;
        var name;
        rxtTable.fields = {};
        for (var index in fields) {
            field = fields[index];
            trasnformField(field)
            name = createCamelCaseName(field.name.name);
            rxtTable.fields[name] = field;
            //Determine if there is a label which has been defined 
            if (!field.name.label) {
                field.name.label = field.name.name;
            }
            field.name.name = name;
        }
        delete rxtTable.field;
    };
    var trasnformField = function(rxtField) {
        var nameBlock = rxtField.name;
        rxtField.name = {};
        rxtField.name = nameBlock[0];
    };

    function RxtManager(registry) {
        this.registry = registry;
        this.rxtMap = {};
        this.mutatorMap = {};
    }
    RxtManager.prototype.load = function() {
        var rxtPaths = GovernanceUtils.findGovernanceArtifacts(DEFAULT_MEDIA_TYPE, this.registry.registry);
        var content;
        var rxtDefinition;
        log.info('RXT paths: ' + stringify(rxtPaths));
        for (var index in rxtPaths) {
            try {
                content = this.registry.get(rxtPaths[index]);
                rxtDefinition = utils.xml.convertE4XtoJSON(createXml(content));
                transformDefinition(rxtDefinition);
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
    RxtManager.prototype.listRxtTypeDetails = function() {
        var list = [];
        for (var type in this.rxtMap) {
            var details = {};
            var template = this.rxtMap[type];
            details.shortName = type;
            details.singularLabel = template.singularLabel;
            details.pluralLabel = template.pluralLabel;
            details.ui = (template.meta) ? (template.meta.ui || {}) : {};
            details.lifecycle = (template.meta) ? (template.meta.lifecycle || {}) : {};
            list.push(details);
        }
        return list;
    };
    RxtManager.prototype.applyMutator = function(type, mutator) {
        this.mutatorMap[type] = {};
        this.mutatorMap[type] = mutator;
        var rxtDefinition = this.rxtMap[type];
        this.rxtMap[type] = applyDefinitionMutation(rxtDefinition, mutator);
    };
    RxtManager.prototype.listRxtTypeTables = function(type) {
        var rxtDefinition = this.rxtMap[type];
        if (!rxtDefinition) {
            log.error('Unable to locate the rxt definition for type: ' + type + ' in order to return tables');
            throw 'Unable to locate the rxt definition for type: ' + type + ' in order to return tables';
        }
        var tables = [];
        var table;
        for (var key in rxtDefinition.content.table) {
            table = parse(stringify(rxtDefinition.content.table[key]));
            var temp = table.name;
            table.label = temp;
            table.name = key;
            tables.push(table);
        }
        return tables;
    };
    RxtManager.prototype.getNameAttribute = function(type) {
        var rxtDefinition = this.rxtMap[type];
        if (!rxtDefinition) {
            log.error('Unable to locate the rxt definition for type: ' + type + ' in order to return name attribute');
            throw 'Unable to locate the rxt definition for type: ' + type + ' in order to return tables';
        }
        if ((rxtDefinition.nameAttribute) && (rxtDefinition.nameAttribute.length > 0)) {
            return rxtDefinition.nameAttribute[0].nameAttribute;
        }
        log.warn('Unable to locate the name attribute for type: ' + type + '.Check if a nameAttribute is specified in the rxt definition.');
        return '';
    };
    RxtManager.prototype.getThumbnailAttribute = function(type) {
        var rxtDefinition = this.rxtMap[type];
        if (!rxtDefinition) {
            log.error('Unable to locate the rxt definition for type: ' + type + ' in order to return thumbail attribute');
            throw 'Unable to locate the rxt definition for type: ' + type + ' in order to return thumbail attribute';
        }
        if ((rxtDefinition.meta) && (rxtDefinition.meta.thumbnail)) {
            return rxtDefinition.meta.thumbnail;
        }
        log.warn('Unable to locate thumbnail attribute for type: ' + type + '.Check if a thumbnail property is defined in the rxt configuration.');
        return '';
    };
    RxtManager.prototype.getLifecycleName = function(type) {
        var rxtDefinition = this.rxtMap[type];
        if (!rxtDefinition) {
            log.error('Unable to locate the rxt definition for type: ' + type + ' in order to return lifecycle ');
            throw 'Unable to locate the rxt definition for type: ' + type + ' in order to return lifecycle ';
        }
        if ((rxtDefinition.meta) && (rxtDefinition.meta.lifecycle)) {
            return rxtDefinition.meta.lifecycle.name || '';
        }
        log.warn('Unable to locate a meta property in order retrieve default lifecycle name for ' + type);
        return '';
    };
    RxtManager.prototype.getDefaultLcAction = function(type) {
        var rxtDefinition = this.rxtMap[type];
        if (!rxtDefinition) {
            log.error('Unable to locate the rxt definition for type: ' + type);
            throw 'Unable to locate the rxt definition for type: ' + type + ' in order to return the default lifecycle action.';
        }
        if ((rxtDefinition.meta) && (rxtDefinition.meta.lifecycle)) {
            return rxtDefinition.meta.lifecycle.defaultAction || '';
        }
        log.warn('Unable to locate a meta property in order retrieve default lifecycle action for ' + type + '.Make sure the lifecycle meta property is present in the configuratio callback of the asset.js');
        return '';
    };
    /**
     * The function checks whether an asset type requires comments when changing state.If the user has not configured
     * this property then it will return false, meaning comments will not be required when changing the lifecycle state
     * @param  {[type]}  type The type of the asset
     * @return {Boolean}      True if comments are required
     */
    RxtManager.prototype.isLCCommentRequired = function(type) {
        var rxtDefinition = this.rxtMap[type];
        if (!rxtDefinition) {
            log.error('Unable to locate the rxt definition for type: ' + type);
            throw 'Unable to locate the rxt definition for type: ' + type + ' in order to determine if lifecycle comments are required';
        }
        if ((rxtDefinition.meta) && (rxtDefinition.meta.lifecycle)) {
            return rxtDefinition.meta.lifecycle.commentRequired || false;
        }
        log.warn('Unable to locate the lifecycle meta property to determine whether comments are required ' + type + '.Make sure the lifecycle meta property is present in the configuratio callback of the asset.js');
        return false;
    };
    /**
     * The function will retrieve all fields of the provided field type
     * @param  {[type]} type      The Rxt type
     * @param  {[type]} fieldType [description]
     * @return {[type]}           An array of fields qualified by the table name
     */
    RxtManager.prototype.listRxtFieldsOfType = function(type, fieldType) {
        var tables = this.listRxtTypeTables(type);
        if (tables.length == 0) {
            log.warn('The rxt definition for ' + type + ' does not have any tables.');
            return [];
        }
        var table;
        var field;
        var result = [];
        //Go through all of the tables
        for (var key in tables) {
            table = tables[key];
            for (var fieldName in table.fields) {
                field = table.fields[fieldName];
                if (field.type == fieldType) {
                    result.push(table.name + '_' + fieldName);
                }
            }
        }
        return result;
    };
    /**
     * The function returns an array of states in which an asset can be deleted
     * @param  {[type]} type The rxt type
     * @return {[type]}      An array of states in which an rxt instance can be deleted
     */
    RxtManager.prototype.getDeletableStates = function(type) {
        var rxtDefinition = this.rxtMap[type];
        var deletableStates = [];
        if (!rxtDefinition) {
            log.error('Unable to locate the rxt definition for type: ' + type);
            throw 'Unable to locate the rxt definition for type: ' + type + ' in order to return the deletable states.';
        }
        if (!rxtDefinition.meta) {
            log.warn('Unable to locate meta information in the rxt definition for type: ' + type + '.Cannot fetch deletable states.');
            return deletableStates;
        }
        if (!rxtDefinition.meta.lifecycle) {
            log.warn('Unable to locate lifecycle information in the rxt definition for type ' + type + '.Cannot fetch lifecycle data.');
            return deletableStates;
        }
        if (!rxtDefinition.meta.lifecycle.deletableStates) {
            log.warn('No deletable states have been defined for the rxt definition of type: ' + type + '.');
            return deletableStates;
        }
        deletableStates = rxtDefinition.meta.lifecycle.deletableStates;
        return deletableStates;
    };
    /**
     * The function indicates a set of states in which an asset is deemed to be
     * in the published state.An asset in the published state is visible in the Store
     * @param  {[type]} type The type of the asset
     * @return {[type]}      An array of strings indicating the set of published states
     */
    RxtManager.prototype.getPublishedStates = function(type) {
        var rxtDefinition = this.rxtMap[type];
        var publishedStates = [];
        if (!rxtDefinition) {
            log.error('Unable to locate the rxt definition for type: ' + type);
            throw 'Unable to locate the rxt definition for type: ' + type + ' in order to return the published states.';
        }
        if (!rxtDefinition.meta) {
            log.warn('Unable to locate meta information in the rxt definition for type: ' + type + '.Cannot fetch published states.');
            return publishedStates;
        }
        if (!rxtDefinition.meta.lifecycle) {
            log.warn('Unable to locate lifecycle information in the rxt definition for type ' + type + '.Cannot fetch lifecycle data.');
            return publishedStates;
        }
        if (!rxtDefinition.meta.lifecycle.publishedStates) {
            log.warn('No published states have been defined for the rxt definition of type: ' + type + '.');
            return publishedStates;
        }
        publishedStates = rxtDefinition.meta.lifecycle.publishedStates;
        return publishedStates;
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
            configs = createTenantRxtMap(tenantId, rxtMap); //TODO:Check if this is map or rxtMap
        }
        return configs;
    };
    core.rxtManager = function(tenantId) {
        var map = application.get(RXT_MAP);
        if (!map) {
            throw 'rxt map was not found in the application object';
        }
        var manager = map[tenantId].rxtManager;
        if (!manager) {
            log.debug('Creating a new rxt manager');
            manager = createRxtManager(tenantId, map);
            map[tenantId].rxtManager = manager;
        }
        return manager;
    };
    core.assetResources = function(tenantId, type) {
        var configs = core.configs(tenantId);
        var assetResource = configs.assetResources[type];
        if (!assetResource) {
            log.error('Unable to locate assetResources for tenant: ' + tenantId + ' and type: ' + type);
            throw 'Unable to locate assetResources for tenant: ' + tenantId + ' and type: ' + type;
        }
        return assetResource;
    };
    core.init = function() {
        var event = require('event');
        var server = require('store').server;
        var options = server.options();
        var map = {};
        application.put(RXT_MAP, map);
        event.on('tenantLoad', function(tenantId) {
            map = application.get(RXT_MAP);
            createTenantRxtMap(tenantId, map);
            createRxtManager(tenantId, map);
        });
    };
    core.createAssetContext = function(session, type) {
        var user = require('store').user;
        var server = require('store').server;
        var userDetails = server.current(session);
        //If there is no user then build an anonymous registry for the super tenant
        if (!userDetails) {
            log.debug('Obtaining anon asset context for ' + type);
            return this.createAnonAssetContext(session, type);
        } else {
            return this.createUserAssetContext(session, type);
        }
    };
    core.createAnonAssetContext = function(session, type) {
        var server = require('store').server;
        var user = require('store').user;
        var tenantId = DEFAULT_TENANT;
        var sysRegistry = server.anonRegistry(tenantId);
        var userManager = server.userManager(tenantId);
        var tenatConfigs = user.configs(tenantId);
        var serverConfigs = server.configs(tenantId);
        var rxtManager = core.rxtManager(tenantId);
        var username = "wso2.anonymous";
        return {
            username: username,
            userManager: userManager,
            username: username,
            tenantId: tenantId,
            systemRegistry: sysRegistry,
            assetType: type,
            rxtManager: rxtManager,
            tenantConfigs: tenatConfigs,
            serverConfigs: serverConfigs,
            isAnonContext:true
        };
    };
    core.createUserAssetContext = function(session, type) {
        var user = require('store').user;
        var server = require('store').server;
        var userDetails = server.current(session);
        var tenantId = userDetails.tenantId;
        var sysRegistry = server.systemRegistry(tenantId);
        var userManager = server.userManager(tenantId);
        var tenatConfigs = user.configs(tenantId);
        var serverConfigs = server.configs(tenantId);
        var username = server.current(session).username;
        var rxtManager = core.rxtManager(tenantId);
        return {
            username: username,
            userManager: userManager,
            username: username,
            tenantId: tenantId,
            systemRegistry: sysRegistry,
            assetType: type,
            rxtManager: rxtManager,
            tenantConfigs: tenatConfigs,
            serverConfigs: serverConfigs,
            isAnonContext:false
        };
    };
}(core));