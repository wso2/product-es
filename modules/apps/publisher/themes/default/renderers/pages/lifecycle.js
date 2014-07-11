var render=function(theme,data,meta,require){
	theme('single-col-fluid',{
        title: 'Lifecycle',
     	header: [
            {
                partial: 'header',
                context: data
            }
        ]
    });
};