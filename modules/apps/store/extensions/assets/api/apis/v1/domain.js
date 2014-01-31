var resource=(function(){

    var log=new Log('domain-api');


    /*
    The function updates all of the domains
     */
    var updateDomains=function(context){

        log.info('updating domains');

        var KeyService=require('/extensions/assets/api/services/key.js').serviceModule;


        var keyApi=new KeyService.KeyService();

        keyApi.init(jagg,session);

        var parameters=request.getContent();

        var accessAllowDomains=parameters.accessAllowedDomains.split(',')||[];

        log.info(parameters);

        var result=keyApi.updateAccessAllowDomains({
            accessToken:parameters.accessToken,
            accessAllowDomains:accessAllowDomains
        });

        log.info(result);

    };

    return{
        put:updateDomains
    };

})();