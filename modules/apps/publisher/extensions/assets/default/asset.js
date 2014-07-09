manager=function(ctx){
	return{
		create:function(){ 
			log.info('Create called!');
		},
	};
};

server=function(ctx){
	return{
		onUserLoggedIn:function(){

		}
	}
};

configure=function(ctx){

	return{
		overview:{

			name:{
				validate:function(){

				}
			},
			version:{

			}
		}

	};
};

ui=function(ctx){
	return {
		renderCreate:function(){},
		renderEdit:function(){},
		renderLifecycle:function(){},
		renderNavigation:function(){}
	};
};
