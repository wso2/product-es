app.server = function(ctx) {
    return {
        endpoints: {
            pages: [{
                title: 'Store | Top Assets',
                url: 'top-assets',
                path: 'top_assets.jag'
            },{
                title:'Store | My Items',
                url:'my-items',
                path:'my_items.jag'
            }]
        }
    }
};
app.pageHandlers = function(req, res, session, page) {
    return {
        onLoad: function() {
            return true;
        }
    };
};
app.renderer = function(ctx) {
    var decoratorApi = require('/modules/page_decorators.js').pageDecorators;
    return {
        pageDecorators: {
            navigationBar: function(page) {
                return decoratorApi.navigationBar(ctx, page, this);
            },
            searchBar: function(page) {
                return decoratorApi.searchBar(ctx, page, this);
            },
            authenticationDetails: function(page) {
                return decoratorApi.authenticationDetails(ctx, page, this);
            },
            recentAssetsOfActivatedTypes: function(page) {
                return decoratorApi.recentAssetsOfActivatedTypes(ctx, page, this);
            },
            popularAssets:function(page){
                return decoratorApi.popularAssets(ctx,page,this);
            }
        }
    }
};