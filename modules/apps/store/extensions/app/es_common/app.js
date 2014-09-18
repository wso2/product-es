app.server = function(ctx) {
    return {
        endpoints: {
            pages: [{
                title: 'Store | Top Assets',
                url: 'top-assets',
                path: 'top_assets.jag'
            }]
        }
    }
};
app.pageHandlers = function(req,res,session,page) {
    return {
        onLoad: function() {
            return true;
        }
    };
};
app.renderer = function(ctx) {
    return {
        pageDecorators: {
            navigationBar: function(page) {},
            searchBar: function(page) {},
            authenticationDetails:function(page){}
        }
    }
};