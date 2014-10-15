/*
var render = function (theme, data, meta, require) {
    //print(caramel.build(data));
   
    theme('1-column', {
        title: data.title,
        navigation: [
            {
                partial: 'navigation',
                context: data.navigation
            }
        ],
        body: [
            {
                partial: 'userAssets',
                context: data.userAssets
            }
        ]
    });
};

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
       /*
        navigation: [
                   {
                       partial: 'navigation',
                       context: data.navigation
                   },
                   {
                       partial: 'search',
                       context: data.search
                   }
               ],
              */
       
        body: [
            {
                partial: 'userAssets',
                context: {
        		'userAssets': data.userAssets,
        		'URL': data.URL
				}
            }
        ],
        right: [
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

