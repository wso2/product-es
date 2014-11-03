asset.server = function() {
    return {
        endpoints: {
            apis: [{
                url: 'new_api',
                path: 'new_api.jag'
            }],
            pages: [{
                title: 'New Servicex Page',
                url: 'new_page',
                path: 'new_page.jag'
            },{
                title:'New Servicex Caramel Page',
                url:'new_caramel_page',
                path:'new_caramel_page.jag'
            }
            , {
                title: 'Details ',
                url: 'details',
                path: 'details.jag'
            }]
        }
    };
};