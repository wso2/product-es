var pageDecorators = {};
(function() {
    pageDecorators.navigationBar = function(ctx, page) {
        var rxtManager = ctx.rxtManager;
        //Obtain all of the available rxt types
        var availableTypes = rxtManager.listRxtTypeDetails();
        var types = [];
        var currentType = ctx.assetType;
        page.navigationBar = {};
        for (var index in availableTypes) {
            currentType = availableTypes[index];
            currentType.selected = false;
            if (currentType.shortName == ctx.assetType) {
                currentType.selected = true;
            }
            types.push(currentType);
        }
        page.navigationBar.types = types;
        return page;
    };
    /**
     * The function populates any text field as a search field
     * @param  {[type]} ctx  [description]
     * @param  {[type]} page [description]
     * @return {[type]}      [description]
     */
    pageDecorators.searchBar = function(ctx, page) {
        page.searchBar = {};
        page.searchBar.searchFields = [];
        var searchFields = page.assetMeta.searchFields;
        for (var index in searchFields) {
            if ((searchFields[index].type == 'text') ||(searchFields[index].type=='options')){
                page.searchBar.searchFields.push(searchFields[index]);
            }
        }
        return page;
    };
}());