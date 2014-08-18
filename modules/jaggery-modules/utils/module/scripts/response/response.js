/**
 * Description: The response of the currently invoked api enpoint is organized
                 */

var response = {};
(function(response) {
    
    //on error - response
    response.buildErrorResponse = function(resp,code,message) {
        var obj={};
        //obj.code = code;
        obj.error = message;   
        resp.status = code;
        resp.content = obj;  
        resp.contentType = 'application/json';
        return resp;
    };
    
    //on sucess - response
    response.buildSuccessResponse= function(resp, code, data){
        var obj={};
        //obj.code = code;
        //obj.message = message;  
        var dataOut = [];
        dataOut =  data;
        obj.data = dataOut; 
        resp.status = code;
        resp.content = obj;     
        return resp;
    };
    
}(response))