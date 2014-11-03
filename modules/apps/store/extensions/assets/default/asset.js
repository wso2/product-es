/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
asset.manager = function(ctx) {
    /**
     * The function augments the provided query to include published state information
     * @param  {[type]} query [description]
     * @return {[type]}       The provided query object 
     */
    var buildPublishedQuery = function(query) {
        //Get all of the published assets
        var publishedStates = ctx.rxtManager.getPublishedStates(ctx.assetType) || [];
        //Determine if there are any published states
        if (publishedStates.length == 0) {
            return query;
        }
        //If there is no query then build a new one
        if (!query) {
            query = {};
        }
        //TODO: Even though an array is sent in only the first search value is accepted
        query.lcState=[publishedStates[0]];
        return query;
    };
    return {
        search: function(query, paging) {
            query=buildPublishedQuery(query);
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
    var typeDetails = ctx.rxtManager.listRxtTypeDetails(type);
    var typeSingularLabel = type; //Assume the type details are not returned
    if (typeDetails) {
        typeSingularLabel = typeDetails.singularLabel;
    }
    return {
        onUserLoggedIn: function() {},
        endpoints: {
            apis: [{
                url: 'assets',
                path: 'assets.jag'
            }, {
                url: 'subscriptions',
                path: 'subscriptions.jag'
            }, {
                url: 'rate',
                path: 'rate.jag'
            }],
            pages: [{
                title: 'Store |  ' + typeSingularLabel,
                url: 'details',
                path: 'details.jag'
            }, {
                title: 'Store | ' + typeSingularLabel,
                url: 'list',
                path: 'list.jag'
            }, {
                title: 'Store | ' + typeSingularLabel,
                url: 'subscriptions',
                path: 'subscriptions.jag'
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
            categories: {
                categoryField: 'overview_category'
            },
            search: {
                searchableFields: ['all'],
            },
            paging: {
                size: 10
            },
            thumbnail: 'images_thumbnail',
            banner: 'images_banner'
        }
    };
};
asset.renderer = function(ctx) {
    var decoratorApi = require('/modules/page_decorators.js').pageDecorators;
    return {
        pageDecorators: {
            navigationBar: function(page) {
                return decoratorApi.navigationBar(ctx, page, this);
            },
            searchBar: function(page) {
                return decoratorApi.searchBar(ctx, page, this);
            },
            categoryBox: function(page) {
                return decoratorApi.categoryBox(ctx, page, this);
            },
            authenticationDetails: function(page) {
                return decoratorApi.authenticationDetails(ctx, page, this);
            },
            recentAssets: function(page) {
                return decoratorApi.recentAssets(ctx, page);
            },
            tags: function(page) {
                return decoratorApi.tags(ctx, page);
            },
            myAssets: function(page) {
                return decoratorApi.myAssets(ctx, page);
            },
            socialFeature: function(page) {
                return decoratorApi.socialFeature(ctx, page);
            }
        }
    };
};