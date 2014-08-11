var addOptionTextRow = {};
var removeOptionTextRow = {};
$(function() {
    var updateName = function(name, index) {
        var sub = name.substring(0, name.length - 1);
        return sub + index;
    };
    var clearContent = function(row) {
        for (var index = 0; index < row.childNodes.length; index++) {
            var cell = row.childNodes[index];
            var element = cell.childNodes[0];
            var name = $(element).attr('name');
            if (name) {
                $(cell.childNodes[0]).val('');
            }
        }
    };
    var generateId = function(root, row) {
        var rowCount = $(root).data('rowCount');
        if (!rowCount) {
            rowCount = 1;
            $(root).data('rowCount', rowCount);
        } else {
            rowCount++;
            $(root).data('rowCount', rowCount);
        }
        for (var index = 0; index < row.childNodes.length; index++) {
            var cell = row.childNodes[index];
            var element = cell.childNodes[0];
            var name = $(element).attr('name');
            if (name) {
                $(element).attr('name', updateName(name, rowCount));
            }
        }
    };
    var countRows = function(root) {
        var count = root.childNodes.length;
        return count;
    };
    addOptionTextRow = function(el) {
        var root = el.parentNode.parentNode.parentNode;
        //Check if there is a saved template in the root
        var template = $(root).data('template');
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
        var rowCount = $(root).data('rowCount');
        if (!rowCount) {
            rowCount = countRows(root);
            $(root).data('rowCount', rowCount);
        }
        $(root).data('template', row);
        //Cache this row before removal
        $(row).remove();
    };
});