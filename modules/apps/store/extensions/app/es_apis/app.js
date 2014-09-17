app.server = function(ctx) {
    return {
        endpoints: {
            apis: [{
                url: 'assets',
                path: 'assets.jag'
            },{
            	url:'lifecycles',
            	path:'lifecycles.jag'
            },
            {
                url:'authenticate',
                path:'authenticate.jag'
            },
            {
                url:'logout',
                path:'logout.jag'
            },{
                url:'asset',
                path:'asset.jag'
            }]
        }
    };
};