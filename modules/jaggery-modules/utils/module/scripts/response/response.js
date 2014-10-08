/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
/**
 * Description: The response of the currently invoked api endpoint is organized
 */

var response = {};
var log = new Log("response");

(function(response) {

    /**
     * Build Error response
     * @param  {response}   resp   jaggery-response object to retrieve to client
     * @param  {int}        code        status code
     * @param  {string}     message  message to the client side
     * @return {response}   return response
     */
    response.buildErrorResponse = function(resp,code,message) {
        var content={};
        content.error = message;
        resp = processResponse(resp,code,content);
        return resp;
    };

    /**
     * Build success response
     * @param  {response}   resp jaggery response object
     * @param  {int}        code    status code
     * @param               data    the result to client
     * @return {response}   return response
     */
    response.buildSuccessResponse= function(resp, code, data){
        var content={};
        //var dataOut = data;
        content.data = data;
        resp = processResponse(resp,code,content);
        return resp;
    };

    /**
     * process General response
     * @param  {response}  resp  jaggery response
     * @param  {int}       code  status code
     * @param  {JSON}      data  success result
     * @return {response}  resp  jaggery response
     */
    response.buildSuccessResponseForRxt= function(resp, code, data){
        resp.contentType = 'application/json';
        resp.status = code;
        resp.content = data;
        return resp;
    };

    /**
     * General response builder
     * @param  {response}   resp jaggery response
     * @param  {int}        code status code
     * @param  {JSON}       content what ever the content to be sent as response
     * @return {response}   resp jaggery response
     */
    function processResponse(resp, code, content){
        log.info("Building Response");
        resp.status = code;
        resp.contentType = 'application/json';
        resp.content = content;
        return resp;

    };
}(response))