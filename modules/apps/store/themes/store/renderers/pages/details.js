var render = function(theme, data, meta, require) {
    var log = new Log();
    var navigation = 'navigation';
    log.info(data);
    var navigationContext=data;
    //var navigationContext = require('/helpers/navigation.js').currentPage(data.navigationBar, data.type, data.search);
    switch (data.assetTypeCount) {
        case 1:
            navigation = 'navigation-single';
            //navigationContext=require('/helpers/navigation-single.js').currentPage(data.navigation, data.type, data.search);
            break;
        default:
            break;
    }

    theme('2-column-right', {
        title: data.meta.title,
        header: [{
            partial: 'header',
            context: data.meta
        }],
        navigation: [{
            partial: navigation,
            context: navigationContext
        }],
        body: [
            {
                partial: 'asset',
                context: data
            }
        ],
        // right: [
        //     {
        //         partial: 'my-assets-link',
        //         context: data.myAssets
        //     },
        //     {
        //         partial: 'recent-assets',
        //         context: require('/helpers/asset.js').formatRatings(data.recentAssets)
        //     },
        //     {
        //         partial: 'tags',
        //         context: data.tags
        //     },
        //     {
        //         partial: 'more-assets-from-provider',
        //         context: require('/helpers/asset.js').formatAssetFromProviderRatings(data.moreAssetsFromProvider)
        //     }
        // ]
    });
};