var log=new Log('assets-main');

log.info('Assets main has been called');

var app=require('/modules/fiber.js').getApp();

app.services.register('assetRegistry',function(){
     var handle=function(context,handlers){
            var log=new Log('assets-registry');
            log.info('This is a component');
           // handlers();
     };

     return{
         handle:handle
     };
});

var svc=app.services.get('assetRegistry');

svc.handle();

//var app=require('/modules/fiber.js').getApp();

/*app.services.register('assetRegistry',function(){

      var list=[];
      var addAssetType=function(type){
         list.push(type);
      };

      var printAssetTypes=function(){

      };

      return{
         addAssetType:addAssetType
      };
});*/