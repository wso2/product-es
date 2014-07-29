/**
 * Description: The response of the currently invoked api enpoint is organized
                 */

var response = {};
(function(response) {
    
    //on error - response
    response.buildErrorResponse = function(code, message) {
        var obj={};
        obj.code = code;
        obj.message = message;       
        return obj;
    };
    
    //on sucess - response
    response.buildSuccessResponse= function(code, message, data){
        var obj={};
        obj.code = code;
        obj.message = message;  
        var dataOut = [];
        dataOut =  data;
        obj.data = dataOut;    
        return obj;
    };
    
}(response))