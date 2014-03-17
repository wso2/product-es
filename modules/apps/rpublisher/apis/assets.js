var log=new Log('api-asset');

log.info('Asset API deployed');

var router=require('router');

router.app.get('/api/assets/:type',function(req,res,session){
   print('API for asset: '+req.params.type);
});