var meta={
    use:'export',
    type:'formo',
    required:['model','template']
};

var module=function(){

    /**
     * The function creates a options data object which will be used
     * to render drop downs
     * @param fieldTemplate  : An object describing the structure of the field
     * @param data The options data will be added to this object
     */
    var buildOptionsObject = function (fieldTemplate, data) {
        //Only apply the logic if the field type is options
        if (!data.isOptions) {
            return;
        }

        var optionData = {};
        var options = csvToArray(fieldTemplate.value || '');
        optionData['selected'] = options[0];
        data['valueList']=options;

        data['optionData'] = optionData;

    };

    /*
     The function converts a string comma seperated value list to an array
     @str: A string representation of an array
     TODO: Move to utility script
     */
    function csvToArray(str) {
        var array = str.split(',');
        return array;
    }


    function getOverviewFields(template){

        var fields=[];

        for each(var table in template.tables){

            if(table.name.toLowerCase()=='overview'){

               for each(var field in table.fields){

                    var searchBool = (field.meta.search == "true");

                    var search=searchBool||false;
                    //log.info(stringify(field));
                    var data={};
                    data.isOptions=(field.type=='options')?true:false;
                    data.field_name=field.name;
                    data.field_label=field.label;

                    data.search=search;
                    buildOptionsObject(field,data);

                    //log.info(stringify(field));
                    fields.push(data);
                }
            }
        }

        return fields;
    }
    return{
        execute:function(context){
            var model=context.model;
            var template=context.template;
            return getOverviewFields(template);
        }
    }
}