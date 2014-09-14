app.extensionDependencies=['es_subscriptions'];

app.server=function(ctx){

	return{
		endpoints:{
			pages:[
				{
					title:'Top Assets',
					url:'top-aasets',
					path:'top_assets.jag'
				},
				{
					title:'My Items',
					url:'my_items',
					path:'my_items.jag'
				}

			]

		}

	}

};