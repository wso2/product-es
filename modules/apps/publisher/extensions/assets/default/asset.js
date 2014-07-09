
log.info('Start of script!');

asset.manager = function(ctx) {
    return {
        create: function() {
            log.info('Create called!');
        }
    };
};
asset.server = function(ctx) {
    return {
        onUserLoggedIn: function() {}
    }
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
                        validate: function() {}
                    },
                    version: {
                        label: 'Super Version'
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
asset.ui = function(ctx) {
    return {
        renderCreate: function() {},
        renderEdit: function() {},
        renderLifecycle: function() {},
        renderNavigation: function() {}
    };
};