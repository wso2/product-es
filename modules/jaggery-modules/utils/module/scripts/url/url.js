var url = {};
(function() {
	var log=new Log('utils-url');
    url.popServerDetails = function(obj) {
        var process = require('process');
        var localIP = process.getProperty('server.host');
        var httpPort = process.getProperty('http.port');
        var httpsPort = process.getProperty('https.port');
        var value = '';

        for (var key in obj) {
            if (obj.hasOwnProperty(key)) {
                value = obj[key];
                log.info('Value before transformation : '+value);
                if ((typeof value === 'string') && value.indexOf('%https.host%') > -1) {
                    value=value.replace('%https.host%', 'https://' + localIP + ':' + httpsPort);
                } else if ((typeof value === 'string') && value.indexOf('%http.host%') > -1) {
                    value=value.replace('%http.host%', 'http://' + localIP + ':' + httpPort);
                } else if ((typeof value === 'string') && value.indexOf('%https.carbon.local.ip%') > -1) {
                    value=value.replace('%https.carbon.local.ip%', 'https://' + carbonLocalIP + ':' + httpsPort);
                } else if ((typeof value === 'string') && value.indexOf('%http.carbon.local.ip%') > -1) {
                    value=value.replace('%http.carbon.local.ip%', 'http://' + carbonLocalIP + ':' + httpPort);
                }
                obj[key] = value;
                log.info('Value after transformation : '+value);
            }
        }
        return obj;
    };
}(url));