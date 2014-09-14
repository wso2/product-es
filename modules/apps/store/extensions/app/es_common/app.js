app.extensions=function(){
	return{
		dependencies:['es_common']
	};
};

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