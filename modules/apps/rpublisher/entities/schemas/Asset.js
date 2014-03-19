var entity = require('entity');


var Asset = new entity.Schema('Asset', {
    name: String,
    id: String,
    type: String,
    description: String,
    attributes: {
        overview_name: {type: String, default: 'test'},
        overview_author: String,
        overview_category: String
    }
});

Asset.field('name').validation(function (fieldValue, fieldSchema) {
    var log = new Log();
    log.info('Checking the name');
}, 'This is an offensive name');

Asset.to('save',function(entity,next){
    var log=new Log();
    log.info('Save success');
    next();
    //next({msg:'Failed'});
});

Asset.to('save',function(err,entity,next){
    var log=new Log();
    log.info('Saving failed'+err);
});

