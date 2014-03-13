var fiberRouteRegister = function (fiber, options) {
    var log = new Log('fiber-route-register');
    fiber.events.on('routes', 'init', function (context) {
         log.info(stringify(context));

         //Get all files within the directory
         var subs=context.dir.listFiles();

         var path=context.extension.path+'/'+context.dir.getName()+'/';
         for(var index in subs){
             log.info('Routes '+subs[index].getName());
             require(path+subs[index].getName());
         }
    });
};