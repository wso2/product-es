asset.manager = function(ctx) {
    return {
        search: function(query, paging) {
            var assets = this._super.search.call(this, query, paging);
            return assets;
        },
        list: function(paging) {
            var assets = this._super.list.call(this, paging);
            return assets;
        },
        get: function(id) {
            var asset = this._super.get.call(this, id);
            return asset;
        }
    };
};
asset.server = function(ctx) {
    var type = ctx.assetType;
    var typeDetails=ctx.rxtManager.listRxtTypeDetails(type);
    var typeSingularLabel=type;//Assume the type details are not returned
    if(typeDetails){
        typeSingularLabel=typeDetails.singularLabel;
    }
    return {
        onUserLoggedIn: function() {},
        endpoints: {
            apis: [{
                url: 'assets',
                path: 'assets.jag'
            }],
            pages: [ {
                title: 'Store |  ' + typeSingularLabel,
                url: 'details',
                path: 'details.jag'
            }, {
                title: 'Store | ' + typeSingularLabel,
                url: 'list',
                path: 'list.jag'
            }]
        }
    };
};
asset.configure = function() {
    return {
        table: {
            overview: {
                fields: {
                    provider: {
                        readonly: true
                    },
                    name: {
                        name: {
                            name: 'name',
                            label: 'Name'
                        },
                        validation: function() {}
                    },
                    version: {
                        name: {
                            label: 'Version'
                        }
                    }
                }
            },
            images: {
                fields: {
                    thumbnail: {
                        type: 'file'
                    },
                    banner: {
                        type: 'file'
                    }
                }
            }
        },
        meta: {
            lifecycle: {
                name: 'SampleLifeCycle2',
                commentRequired: false,
                defaultAction: 'Promote',
                deletableStates:[],
                publishedStates:['Published']
            },
            ui: {
                icon: 'icon-cog'
            },
            categories:{
                categoryField:'overview_category'
            },
            search:{
                searchableFields:['all']
            },
            thumbnail: 'images_thumbnail'
        }
    };
};
asset.renderer = function(ctx) {
    var decoratorApi=require('/modules/page_decorators.js').pageDecorators;

    return {
        pageDecorators: {
            navigationBar:function(page){
                return decoratorApi.navigationBar(ctx,page);
            },
            searchBar:function(page){
                return decoratorApi.searchBar(ctx,page);
            }
        }
    };
};