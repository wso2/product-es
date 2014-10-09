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
 * Description: The response of the currently invoked api enpoint is organized
 */

var exception = {};
var log = new Log('exception_module');

(function(exception) {

    exception.buildExceptionObject = function(message, code) {
        var error = {};
        error.message = message;
        error.code = code;
        return error;
    };

    exception.handleError = function (exception, type, code){
        var constants = require('rxt').constants;

        if (type == constants.THROW_EXCEPTION_TO_CLIENT) {
            log.debug(exception);
            var e = exceptionModule.buildExceptionObject(exception, code);
            throw e;
        } else if (type == constants.LOG_AND_THROW_EXCEPTION) {
            log.error(exception);
            throw exception;
        } else if (type == constants.LOG_EXCEPTION_AND_TERMINATE) {
            log.error(exception);
            var msg = 'An error occurred while serving the request!';
            var e = exceptionModule.buildExceptionObject(msg, constants.ERROR_STATUS_CODES.INTERNAL_SERVER_ERROR);
            throw e;
        } else if (type == constants.LOG_EXCEPTION_AND_CONTINUE) {
            log.debug(exception);
        }
        else {
            log.error(exception);
            throw e;
        }
    };
}(exception))

