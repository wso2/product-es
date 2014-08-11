/**
 * The script is used to handle the rendering of the options text field type.It handles
 * - Adding of new rows
 * - Removal of existing rows;
 * @type {Object}
 */
var addOptionTextRow = {};
var removeOptionTextRow = {};
$(function() {
    var ROW_COUNT = 'rowCount'; //A counter which maintains the number of rows created in order to generate unique ids for each row
    var ROW_TEMPLATE = 'template';
    /**
     * The function generates a unique name given the current name of an element and an index
     * @param  {[type]} name  The current name of the element
     * @param  {[type]} index A unique index value
     * @return {[type]}       A unique name based on the current name and the provided index.It can be considered unique for the lifetime of the page
     */
    var updateName = function(name, index) {
        var sub = name.substring(0, name.length - 1);
        return sub + index;
    };
    /**
     * The function resets the contents of the row since the new rows
     * which are added contain the values of the template
     * @param  {[type]} row [description]
     */
    var clearContent = function(row) {
        iterateRow(row, function(cell) {
            $(cell.childNodes[0]).val('');
        })
    };
    var iterateRow = function(row, fn) {
        for (var index = 0; index < row.childNodes.length; index++) {
            var cell = row.childNodes[index];
            var element = cell.childNodes[0];
            var name = $(element).attr('name');
            if (name) {
                fn(cell);
            }
        }
    };
    var generateId = function(root, row) {
        var rowCount = $(root).data(ROW_COUNT);
        if (!rowCount) {
            rowCount = 1;
        } else {
            rowCount++;
        }
        $(root).data(ROW_COUNT, rowCount);
        iterateRow(row, function(cell) {
            var element = cell.childNodes[0];
            var name = $(element).attr('name');
            $(element).attr('name', updateName(name, rowCount));
        });
    };
    var countRows = function(root) {
        var count = root.childNodes.length;
        return count;
    };
    addOptionTextRow = function(el) {
        var root = el.parentNode.parentNode.parentNode;
        //Check if there is a saved template in the root
        var template = $(root).data(ROW_TEMPLATE);
        if (template) {
            generateId(root, template);
            var clone = $(template).clone();
        } else {
            var row = root.childNodes[2];
            generateId(root, row);
            var clone = $(row).clone();
        }
        clearContent(clone[0]);
        clone.appendTo(root);
    };
    removeOptionTextRow = function(el) {
        var row = el.parentNode.parentNode;
        var root = row.parentNode;
        var rowCount = $(root).data(ROW_COUNT);
        if (!rowCount) {
            rowCount = countRows(root);
            $(root).data(ROW_COUNT, rowCount);
        }
        //Cache this row before removal
        $(root).data(ROW_TEMPLATE, row);
        $(row).remove();
    };
});