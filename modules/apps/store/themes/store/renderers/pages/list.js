var render = function(theme, data, meta, require) {
    var log = new Log();
    var navigation = 'navigation';
    var navigationContext = data;
    switch (data.assetTypeCount) {
        case 1:
            navigation = 'navigation-single';
            break;
        default:
            break;
    }
    //var assets = require('/helpers/assets.js');
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
        body: [
            {
                partial: 'sort-assets',
                context: data
            },
            {
                partial: 'assets',
                context: data
            }
            // {
            //     partial: 'pagination',
            //     context: require('/helpers/pagination.js').format(data.paging)
            // } 
        ]
        // right: [
        //  {
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
        //     }
        // ]
    });
};