asset.server=function(){
	return{
		endpoints:{
			apis:[{
				url:'new_api',
				path:'new_api.jag'
			}],
			pages:[{
				title:'New Servicex Page',
				url:'new_page',
				path:'new_page.jag'
			},{
				title:'Servicex Listing Page',
				url:'list',
				path:'list.jag'
			}]
		}
	};
};