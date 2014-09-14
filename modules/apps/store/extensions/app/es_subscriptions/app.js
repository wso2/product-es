app.server=function(ctx){

	return{
		endpoints:{
			pages:[
				{
					title:'Manage Subscriptions',
					url:'manage_subs',
					path:'manage_subs.jag'
				},
				{
					title:'View Subscriptions',
					url:'view_subs',
					path:'view_subs.jag'
				}

			]

		}

	}

};