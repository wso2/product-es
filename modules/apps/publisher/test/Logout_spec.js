/*
 * Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

var conf = require("conf.json");

describe('Logout END Point - Publisher API', function () {

    /*
     * Endpoint: /publisher/apis/logout
     * Method: POST
     * Response: success message
     * test: message return correctly
     */
    it('Test logout with valid SessionId', function () {
        var username = conf.authConfiguration.username;
        var password = conf.authConfiguration.password;
        var server_url = conf.StoreConfigurations.url;
        var url = server_url + '/logout';
        var login = post(server_url + '/authenticate', {"password": password, "username": username}, {}, 'json');
        var header = {'cookie': "JSESSIONID=" + login.data.data.sessionId + ';'};
        try {
            var response = post(url, {"password": password, "username": username}, header, 'json');
        } catch (e) {
            log.debug(e);
        } finally {
            expect(response.data.data.message).not.toBe(undefined);
        }

    });

    /*
     * Endpoint: /publisher/apis/logout
     * Method: POST
     * Response: success message
     * test: message "User Logged out succesfully"
     */
    it('Test logout valid SessionId: response data', function () {
        var username = conf.authConfiguration.username;
        var password = conf.authConfiguration.password;
        var server_url = conf.StoreConfigurations.url;
        var url = server_url + '/logout';

        var login = post(server_url + '/authenticate', {"password": password, "username": username}, {}, 'json');
        var header = {'cookie': "JSESSIONID=" + login.data.data.sessionId + ';'};
        try {
            var response = post(url, {}, header, 'json');
        } catch (e) {
            log.debug(e);
        } finally {
            expect(response.data.data.message).not.toBe(undefined);
            expect(response.data.data.message).toEqual('User Logged out successfully');
        }

    });

    /*
     * Endpoint: /publisher/apis/logout
     * Method: POST
     * Response: success message
     * test: message 'Unable to logout user!'
     */
    it('Test logout invalid SessionId', function () {
        var username = conf.authConfiguration.username;
        var password = conf.authConfiguration.password;
        var server_url = conf.StoreConfigurations.url;
        var url = server_url + '/logout';

        var login = post(server_url + '/authenticate', {"password": password, "username": username }, {}, 'json');
        var header = {'cookie': "JSESSIONID=" + login.data.data.sessionId + 'AHJSKASHJK;'};
        try {
            var response = post(url, {}, header, 'json');
        } catch (e) {
            log.debug(e);
        } finally {
            expect(response.data.data).toBe(undefined);
            expect(response.data.error).toEqual('Unable to logout user!');
            post(url, {}, {'cookie': "JSESSIONID=" + login.data.data.sessionId + ';'}, 'json');
        }

    });

});