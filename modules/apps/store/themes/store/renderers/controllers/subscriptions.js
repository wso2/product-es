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

var render = function (theme, data, meta, require) {

    var navigation = 'navigation';
    var navigationContext = require('/helpers/navigation.js').currentPage(data.navigation, data.type, data.search);

    switch (data.assetTypeCount) {
        case 1:
            navigation = 'navigation-single';
            navigationContext = require('/helpers/navigation-single.js').currentPage(data.navigation, data.type, data.search);
            break;
        default:
            break;
    }

    theme('2-column-right', {
        title: data.title,
        header: [
            {
                partial: 'header',
                context: data.header
            }
        ],
        navigation: [
            {
                partial: navigation,
                context: navigationContext
            }
        ],
        body: [
            {
                partial: 'subscriptions',
                context: {
                    'subscriptions': data.subscriptions,
                    'URL': data.URL,
                    'myAssets' : data.myAssets
                }
            }
        ],
        right: [
        	/*
			{
							partial: 'my-assets-link',
							context: data.myAssets
						},*/
			
            {
                partial: 'recent-assets',
                context: require('/helpers/asset.js').formatRatings(data.recentAssets)
            },
            {
                partial: 'tags',
                context: data.tags
            }
        ]
    });
};

