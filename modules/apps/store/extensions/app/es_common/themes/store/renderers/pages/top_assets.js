var render = function(theme, data, meta, require) {
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
            partial: 'top_assets',
            context: {}
        }]
    });
};