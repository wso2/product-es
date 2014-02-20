/**
 * Description: The following script contains a set of commonly used plug-ins
 */
$(function () {

    /**
     * The plugin is used to indicate a field is required as well as perform a check
     * to see if the user has entered a value
     * @constructor
     */
    function RequiredField() {

    }

    /**
     * The plug-in will only be applied if the required property has been set to true
     * @param element
     * @returns True if the plug-in should be applied else false
     */
    RequiredField.prototype.isHandled=function(element){
        var isRequired=element.meta.required?element.meta.required:false;
        return isRequired;
    };

    RequiredField.prototype.init = function (element) {
        $('#' + element.id).after('<span class="label-required">*</span>');
        $('#' + element.id).attr('required', 'true');
    };

    RequiredField.prototype.validate = function (element) {
        var value = $('#' + element.id).val();
        if (value == '') {
            return {msg: 'Field: ' + element.id + ' is a required field.'};
        }
    };

    function ReadOnlyField() {

    }

    ReadOnlyField.prototype.init = function (element) {
        $('#' + element.id).attr('disabled', true);
    };


    /**
     *
     * Text Field Value Extractor plugin
     * Description: Obtains the value of a text field
     *
     */

    function TextFieldValueExtractor() {

    }

    TextFieldValueExtractor.prototype.init = function (element) {

    };

    TextFieldValueExtractor.prototype.getData = function (element) {
        var data = {};
        data[element.id] = $('#' + element.id).val();
        return data;
    };

    /**
     * The plugin will print the value of a text field to the console when ever it changes
     * @constructor
     */
    function PrintValueToConsole() {

    }

    PrintValueToConsole.prototype.init = function (element) {
        $('#' + element.id).on('change', function () {
            console.log('Value changed');
        })
    };

    function DefaultFileUploadPlugin() {

    }

    DefaultFileUploadPlugin.prototype.getData = function (element) {
        var file = $('#' + element.id).get('0').files[0];
        var data = {};
        data[element.id] = file;
        return data;
    }

    FormManager.register('RequiredField', RequiredField);
    FormManager.register('ReadOnlyField', ReadOnlyField);
    FormManager.register('TextFieldValueExtractor', TextFieldValueExtractor);
    FormManager.register('PrintValueToConsole', PrintValueToConsole);
    FormManager.register('DefaultFileUploadPlugin', DefaultFileUploadPlugin);
});