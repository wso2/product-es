app.dependencies=['es_common'];
app.ignoreExtension=true;

app.server=function(ctx){

	return{
		endpoints:{
			pages:[
				{
					title:'WSO2 GREG Top Assets',
					url:'top-assets',
					path:'top_assets.jag'
				}
			]

		}

	}

};