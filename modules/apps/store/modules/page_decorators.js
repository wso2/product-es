var pageDecorators = {};
(function() {
    pageDecorators.navigationBar = function(ctx, page,utils) {
        var rxtManager = ctx.rxtManager;
        //Obtain all of the available rxt types
        var availableTypes = rxtManager.listRxtTypeDetails();
        var types = [];
        var currentType = ctx.assetType;
        var log=new Log();
        page.navigationBar = {};
        for (var index in availableTypes) {
            currentType = availableTypes[index];
            currentType.selected = false;
            currentType.listingUrl=utils.buildBaseUrl(availableTypes[index].shortName)+'/list';
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

    /**
     * The function populates the categories for the category box
     * @param  {[type]} ctx  [description]
     * @param  {[type]} page [description]
     * @return {[type]}      [description]
     */
    pageDecorators.categoryBox=function(ctx,page){
        page.categoryBox={};
        page.categoryBox.categories=page.assetMeta.categories;
        page.categoryBox.searchEndpoint='/apis/assets?type='+ctx.assetType;
        return page;
    };

    pageDecorators.authenticationDetails=function(ctx,page){
        var authenticationMethods=ctx.tenantConfigs.authentication?ctx.tenantConfigs.authentication:{};
        var activeMethod=authenticationMethods.activeMethod?authenticationMethods.activeMethod:'';
        //Obtain the details for this method of authentication
        var authDetails=fetchActiveAuthDetails(activeMethod, authenticationMethods.methods||[]);

        page.security.method=activeMethod;
        page.security.details=authDetails;
        return page;
    };

    var fetchActiveAuthDetails=function(method,methods){
        for(var key in methods){
            if(key==method){
                return methods[key];
            }
        }
        return null;
    };
}());