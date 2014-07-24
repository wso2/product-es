var request = {};
(function(request) {
    var hasOwnProperty = function(obj, element) {
        return Object.prototype.hasOwnProperty.call(obj, element);
    };
    var isObject = function(object) {;
        return typeof object === 'object';
    };
    /*
     * ECMA Standard (ECMA-262 : 5.1 Edition)*/
    var decodes = function(encodedURI) {
        return decodeURIComponent(encodedURI);
    };
    request.getQueryOptions = function(queryString) {
        var sep = opt.sep || '&',
            assign = opt.assign || '=',
            compoArray = [];
        var obj = {};
        var decodedURI = decodes(queryString);
        decodedURI.split(sep).forEach(function(comp) {
            comp.split(assign).some(function(element, index, array) {
                if (hasOwnProperty(obj, element.toString())) {
                    compoArray.push(obj[element]);
                    compoArray.push(array[1]);
                    obj[element] = compoArray;
                } else {
                    Object.defineProperty(obj, element, {
                        enumerable: true,
                        writable: true,
                        value: array[1]
                    });
                }
                return true;
            });
        });
        return obj;
    };
}(request))