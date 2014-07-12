var render = function(theme, data, meta, require) {
    theme('single-col-fluid', {
        title: 'Asset',
        header: [{
            partial: 'header',
            context: data
        }],
        listassets: [{
            partial: 'list-assets',
            context: data
        }]
    });
};