/**
 * Description: The script is used to manage the rendering and generation of form data
 * Filename: form-manager.js
 * Created Date: 16/2/2014
 */
var FormManager = {};


var formManagerLogic = (function () {

    var FORM_MANAGER_DIRECTIVE = '.fm-managed';

    var USE_PLUGINS = 'usePlugins';
    var PLUGIN_ACTION_VALIDATE = 'validate';


    function Manager(formId) {
        this.formId = formId;
        this.pluginMap = {}; //The list of all the registered plugins
        this.fieldMap = {};
        this.formMap = {};

        //Build a map of the fields that will be managed
        buildFieldMap(this.formId, this.fieldMap);

        buildFormMap(this.formId, this.formMap, this.fieldMap);
    }

    /**
     * The function allows a plugin to register itself with the
     * Manager
     * @param plugin The plug-in to be registered
     */
    Manager.prototype.register = function (name, plugin) {
        plugin[name] = plugin;
    };

    /**
     * The method will go through the form and each field and then instantiate the
     * plugins that will be used and give a reference
     */
    Manager.prototype.init = function () {

        initElement(this.formMap, this.pluginMap);

        //Initialize each field managed by the form manager
        for (var index in this.fieldMap) {
            initElement(this.fieldMap[index], this.pluginMap);
        }
    };

    /**
     * The method invokes any validation plugins on the form and the fields.A validation
     * plugin is determined by the presence of the validate method in a plugin instance
     */
    Manager.prototype.validate = function () {
        var validationReport = {};
        validationReport.form={};
        validationReport.form.fields={};
        validationReport.form.errors = invokePluginAction(this.formMap, PLUGIN_ACTION_VALIDATE);

        //Attempt to validate all fields
        for(var fieldName in this.fieldMap){
               validationReport.form.fields[fieldName]=invokePluginAction(this.fieldMap[fieldName],PLUGIN_ACTION_VALIDATE);
        }

        return validationReport;
    };

    /**
     * The function takes an element (field or form) and then creates instances of
     * all required plugins. Each element has its own instance of a plugin
     * @param elementMap An object containing meta information on an element (field or form)
     * @param pluginMap The map of plugins available to the form manager
     */
    var initElement = function (elementMap, pluginMap) {
        var pluginsToUse = elementMap.meta[USE_PLUGINS] || [];
        var plugins = getPlugins(pluginsToUse, pluginMap);
        var instance;

        for (var index in plugins) {
            instance = new plugins[index]();

            //Check if the plugin can handle the element
            if (instance.isHandled(elementMap)) {
                instance.init(elementMap);
                //Create an instance of the plugin for this element
                elementMap.plugins(new plugins[index]());
            }
            else {
                console.log('Plugin: ' + index + ' does not support the provided element');
            }
        }

    };

    /**
     * The function is used to invoke a given plugin action on an element (field or form)
     * If the action is not supported by the plugin then an error is recorded
     * @param elementMap
     * @param action
     */
    var invokePluginAction = function (elementMap, action) {
        var plugin;
        var result;
        var output;

        for (var index in elementMap.plugins) {
            plugin = elementMap.plugins[index];

            if (plugin.hasOwnProperty(action)) {
                console.log('Element: ' + JSON.stringify(elementMap) + ' validated by plugin: ' + index);
                result = plugin[action](elementMap) || {};
                output.push(result);
            }

        }

        return output;
    };

    /**
     * The function returns all plugins that can be used with the provided field
     * @param field
     * @param plugins  A plugin map
     * @returns An array of plugins that can be used with the field
     */
    var determineApplicablePlugins = function (field, plugins) {

        var applicablePlugins = [];
        var plugin;

        //Go through all the plugins and check if they are compatible with a given field
        for (var key in plugins) {
            plugin = plugins[key];

            if (plugin.isApplicable(field)) {
                applicablePlugins.push(plugin)
            }
        }
        return applicablePlugins;
    };

    /**
     * The method returns references to a set of plugins names
     * @param pluginNames
     * @plugins: The plugin map
     * @return: An array containing references to the plugin implementation
     */
    var getPlugins = function (pluginNames, plugins) {

        var ref = [];
        var name;

        for (var index in pluginNames) {
            name = pluginNames[index];
            ref.push(plugins[name]);
        }

        return ref;
    };


    /**
     * The function obtains all fields that need to be managed
     * by the FormManager by checking for elements containing the
     * fm-manage class
     * @param formId
     */
    var buildFieldMap = function (formId, fieldMap) {
        var fieldId;

        $(FORM_MANAGER_DIRECTIVE).each(function () {
            fieldId = this.id;
            fieldMap[fieldId] = {};
            fieldMap[fieldId].id = this.id;
            fieldMap[fieldId].name = this.name;
            fieldMap[fieldId].value = this.value || '';
            fieldMap[fieldId].meta = $('#' + fieldId).data() || {};
            fieldMap[fieldId].plugins = [];
        });
    };

    /**
     * The function is used to extract form meta data
     * @param formId  The id of the form
     * @param formMap An object used to store meta information and plugin details for a form
     * @param fieldMap An object containing a reference to all the fieldd within the form
     */
    var buildFormMap = function (formId, formMap, fieldMap) {
        formMap.meta = $('#' + formId).data() || {};
        formMap.plugins = [];     //A list of plugins to be applied to the form level
        formMap.fields = fieldMap;
    };


    FormManager = Manager;

}());