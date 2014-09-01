
asset.server = function(ctx) {
    var type = ctx.type;
    return {
        onUserLoggedIn: function() {},
        endpoints: {
            apis:[{
                url:'configs',
                path:'config.jag'
            }],
            pages: [{
                title:'Configuration',
                url:'configuration',
                path:'configuration.jag'
            }]
        }
    };
};

asset.renderer = function(ctx) {
    var type = ctx.assetType;
    var buildListLeftNav = function(page, util) {
        var log = new Log();
        return [{
            name: 'Add ',
            iconClass: 'icon-plus-sign-alt',
            url: util.buildUrl('create')
        }, {
            name: 'Statistics',
            iconClass: 'icon-dashboard',
            url: '/assets/statistics/' + type + '/'
        },{
            name:'Configuration',
            iconClass:'icon-dashboard',
            url:util.buildUrl('configuration')
        }];
    };
    var buildDefaultLeftNav = function(page, util) {
        var id = page.assets.id;
        return [{
            name: 'Overview',
            iconClass: 'icon-list-alt',
            url: util.buildUrl('details') + '/' + id
        }, {
            name: 'Edit',
            iconClass: 'icon-edit',
            url: util.buildUrl('update') + '/' + id
        }, {
            name: 'Life Cycle',
            iconClass: 'icon-retweet',
            url: util.buildUrl('lifecycle') + '/' + id
        }];
    };
    var buildAddLeftNav = function(page, util) {
        return [];
    };
    var isActivatedAsset = function(assetType) {
        var activatedAssets = ctx.tenantConfigs.assets;
        return true;
        if (!activatedAssets) {
            throw 'Unable to load all activated assets for current tenant: ' + ctx.tenatId + '.Make sure that the assets property is present in the tenant config';
        }
        for (var index in activatedAssets) {
            if (activatedAssets[index] == assetType) {
                return true;
            }
        }
        return false;
    };
    return {
        create: function(page) {},
        update: function(page) {},
        list: function(page) {
            var assets = page.assets;
            for (var index in assets) {
                var asset = assets[index];
                if (asset.attributes.overview_createdtime) {
                    var value = asset.attributes.overview_createdtime;
                    var date = new Date();
                    date.setTime(value);
                    asset.attributes.overview_createdtime = date.toUTCString();
                }
            }
        },
        details: function(page) {},
        lifecycle: function(page) {},
        leftNav: function(page) {
            log.info('Building leftNav for gadget');
            switch (page.meta.pageName) {
                case 'list':
                    page.leftNav = buildListLeftNav(page, this);
                    break;
                case 'create':
                    page.leftNav=buildAddLeftNav(page,this);
                    break;
                default:
                    page.leftNav = buildDefaultLeftNav(page, this);
                    break;
            }
            return page;
        },
        ribbon: function(page) {
            var ribbon = page.ribbon = {};
            var DEFAULT_ICON = 'icon-cog';
            var assetTypes = [];
            var assetType;
            var assetList = ctx.rxtManager.listRxtTypeDetails();
            for (var index in assetList) {
                assetType = assetList[index];
                if (isActivatedAsset(assetType.shortName)) {
                    assetTypes.push({
                        url: this.buildBaseUrl(assetType.shortName) + '/list',
                        assetIcon: assetType.ui.icon || DEFAULT_ICON,
                        assetTitle: assetType.singularLabel
                    });
                }
            }
            ribbon.currentType = page.rxt.singularLabel;
            ribbon.currentTitle = page.rxt.singularLabel;
            ribbon.currentUrl = this.buildBaseUrl(type) + '/list'; //page.meta.currentPage;
            ribbon.shortName = page.rxt.singularLabel;
            ribbon.query = 'Query';
            ribbon.breadcrumb = assetTypes;
            return page;
        }
    };
};