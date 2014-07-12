log.info('Start of scriptqq!');
asset.manager = function(ctx) {
    return {
        create: function(ctx) {
        	this._super.create(options);
            log.info('Create called!');
        },
        search:function(options){
            log.info('SEARCH method '+stringify(options));
            return [
            {
                id:'146',
                attributes:{
                    overview_name:'Test Asset 1',
                    overview_provider:'Admin'
                }
            }
            ];
        }
    };
};
asset.server = function(ctx) {
    var type=ctx.type;
    return {
        onUserLoggedIn: function() {},
        endpoints: {
            apis: [{
                url: 'asset',
                path: 'asset.jag'
            },
            {
                url:'assets',
                path:'assets.jag'
            }],
            pages: [{
                title: 'Asset: '+type,
                url: 'asset',
                path: 'asset.jag'
            },
            {
                title:'Assets '+type,
                url:'assets',
                path:'assets.jag'
            },
            {
                title:'Create '+type,
                url:'create',
                path:'create.jag'
            },
            {
                title:'Update '+type,
                url:'update',
                path:'update.jag'
            },
            {
                title:'Details '+type,
                url:'details',
                path:'details.jag'
            },
            {
                title:'List '+type,
                url:'list',
                path:'list.jag'
            },
            {
                title:'Lifecycle',
                url:'lifecycle',
                path:'lifecycle.jag'
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
            }
        }
    };
};
asset.renderer = function(ctx) {

    return {
        create: function(page) {
        },
        update: function(page) {},
        list: function(page) {    
            return page;
        },
        details:function(page){},
        lifecycle: function(page) {},
        leftNav: function(page) {},
        ribbon:function(page){}
    };
};