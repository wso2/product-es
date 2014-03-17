var log=new Log('api-asset');

log.info('Asset API deployed');

var router=require('router');

router.app.get('/assets',function(){
   print('ASSETS');
});