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
    var type = ctx.type;
    return {
        onUserLoggedIn: function() {},
        endpoints: {
            apis: [{
                url: 'assets',
                path: 'assets.jag'
            }],
            pages: [ {
                title: 'Details ' + type,
                url: 'details',
                path: 'details.jag'
            }, {
                title: 'List ' + type,
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
            thumbnail: 'images_thumbnail'
        }
    };
};
asset.renderer = function(ctx) {

    return {
        list: function(page) {
            return page;
        },
        pageDecorators: {
            leftNav: function(page) {
                return page;
            },
            ribbon: function(page) {
                return page;
            }
        }
    };
};