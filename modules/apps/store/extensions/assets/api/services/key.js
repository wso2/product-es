/*
 Description: The script is used to manage the keys for a given application subscription
 Filename: key.js
 Created Date: 20/1/2014
 */
var serviceModule = (function () {

    var log = new Log('keyservice');

    function KeyService() {
        this.instance = null;
    }

    KeyService.prototype.init = function (context, session) {
        this.instance = context.module('manager').getAPIStoreObj();
    };

    /*
     options.apiProvider
     options.apiName
     options.apiVersion
     options.appContext
     options.appName
     options.username
     options.keyType
     options.callbackUrl
     */
    KeyService.prototype.generateAPIKey = function (options) {
        var result = this.instance.getKey(options.apiProvider,
            options.apiName,
            options.apiVersion,
            options.appContext,
            options.appName,
            options.username,
            options.keyType,
            options.callbackUrl);

        return result.key;
    };

    /*
     options.username:
     options.appName:
     options.keyType
     options.callbackUrl
     options.accessAllowDomains
     options.validityTime
     */
    KeyService.prototype.generateApplicationKey = function (options) {
        log.info(options);
        var result = this.instance.getApplicationKey(options.username, options.appName, options.keyType, options.callbackUrl,
            options.accessAllowDomains, options.validityTime);
        return result;
    };

    /*
     options.username
     options.appName
     options.keyType
     options.oldAccessToken
     options.accessAllowDomains
     options.clientId = consumerKey
     options.clientSecret = consumerSecret
     options.validityTime
     */
    KeyService.prototype.refreshToken = function (options) {

        log.info(options);
        var key = this.instance.refreshToken(options.username,
            options.appName,
            options.keyType,
            options.oldAccessToken,
            options.accessAllowDomains,
            options.clientId,
            options.clientSecret,
            options.validityTime);

        return key;
    };

    /*
     options.oldAccessToken
     options.accessAllowDomains
     */
    KeyService.prototype.updateAccessAllowDomains = function (options) {
        log.info(options);
        var accessAllowDomains = this.instance.updateAccessAllowDomains(options.accessToken,
            options.accessAllowDomains);
    };

    return{
        KeyService: KeyService
    }

})();