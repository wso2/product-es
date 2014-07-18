var render = function (theme, data, meta, require) {

    var topAssets='top-assets';
    var topAssetContext=require('/helpers/top-assets.js').currentPage(data.topAssets,data.sso,data.user);
    var navigation='navigation';
    var navigationContext=require('/helpers/navigation.js').currentPage(data.navigation, data.type, data.search);

    switch(data.assetTypeCount) {
        case 1:
            topAssets='top-assets-single';
            topAssetContext=require('/helpers/top-assets-single.js').currentPage(data.topAssets,data.sso,data.user);
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
                partial: topAssets,
                context: topAssetContext
            }
        ],
        right: [
            {
                partial: 'recent-assets',
                context: require('/helpers/asset.js').formatRatings(data.recentAssets)
            }
        ]
    });
};