/*
 * Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
asset.server = function(ctx) {
    var type = ctx.type;
    return {
        onUserLoggedIn: function() {},
        endpoints: {
            apis: [{
                url: 'configs',
                path: 'config.jag'
            }],
            pages: [{
                title: 'Configuration',
                url: 'configuration',
                path: 'configuration.jag'
            }]
        }
    };
};
asset.renderer = function(ctx) {
    var type = ctx.assetType;
    var buildListLeftNav = function(page, util) {
        var navList = util.navList();
        navList.push('Add', 'icon-plus-sign-alt', util.buildUrl('create'));
        navList.push('Statistics', 'icon-dashboard', '/assets/statistics/' + type + '/');
        //navList.push('Configuration', 'icon-dashboard', util.buildUrl('configuration'));
        return navList.list();
    };
    var buildDefaultLeftNav = function(page, util) {
        var id = page.assets.id;
        var navList = util.navList();
        navList.push('Overview', 'icon-list-alt', util.buildUrl('details') + '/' + id);
        navList.push('Edit', 'icon-edit', util.buildUrl('update') + '/' + id);
        navList.push('Life Cycle', 'icon-retweet', util.buildUrl('lifecycle') + '/' + id);
        return navList.list();
    };
    var buildAddLeftNav = function(page, util) {
        return [];
    };
    var isActivatedAsset = function(assetType) {
        var app = require('rxt').app;
        var activatedAssets = app.getActivatedAssets(ctx.tenantId); //ctx.tenantConfigs.assets;
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
        pageDecorators: {
            leftNav: function(page) {
                switch (page.meta.pageName) {
                    case 'list':
                        //log.info('Rendering list');
                        page.leftNav = buildListLeftNav(page, this);
                        break;
                    case 'create':
                        page.leftNav = buildAddLeftNav(page, this);
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
        }
    };
};