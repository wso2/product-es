var logger=function(fiber,options){
    var log=new Log('logger');

    fiber.events.on('*','*',function(context){
            log.info('Target: '+context.target+' action: '+context.action);
    });
};