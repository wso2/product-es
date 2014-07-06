var artifacts=function(ctx){
	return{
		create:null,
		search:null,
		remove:null,
		update:null,
		invokeLifecycleAction:null
	};
};

var configure=function(ctx){

	return{
		overview:{

			name:{

			},
			version:{

			}
		}

	};
};

var render=function(ctx){
	return {

	};
};

var application=function(){
	return{
		onTenantCreate:function(){},
		onTenantLoad:function(){}
	};
};