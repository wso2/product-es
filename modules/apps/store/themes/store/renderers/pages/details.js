var render = function(theme, data, meta, require) {
    var log = new Log();
    var navigation = 'navigation';
    var navigationContext = data;
    // log.info('Data '+stringify(data));
    switch (data.assetTypeCount) {
        case 1:
            navigation = 'navigation-single';
            break;
        default:
            break;
    }
    theme('2-column-right', {
        title: data.meta.title,
        header: [{
            partial: 'header',
            context: data
        }],
        navigation: [{
            partial: navigation,
            context: navigationContext
        }],
        body: [{
            partial: 'asset',
            context: data
        }],
        right: [
            {
                partial: 'my-assets-link',
                context: data
            },
            {
                partial: 'recent-assets',
                context: data
            },
            {
                partial: 'tags',
                context: data
            }
            // {
            //     partial: 'more-assets-from-provider',
            //     context: require('/helpers/asset.js').formatAssetFromProviderRatings(data.moreAssetsFromProvider)
            // }
        ]
    });
};