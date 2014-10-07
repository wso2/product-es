/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
/**
 * The core namespace contains methods and classes that are used to work with the raw RXT definitions as well
 * building context objects
 * @namespace
 * @example
 *     var core = require('rxt').core;
 *     core.init();
 * @requires event
 * @requires utils
 * @requires store
 * @requires Packages.org.wso2.carbon.governance.api.util.GovernanceUtils
 */
var core = {};
(function(core, constants) {
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
            transformTable(rxtDefinition.content.table[table.name], table, tableName);
        }
    };
    var transformTable = function(rxtDefinition, rxtTable, tableName) {
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
            field.name.fullName = tableName + '_' + name;
        }
        delete rxtTable.field;
    };
    var trasnformField = function(rxtField) {
        var nameBlock = rxtField.name;
        rxtField.name = {};
        rxtField.name = nameBlock[0];
    };
    /**
     * Represents an interface for managing interactions with the different
     * RXT types deployed in the Governance Registry
     * @constructor
     * @param {Object} registry Carbon Registry instance
     * @memberOf core
     */
    function RxtManager(registry) {
        this.registry = registry;
        this.rxtMap = {};
        this.mutatorMap = {};
    }
    /**
     * Loads the RXT definition files from the Governance Registry and converts the definitions to JSON.RXT definitions
     * are read as XML files which are then converted to a JSON representation.
     */
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
    /**
     * Returns the JSON representation for a given RXT type.The RXT type is the shortName property defined in the
     * RXT definition file.
     * @example
     *     var rxtDef=rxtManager.getRxtDefinition('gadget');
     * @param  {String} rxtType The RXT type
     * @return {Object}         The RXT definition as a JSON
     * @throws Unable to locate rxt type
     */
    RxtManager.prototype.getRxtDefinition = function(rxtType) {
        if (this.rxtMap[rxtType]) {
            return this.rxtMap[rxtType];
        }
        throw "Unable to locate rxt type: " + rxtType;
    };
    /**
     * Returns the storage path of assets of a given RXT type.The value returned is the storagePath property
     * defined in the RXT definition file.The path returned may either contain one or more dynamic values refererncing
     * attributes defined in the RXT.
     * If the RXT definition is not located then an empty string is returned
     * @todo Change the value which is returned when there is no definition
     * @example
     *     /_system/governance/gadgets/{overview_provider}
     * @param  {String} rxtType The RXT type
     * @return {String}         The Registry storage path
     * @throws Unable to locate storage path
     */
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
    /**
     * Returns a list of all RXT types in the Governance registry
     * @example
     *     var list=rxtManager.listRxtTypes();
     *     print(list); // ['gadget','site','ebook']
     * @return {Array} An array of strings that represent the RXT type names
     */
    RxtManager.prototype.listRxtTypes = function() {
        var list = [];
        for (var type in this.rxtMap) {
            list.push(type);
        }
        return list;
    };
    /**
     * Returns meta information about either the provided RXT type or all of the RXTs in the governance registry.The object
     * returned is similar to the result returned by the @see getRxtTypeDetails
     * @example
     *     //Retrieving details about a single RXT type
     *     var rxt = rxtManager.listRxtTypeDetails('gadget');
     *
     *     //Retrieving details about all RXTs
     *     var rxts = rxtManager.listRxtTypeDetails();
     *
     * @param  {String} rxtType The RXT type name (Optional)
     * @see listRxtTypeDetails
     * @return {Object} Method returns either an object or an array.If a type is given when invoking then a single object is returned,
     *                         else an array
     */
    RxtManager.prototype.listRxtTypeDetails = function() {
        if (arguments.length == 1) {
            return this.getRxtTypeDetails(arguments[0]);
        } else {
            return this.getRxtTypesDetails();
        }
    };
    /**
     * Returns the meta information of a single RXT type.
     * @example
     *     var rxt=getRxtTypeDetails('gadget');
     *     print(rxt.shortName);    //gadget
     *     print(rxt.singularLabel);//Gadget
     *     print(rxt.pluralLabel);  //Gadgets
     *     print(rxt.ui);
     *     print(rxt.lifecycle)
     * @param  {String} type The type of the RXT
     * @return {Object}      A json object with a set of specific RXT meta data
     * @throws Unable to locate the rxt definition for type in order to return rxt details
     */
    RxtManager.prototype.getRxtTypeDetails = function(type) {
        var template = this.rxtMap[type];
        if (!template) {
            log.error('Unable to locate the rxt definition for type: ' + type + ' in order to return rxt details');
            throw 'Unable to locate the rxt definition for type: ' + type + ' in order to return rxt details';
        }
        var details = {};
        details.shortName = type;
        details.singularLabel = template.singularLabel;
        details.pluralLabel = template.pluralLabel;
        details.ui = (template.meta) ? (template.meta.ui || {}) : {};
        details.lifecycle = (template.meta) ? (template.meta.lifecycle || {}) : {};
        return details;
    };
    /**
     * @todo This method should use the getRxtTypeDetails method
     * @return {Array} A list of RXT meta data
     */
    RxtManager.prototype.getRxtTypesDetails = function() {
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
    /**
     * Returns a list of table elements defined for a RXT type definition
     * @example
     *     var tables = rxtManager.listRxtTypeTables('gadget');
     *     for(var i = 0; i<tables.length; i++){
     *         print(tables[0].name);
     *         print(tables[0].label);
     *     }
     * @param  {String} type The RXT name
     * @return {Array}      A array containing the name and label pairs of all the tables
     * @throws Unable to locate the rxt definition for type in order to return tables
     */
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
    /**
     * Returns the name of the attribute that is used as the name property of assets of a given RXT type
     * @example
     *     //Refer to the gadget.rxt
     *     var field =  rxtManager.getNameAttribute('gadget');
     *     print(field);    // overview_name
     * @todo: The name field is mandatory so this method should throw an exception if one is not found
     * @param  {String} type The RXT type
     * @return {String}      The name of a field defined in the RXT definition
     * @throws Unable to locate the rxt definition for type in order to return tables
     */
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
    /**
     * Returns the name of the attribute that is used as the thumbnail property of assets of a given RXT type
     * Some RXTs might not have a thumbnail property in which case an empty string is returned
     * Note: This property is specific to the ES and is defined in the configuration callback
     * @param  {String} type The RXT type
     * @return {String}      The field name defined as the thumbnail property
     * @throws Unable to locate the rxt definition for type in order to return the thumbnail attribute
     */
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
    /**
     * Returns the attribute that is used as the banner property of assets of a given RXT type
     * Note: This property is specific to the ES and is defined in the configuration callback
     * @param  {String} type The RXT type
     * @return {String}      The field name defined as the banner property
     * @throws Unable to locate the rxt definition for type in order to return the banner attribute
     */
    RxtManager.prototype.getBannerAttribute = function(type) {
        var rxtDefinition = this.rxtMap[type];
        if (!rxtDefinition) {
            log.error('Unable to locate the rxt definition for type: ' + type + ' in order to return banner attribute');
            throw 'Unable to locate the rxt definition for type: ' + type + ' in order to return banner attribute';
        }
        if ((rxtDefinition.meta) && (rxtDefinition.meta.banner)) {
            return rxtDefinition.meta.banner;
        }
        log.warn('Unable to locate banner attribute for type: ' + type + '.Check if a banner property is defined in the rxt configuration.');
        return '';
    };
    /**
     * Returns the attribute that is used to track the temporal behaviour of assets of a given RXT type
     * Note: This property is specific to the ES and is defined in the configuration callback
     * @param  {String} type The RXT type
     * @return {String}      The field name deifned as the temporal property
     * @throws Unable to locate the rxt definition for type in order to return the timestamp attribute
     */
    RxtManager.prototype.getTimeStampAttribute = function(type) {
        var rxtDefinition = this.rxtMap[type];
        if (!rxtDefinition) {
            log.error('Unable to locate the rxt definition for type: ' + type + ' in order to return timestamp attribute');
            throw 'Unable to locate the rxt definition for type: ' + type + ' in order to return timestamp attribute';
        }
        if ((rxtDefinition.meta) && (rxtDefinition.meta.timestamp)) {
            return rxtDefinition.meta.timestamp;
        }
        log.warn('Unable to locate timestamp attribute for type: ' + type + '.Check if a timestamp property is defined in the rxt configuration.');
        return null;
    };
    /**
     * Returns the name of the lifecycle that is attached to assets of a given RXT type
     * If no lifecycle is specified then an empty string is returned.
     * Note: This property is specific to the ES and is defined in the configuration callback
     * @param  {String} type  The RXT type
     * @return {String}      The name of a lifecycle which is attached to all asset instances of an RXT type
     * @throws Unable to locate the rxt definition for type in order to return lifecycle
     */
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
    /**
     * Returns the action that is invoked when a lifecycle is first attached to an asset of a given RXT type.
     * Note: This property is specific to the ES and is defined in the configuration callback
     * @param  {String} type The RXT type
     * @return {String}      The lifecycle action to be invoked after attaching a lifecycle to an asset
     */
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
     * Checks whether an asset type requires comments when changing state.If the user has not configured
     * this property then it will return false, meaning comments will not be required when changing the lifecycle state
     * @param  {String}  type The type of the asset
     * @return {Boolean}      True if comments are required
     * @throws Unable to locate the rxt definiton for type in order to determine if lifecycle comments are required
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
     * Returns all fields that match field type provided in the RXT definition
     * @example
     *     var fields = rxtManager.listRxtFieldsOfType('gadget','file');
     *     print( fields ); // ['images_thumbnail', 'images_banner'];
     * @param  {String} type      The RXT type
     * @param  {String} fieldType The field type
     * @return {Array}           An array of fields qualified by the table name
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
    RxtManager.prototype.listRxtFields = function(type) {
        var tables = this.listRxtTypeTables(type);
        var fields = [];
        if (tables.length == 0) {
            log.warn('Unable to return list of rxt fields for type: ' + type + ' as tables were defined in the rxt definition');
            return fields;
        }
        var table;
        var field;
        for (var key in tables) {
            table = tables[key];
            for (var fieldName in table.fields) {
                field = table.fields[fieldName];
                fields.push(field);
            }
        }
        return fields;
    };
    /**
     * Returns an array of states in which an asset can be deleted
     * Note: This property is specific to the ES and is defined in the configuration callback
     * @example
     *     var states =  rxtManager.getDeletableStates('gadget');
     *     print(states); // ['Deleted']
     * @param  {String} type The RXT type
     * @return {Array}      An array of states in which an rxt instance can be deleted
     * @throws Unable to locate the rxt definition for type in order to return the deletable states
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
     * Returns a set of states in which an asset is deemed to be
     * in the published state.An asset in the published state is visible in the Store
     * Note: This property is specific to the ES and is defined in the configuration callback
     * @param  {String} type The RXT type
     * @return {Array}       An array of strings indicating the set of published states
     * @throws Unable to locate the rxt definition for type in order to return the published states
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
    /**
     * Return the field which is designated as the category field.If a category field
     * has not been defined by the user then null will be returned
     * Note: This property is specific to the ES and is defined in the configuration callback
     * @param  {String} type The RXT type
     * @return {String}      The field name which acts as the category field for the RXT type
     * @throws Unable to locate the rxt definition for type in order to return the category field
     */
    RxtManager.prototype.getCategoryField = function(type) {
        var rxtDefinition = this.rxtMap[type];
        var categoryField = null;
        if (!rxtDefinition) {
            log.error('Unable to locate the rxt definition for type: ' + type);
            throw 'Unable to locate the rxt definition for type: ' + type + ' in order to return the category field.';
        }
        if (!rxtDefinition.meta) {
            log.warn('Unable to locate meta information in the rxt definition for type: ' + type + '.Cannot fetch category field');
            return categoryField;
        }
        if (!rxtDefinition.meta.categories) {
            log.warn('Unable to locate category information in the rxt definition for type: ' + type + '.Cannot fetch category field');
            return categoryField;
        }
        if (!rxtDefinition.meta.categories.categoryField) {
            log.warn('No category details have been defined for the rxt definition of type: ' + type + '.');
            return categoryField;
        }
        categoryField = rxtDefinition.meta.categories.categoryField;
        return categoryField;
    };
    /**
     * Returns an array of fields that can be used to search for asset instances of an RXT type
     * If the 'all' value is returned then it can be assumed that fields of the RXT should be searchable
     * Note: This property is specific to the ES and is defined in the configuration callback
     * @param  {String} type  The RXT type
     * @return {Array}        The list of field names that can be used to search the asset
     * @throws Unable to locate the rxt definition for type in order to return the searchable fields
     */
    RxtManager.prototype.getSearchableFields = function(type) {
        var rxtDefinition = this.rxtMap[type];
        var searchableFields = [];
        if (!rxtDefinition) {
            log.error('Unable to locate the rxt definition for type: ' + type);
            throw 'Unable to locate the rxt definition for type: ' + type + ' in order to return the searchable fields';
        }
        if (!rxtDefinition.meta) {
            log.warn('Unable to locate meta information in the rxt definition for type: ' + type + '.Cannot fetch searchable fields');
            return searchableFields;
        }
        if (!rxtDefinition.meta.search) {
            log.warn('Unable to locate search information in the rxt definition for type: ' + type + '.Cannot fetch searchable fields');
            return searchableFields;
        }
        if (!rxtDefinition.meta.search.searchableFields) {
            log.warn('No searchable fields defined in the rxt definition for type: ' + type + '.Cannot fetch searchable fields');
            return searchableFields;
        }
        searchableFields = rxtDefinition.meta.search.searchableFields;
        return searchableFields;
    };
    /**
     * Returns the field definition for a given field.If the definition is not found then null is returned
     * @example
     *     var field =  rxtManager.getRxtField('gadget','overview_name');
     * @param  {String} type      The RXT type
     * @param {String} [name]     The name of the field to be obtained as a {tableName}_{fieldName}
     * @return {Object}            A JSON object defining the field
     * @throws Unable to locate the rxt definition for type in order to retrieve field data 
     */
    RxtManager.prototype.getRxtField = function(type, name) {
        var template = this.rxtMap[type];
        var tableName;
        var fieldName;
        var components = getFieldNameParts(name);
        tableName = components.tableName;
        fieldName = components.fieldName;
        //Convert the table and field names to lowercase
        tableName = tableName ? tableName.toLowerCase() : '';
        fieldName = fieldName ? fieldName.toLowerCase() : '';
        var field = null;
        if (!template) {
            log.error('Unable to locate the rxt definition for type: ' + type);
            throw 'Unable to locate the rxt definition for type: ' + type + ' in order to retrieve field data for ' + tableName + '_' + fieldName;
        }
        if ((!template.content) && (!template.content.table)) {
            log.warn('Content or table definition was not found in the rxt definition of type: ' + type + '.Cannot fetch field data for ' + tableName + '_' + fieldName);
            return field;
        }
        var table = getRxtTable(template.content.table, tableName);
        if (!table) {
            log.warn('Unable to locate table definition : ' + tableName + ' in rxt definition of type: ' + type + '.Cannot fetch field data for ' + tableName + '_' + fieldName);
            return field;
        }
        field = getRxtField(table, fieldName);
        return field;
    };
    /**
     * Returns the RXT field value for a given field.This method internally invokes the @getRxtField method
     * to retrieve the field definition.If the field is not found then an ampty array is returned
     * @example 
     *     var value = rxtManager.getRxtFieldValue('gadget','overview_name');
     * @param  {String} type The RXT type
     * @param  {String} name The name of the field to be obtained as a {tableName}_{fieldName}
     * @return {Array}       The list of default field values
     */
    RxtManager.prototype.getRxtFieldValue = function(type, name) {
        var field = this.getRxtField(type, name);
        var values = [];
        if (!field) {
            log.warn('Unable to locate values for field ' + name + ' as the field was not located in the rxt definition');
            return values;
        }
        var fieldValuesObject = field.values || {};
        var fieldValueItems = fieldValuesObject[0] ? fieldValuesObject[0].value : [];
        for (var index in fieldValueItems) {
            values.push(fieldValueItems[index].value);
        }
        return values;
    };
    var getFieldNameParts = function(fieldName) {
        //Break the field by the _
        var components = fieldName.split('_');
        return {
            tableName: components[0],
            fieldName: components[1]
        };
    };
    var getRxtTable = function(tables, tableName) {
        var table = null;
        for (var key in tables) {
            if (key == tableName) {
                table = tables[key];
                return table;
            }
        }
        return table;
    };
    var getRxtField = function(table, fieldName) {
        var field = null;
        for (var key in table.fields) {
            if (key == fieldName) {
                field = table.fields[key];
                return field;
            }
        }
        return field;
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
    /**
     * Returns the object that is used to maintain the RxtManager map in the application context
     * @param  {Number} tenantId  The tenant ID
     * @return {Object}          A map of RxtManager instances 
     */
    core.configs = function(tenantId) {
        var rxtMap = application.get(RXT_MAP);
        var configs = rxtMap[tenantId];
        if (!configs) {
            configs = createTenantRxtMap(tenantId, rxtMap); 
        }
        return configs;
    };
    /**
     * Returns an instance of an RxtManager based on the tenant ID.If an RxtManager instance is
     * not present for a given tenant it is created when accessing for the first time
     * @param  {Number} tenantId The tenant ID
     * @return {Object}          An RxtManager instance
     * @throws rxt map was not found in the application object
     */
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
    /**
     * Returns the map of asset specific resources based on the tenant and asset type.
     * These resources include th
     * @example
     *     var resources =  core.assetResources(-1234,'gadget');
     * @param  {Number} tenantId The tenant ID
     * @param  {String} type     The asset type 
     * @return {Object}          The asset specific resources
     * @throws Unable to locate assetResources for tenant and type
     */
    core.assetResources = function(tenantId, type) {
        var configs = core.configs(tenantId);
        var assetResource = configs.assetResources[type];
        if (!assetResource) {
            log.error('Unable to locate assetResources for tenant: ' + tenantId + ' and type: ' + type);
            throw 'Unable to locate assetResources for tenant: ' + tenantId + ' and type: ' + type;
        }
        return assetResource;
    };
    /**
     * Returns the map of app specific resources based on the tenant 
     * @param  {Number} tenantId The tenant ID
     * @return {Object}          The app specific resources
     */
    core.appResources = function(tenantId) {
        var configs = core.configs(tenantId);
        var appResources = configs.appResources;
        if (!appResources) {
            log.error('Unable to locate appResources from tenant ' + tenantId);
            throw 'Unable to locate appResources for tenant: ' + tenantId;
        }
        return appResources;
    };
    /**
     * Returns an endpoint qualified by the asset type and base extension url
     * @param  {String} type     The asset type
     * @param  {String} endpoint The endpoint pattern
     * @return {String}          The endpoint url
     */
    core.getAssetPageUrl = function(type, endpoint) {
        return this.getAssetPageBaseUrl() + type + endpoint;
    };
    /**
     * Returns the base url of all asset pages
     * @return {String} The base url
     */
    core.getAssetPageBaseUrl = function() {
        return constants.ASSET_BASE_URL;
    };
    /**
     * Returns the api url given the api endpoint and asset type
     * @param  {String} type     The asset type
     * @param  {String} endpoint The api endpoint to be resolved
     * @return {String}          The api endpoint qualified by asset extension api base url
     */
    core.getAssetApiUrl = function(type, endpoint) {
        return this.getAssetApiBaseUrl() + endpoint + '?type=' + type;
    };
    /**
     * Returns the base url for asset extension apis
     * @return {String}  The base url of asset apis
     */
    core.getAssetApiBaseUrl = function() {
        return constants.ASSET_API_URL;
    };
    /**
     * Returns an application page url qualified by the base url of app pages
     * @param  {String} endpoint A page endpoint
     * @return {String}     The app page url qualified by the vase url of the app pages         
     */
    core.getAppPageUrl = function(endpoint) {
        return this.getAppPageBaseUrl() + endpoint;
    };
    /**
     * Returns the base url of app pages
     * @return {String} Base url of the app pages
     */
    core.getAppPageBaseUrl = function() {
        return constants.APP_PAGE_URL;
    }
    /**
     * Returns an application api url qualified by the base url of the app apis
     * @param  {String} endpoint The api endpoint
     * @return {String}          The app api url qualified by the base url of the app api
     */
    core.getAppApiUrl = function(endpoint) {
        return this.getAppApiBaseUrl() + endpoint;
    };
    core.getAppApiBaseUrl = function() {
        return constants.APP_API_URL;
    };
    core.getAssetSubscriptionSpace = function(type) {
        return constants.SUBSCRIPTIONS_PATH + (type ? '/' + type : '');
    };
    /**
     * Initializes the logic which loads the RXT definitions and creates the RxtManagers
     */
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
    /**
     * Returns a context object that provides access to asset and server information
     * This method will determine if there is a logged in user and delegate the creation of
     * the context object to either @see createUserAssetContext or the @see createAnonAssetContext.
     * The context object that is created will indicate if it is anonymous context.
     * The context can be used in pages which are type specific such as a listing page for gadgets.
     * @example
     *     var ctx = core.createAssetContext(session,'gadget');
     *
     *     ctx.username         //The currently logged in user if one is present
     *     ctx.userManager      //A carbon module User Manager instance
     *     ctx.tenantId         //The tenant ID of the currently logged in user
     *     ctx.systemRegistry   //A carbon module registry instance
     *     ctx.assetType        //The asset type for which the context was created
     *     ctx.rxtManager       //An RxtManager instance
     *     ctx.tenantConfigs    //The tenant configurations
     *     ctx.serverConfigs    //The servcer configurations
     *     ctx.isAnonContext    //True if the current context is annoymous
     *     ctx.session          //The session used to create the context
     *
     * @param  {Object} session  Jaggery session object
     * @param  {String} type     The type of asset
     * @return {Object}          An object which acts as bag for properties and classes
     */
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
    /**
     * Returns an annoymous context object that conforms to contract outlines in the @see createAssetContext.
     * This method should be used to create a context for pages that can be accessed without logging in.
     * The username will be set to wso2.anonymouse
     * The tenantId will be set to the super tenant
     * The context can be used in pages which are type specific such as a listing page for gadgets.
     * @param  {Object} session  Jaggery session object
     * @param  {String} type     The type of asset
     * @return {Object}         An object which acts as bag for properties and classes @see createAssetContext
     */
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
            isAnonContext: true,
            session: session
        };
    };
    /**
     * Returns a context built for a logged in user that conforms to the contract outlined in @see createAssetContext
     * The context can be used in pages which are type specific such as a listing page for gadgets.
     * @param  {Object} session Jaggery session object
     * @param  {String} type    The type of asset
     * @return {Object}         An object which acts as bag for properties and classes @see createAssetContext
     */
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
            isAnonContext: false,
            session: session
        };
    };
    /**
     * Returns a context that contains application classes and properties.This method will determine if there
     * is a logged in user and delegates the creation of the context to either the @see createUserAppContext or
     * the @see createAnonAppContext.
     * The context object that is created will indicate if it is anonymous context.
     * The context can be used in pages which are type specific such as a listing page for gadgets.
     * @example
     *     var ctx = core.createAssetContext(session,'gadget');
     *
     *     ctx.username         //The currently logged in user if one is present
     *     ctx.userManager      //A carbon module User Manager instance
     *     ctx.tenantId         //The tenant ID of the currently logged in user
     *     ctx.systemRegistry   //A carbon module registry instance
     *     ctx.assetType        //The asset type for which the context was created
     *     ctx.rxtManager       //An RxtManager instance
     *     ctx.tenantConfigs    //The tenant configurations
     *     ctx.serverConfigs    //The servcer configurations
     *     ctx.isAnonContext    //True if the current context is annoymous
     *     ctx.session          //The session used to create the context
     * @param  {Object} session Jaggery session object
     * @return {Object}         An object which acts as bag for properties and classes @see createAssetContext
     */
    core.createAppContext = function(session) {
        var server = require('store').server;
        var user = require('store').user;
        var userDetails = server.current(session);
        if (!userDetails) {
            log.debug('Obtaining anon app context ');
            return this.createAnonAppContext(session);
        } else {
            return this.createUserAppContext(session);
        }
    };
    /**
     * Returns an annoymous context object that conforms to contract outlined in the @see createAppContext.
     * This method should be used to create a context for pages that can be accessed without logging in.
     * The username will be set to wso2.anonymouse
     * The tenantId will be set to the super tenant
     * The context can be used in pages which are type specific such as a listing page for gadgets.
     * @param  {Object} session  Jaggery session object
     * @param  {String} type     The type of asset
     * @return {Object}         An object which acts as bag for properties and classes @see createAssetContext
     */
    core.createAnonAppContext = function(session) {
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
            rxtManager: rxtManager,
            tenantConfigs: tenatConfigs,
            serverConfigs: serverConfigs,
            isAnonContext: true,
            session: session
        };
    };
    /**
     * Returns a context built for a logged in user that conforms to the contract outlined in @see createAppContext
     * The context can be used in pages which are type neutral such as a login page
     * @param  {Object} session Jaggery session object
     * @param  {String} type    The type of asset
     * @return {Object}         An object which acts as bag for properties and classes @see createAssetContext
     */
    core.createUserAppContext = function(session) {
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
            rxtManager: rxtManager,
            tenantConfigs: tenatConfigs,
            serverConfigs: serverConfigs,
            isAnonContext: false,
            session: session
        };
    };
}(core, constants));