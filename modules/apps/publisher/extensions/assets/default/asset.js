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

asset.manager = function (ctx) {

    var notifier = require('store').notificationManager;
    var storeConstants = require('store').storeConstants;
    var COMMENT = 'User comment';

    return {
        create: function (options) {
            var ref = require('utils').time;

            //Check if the options object has a createdtime attribute and populate it
            if ((options.attributes) && (options.attributes.hasOwnProperty('overview_createdtime'))) {
                options.attributes.overview_createdtime = ref.getCurrentTime();
            }
            this._super.create.call(this, options);

            var asset = this.get(options.id); //TODO avoid get: expensive operation
            var assetPath = asset.path;
            var user = ctx.username;
            var userRoles = ctx.userManager.getRoleListOfUser(user);

            //Check whether the user has admin role
            var endpoint = storeConstants.PRIVATE_ROLE_ENDPOINT + user;
            for (var role in userRoles) {
                if (userRoles.hasOwnProperty(role) && userRoles[role] == storeConstants.ADMIN_ROLE) {
                    endpoint = storeConstants.ADMIN_ROLE_ENDPOINT;
                }
            }
            //Subscribe the asset author for LC update event and asset update event
            notifier.subscribeToEvent(options.attributes.overview_provider, assetPath, endpoint, storeConstants.LC_STATE_CHANGE);
            notifier.subscribeToEvent(options.attributes.overview_provider, assetPath, endpoint, storeConstants.ASSET_UPDATE);
        },
        update: function (options) {
            this._super.update.call(this, options);
            var asset = this.get(options.id); //TODO avoid get: expensive operation
            //trigger notification on asset update
            notifier.notifyEvent(storeConstants.ASSET_UPDATE_EVENT, asset.type, asset.attributes.overview_name, null, asset.path, ctx.tenantId);
        },
        search: function (query, paging) {
            return this._super.search.call(this, query, paging);
        },
        list: function (paging) {
            return this._super.list.call(this, paging);
        },
        get: function (id) {
            return this._super.get.call(this, id);
        },
        invokeLcAction: function (asset, action) {
            var success = this._super.invokeLcAction.call(this, asset, action);
            //trigger notification on LC state change
            notifier.notifyEvent(storeConstants.LC_STATE_CHANGE_EVENT, asset.type, asset.attributes.overview_name, COMMENT, asset.path, ctx.tenantId);
            return success;
        }
    };
};
asset.server = function (ctx) {
    var type = ctx.type;
    return {
        onUserLoggedIn: function () {
        },
        endpoints: {
            apis: [
                {
                    url: 'assets',
                    path: 'assets.jag'
                }
            ],
            pages: [
                {
                    title: 'Asset: ' + type,
                    url: 'asset',
                    path: 'asset.jag'

                },
                {
                    title: 'Assets ' + type,
                    url: 'assets',
                    path: 'assets.jag'
                },
                {
                    title: 'Create ' + type,
                    url: 'create',
                    path: 'create.jag'
                },
                {
                    title: 'Update ' + type,
                    url: 'update',
                    path: 'update.jag'
                },
                {
                    title: 'Details ' + type,
                    url: 'details',
                    path: 'details.jag'
                },
                {
                    title: 'List ' + type,
                    url: 'list',
                    path: 'list.jag'
                },
                {
                    title: 'Lifecycle',
                    url: 'lifecycle',
                    path: 'lifecycle.jag'
                }
            ]
        }
    };
};
asset.configure = function () {
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
                        validation: function () {
                        }
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
                deletableStates: [],
                publishedStates: ['Published']
            },
            ui: {
                icon: 'icon-cog'
            },
            thumbnail: 'images_thumbnail',
            banner: 'images_banner'
        }
    };
};
asset.renderer = function (ctx) {
    var type = ctx.assetType;
    var buildListLeftNav = function (page, util) {
        var navList = util.navList();
        navList.push('Add', 'icon-plus-sign-alt', util.buildUrl('create'));
        navList.push('Statistics', 'icon-dashboard', '/assets/statistics/' + type + '/');
        return navList.list();
    };
    var buildDefaultLeftNav = function (page, util) {
        var id = page.assets.id;
        var navList = util.navList();
        navList.push('Overview', 'icon-list-alt', util.buildUrl('details') + '/' + id);
        navList.push('Edit', 'icon-edit', util.buildUrl('update') + '/' + id);
        navList.push('Life Cycle', 'icon-retweet', util.buildUrl('lifecycle') + '/' + id);
        return navList.list();
    };
    var buildAddLeftNav = function (page, util) {
        return [];
    };
    var isActivatedAsset = function (assetType) {
        var app = require('rxt').app;

        var activatedAssets = app.getActivatedAssets(ctx.tenantId);//ctx.tenantConfigs.assets;
        //return true;
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
        list: function (page) {
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
        pageDecorators: {
            leftNav: function (page) {
                log.info('Using default leftNav');
                switch (page.meta.pageName) {
                    case 'list':
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
            ribbon: function (page) {
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