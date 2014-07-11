var render=function(theme,data,meta,require){
	theme('single-col-fluid',{
        title: 'Assets',
     	header: [
            {
                partial: 'header',
                context: data
            }
        ]
    });
};