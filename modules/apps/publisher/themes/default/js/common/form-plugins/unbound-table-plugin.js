$(function () {

    var ADD_BUTTON_ID='-add-row';
    var addButton = "<input class='btn' id='{{id}}-add-row' type='button' value='Add'>";
    var deleteButton="<input class='btn btn-danger' type='button'  value='Delete'>";
    var compiledButtonTemplate=Handlebars.compile(addButton);


    function UnboundTablePlugin() {

    }

    UnboundTablePlugin.prototype.init = function (element) {
        console.log('Activating the UnboundTablePlugin');
        console.log('*********************************');

        //Get a reference to the table
        var table = $(getTableId(element))[0];

        //Add the add row button to the table
         populateAddButton(table,element);

        //Add a delete button to each row
    };

    /**
     * The attaches a handler which will delete the provided row
     * @param table The targeted row is contained within this table
     * @param row The row to which the delete handler will be bound
     */
    var attachDeleteRowHandler = function (table, row) {

    };

    /**
     * The function adds a delete button to the provided row
     * @param row The row to which the delete button needs to be added
     */
    var populateDeleteButton = function (row) {
         var deleteRow=row.cells[row.cells.length-1];

        $(deleteRow).html(deleteButton);
        //Delete the row when the button is clicked
        var button=$(deleteRow).children()[0];
        $(button).on('click',function(){
           alert('Delete clicked');
        });
    };

    /**
     * The function creates an add button which is used to add a new row
     * to the unbounded table
     * @param table
     * @param element
     */
    var populateAddButton=function(table,element){

        $(getControlContainerId(element)).html(compiledButtonTemplate(element));

        $('#'+element.id+ADD_BUTTON_ID).on('click',function(){

            var table=getTable(element);

            var row=table.rows[1];

            var clonedRow=$(row).clone();

            populateDeleteButton(row);

            //table.insertRow()
            $(table).append(clonedRow);

        });
    };

    var getTableId = function (element) {
        return '#' + element.id;
    };

    var getTable=function(element){
        return $(getTableId(element))[0];
    };

    var getControlContainerId=function(element){
        return '#'+element.id+'-controls';
    };



    FormManager.register('UnboundTablePlugin', UnboundTablePlugin);
}());