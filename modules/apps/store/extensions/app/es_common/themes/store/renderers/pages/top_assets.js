var render = function(theme, data, meta, require) {
	var log=new Log();
	log.info('Calling theme');
    theme('2-column-right', {
        body: [{
            partial: 'top_assets',
            context: {}
        }]
    });
};