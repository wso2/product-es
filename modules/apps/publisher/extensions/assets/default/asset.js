log.info('Start of scriptqq!');
asset.manager = function(ctx) {
    return {
        create: function(options) {
            this._super.create(options);
            log.info('Create called!');
        },
        search: function(query,paging) {
            return this._super.search.call(this,query,paging);
        }
    };
};
asset.server = function(ctx) {
    var type = ctx.type;
    return {
        onUserLoggedIn: function() {},
        endpoints: {
            apis: [{
                url: 'asset',
                path: 'asset.jag'
            }, {
                url: 'assets',
                path: 'assets.jag'
            }],
            pages: [{
                title: 'Asset: ' + type,
                url: 'asset',
                path: 'asset.jag'
            }, {
                title: 'Assets ' + type,
                url: 'assets',
                path: 'assets.jag'
            }, {
                title: 'Create ' + type,
                url: 'create',
                path: 'create.jag'
            }, {
                title: 'Update ' + type,
                url: 'update',
                path: 'update.jag'
            }, {
                title: 'Details ' + type,
                url: 'details',
                path: 'details.jag'
            }, {
                title: 'List ' + type,
                url: 'list',
                path: 'list.jag'
            }, {
                title: 'Lifecycle',
                url: 'lifecycle',
                path: 'lifecycle.jag'
            }]
        }
    };
};
asset.configure = function() {
    return {
        table: {
            overview: {
                fields: {
                    name: {
                        name: {
                            name: 'name',
                            label: 'Name'
                        },
                        required: false,
                        validation: function() {}
                    },
                    version: {
                        name: {
                            label: 'Super Version'
                        }
                    }
                }
            }
        },
        meta: {
            lifeCycle: {
                name: 'SimpleLifecycle',
                commentRequired: true
            },
            ui: {
                icon: 'icon-cog'
            }
        }
    };
};
asset.renderer = function(ctx) {
    var buildListLeftNav = function(page,util) {
        var log=new Log();
        return [{
            name: 'Add ',
            iconClass: 'icon-plus-sign-alt',
            url: util.buildUrl('create')
        }, {
            name: 'Statistics',
            iconClass: 'icon-dashboard',
            url: util.buildUrl('stats')
        }];
    };
    var buildDefaultLeftNav = function(page,util) {

        return [{
            name: 'Overview',
            iconClass: 'icon-list-alt',
            url: util.buildUrl('details')
        }, {
            name: 'Edit',
            iconClass: 'icon-edit',
            url: util.buildUrl('update')
        }, {
            name: 'Life Cycle',
            iconClass: 'icon-retweet',
            url: util.buildUrl('lifecycle')
        }];
    };
    return {
        create: function(page) {},
        update: function(page) {},
        list: function(page) {},
        details: function(page) {},
        lifecycle: function(page) {},
        leftNav: function(page) {
            switch (page.meta.pageName) {
                case 'list':
                    page.leftNav = buildListLeftNav(page,this);
                    break;
                default:
                    page.leftNav = buildDefaultLeftNav(page,this);
                    break;
            }

            return page;
        },
        ribbon: function(page) {
            page.ribbon.currentType = 'Gadget111';
            page.ribbon.shortName = 'aaaa';
            page.ribbon.currentTitle = 'Test';
            return page;
        }
    };
};