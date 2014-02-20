/**
 * The following script is used to convert an element to a Date field
 */
(function(FormManager){

    function DateFieldWidget(){

    }

    DateFieldWidget.prototype.init=function(element){
    };

    DateFieldWidget.prototype.getData=function(){
    };

    /**
     * The method is used to determine if the particular field is supported by the
     * current field
     * @param element
     */
    DateFieldWidget.prototype.isHandled=function(element){

    };

    /**
     * The class is used to define a plugin that will validate if the entered date is today.
     * If not it will record an error
     * @constructor
     */
    function ValidateDateIsToday(){

    }

    ValidateDateIsToday.prototype.init=function(field){

    };

    ValidateDateIsToday.prototype.validate=function(field,errors){

    };

    ValidateDateIsToday.prototype.isHandled=function(field){

    };

    function ValidateDateInRange(){

    }

    FormManager.register('DateFieldWidget',DateFieldWidget);
    FormManager.register('ValidateDateIsToday',ValidateDateIsToday);


}(FormManager));