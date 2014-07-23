var render = function (theme, data, meta, require) {

    var navigation='navigation';
    var navigationContext=require('/helpers/navigation.js').currentPage(data.navigation, data.type, data.search);

    switch(data.assetTypeCount) {
        case 1:
            navigation='navigation-single';
            navigationContext=require('/helpers/navigation-single.js').currentPage(data.navigation, data.type, data.search);
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

