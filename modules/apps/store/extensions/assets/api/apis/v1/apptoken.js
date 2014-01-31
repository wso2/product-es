var resource = (function () {

    var log = new Log('token-api');
    var KeyService = require('/extensions/assets/api/services/key.js').serviceModule;

    var keyApi = new KeyService.KeyService();
    keyApi.init(jagg, session);


    var generateToken = function (context) {

        var parameters = context.request.getAllParameters();
        var accessAllowDomains = parameters.accessAllowDomains.split(',') || [];
        log.info(parameters);
        log.info(accessAllowDomains);
        var key = keyApi.generateApplicationKey({
            username: 'admin',
            appName: parameters.appName,
            keyType: parameters.keyType,
            callbackUrl: parameters.callbackUrl,
            accessAllowDomains: accessAllowDomains,
            validityTime: parameters.validityTime
        });

        log.info(key);

        return key;
    };


    var refreshToken = function (context) {

        var parameters = request.getContent();
        var accessAllowDomains = parameters.accessAllowDomains.split(',') || [];
        log.info(accessAllowDomains);

        var key = keyApi.refreshToken({
            username: 'admin',
            appName: parameters.appName,
            keyType: parameters.keyType,
            oldAccessToken: parameters.oldAccessToken,
            accessAllowDomains: accessAllowDomains,
            clientId: parameters.clientId,
            clientSecret: parameters.clientSecret,
            validityTime: parameters.validityTime
        });

        log.info(key);

        return key;
    };

    return{
        post: generateToken,
        put: refreshToken
    };
})();