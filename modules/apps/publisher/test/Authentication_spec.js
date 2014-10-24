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

var encoder = require('utils').base64;
var conf = require("conf.json");

describe('Authentication End Point - Publisher API', function () {

    /*
     * Endpoint: /publisher/apis/authenticate
     * Method: POST
     * Response: success message
     * test: message return
     */
    it('Test valid username:password', function () {
        var username = conf.authConfiguration.username;
        var password = conf.authConfiguration.password;
        var server_url = conf.StoreConfigurations.url;
        var url = server_url + '/authenticate';

        try {
            var response = post(url, {"password": password, "username": username}, {}, 'json');
        } catch (e) {
            log.debug(e);
        } finally {
            expect(response.data.data).not.toBe(undefined);
            var header = {'cookie': "JSESSIONID=" + response.data.data.sessionId + ';'};
            post(server_url + '/logout', {}, header, 'json');
        }
    });

    /*
     * Endpoint: /publisher/apis/authenticate
     * Method: POST
     * Response: success message
     * test: sessionID
     */
    it('Test valid username:password response data', function () {
        var username = conf.authConfiguration.username;
        var password = conf.authConfiguration.password;
        var server_url = conf.StoreConfigurations.url;
        var url = server_url + '/authenticate';
        try {
            var response = post(url, {"password": password, "username": username}, {}, 'json');
        } catch (e) {
            log.debug(e);
        } finally {
            expect(response.data.data.sessionId).not.toBe(undefined);
            expect(response.data.data).not.toEqual(jasmine.objectContaining(jasmine.any(Object)));
            var header = {'cookie': "JSESSIONID=" + response.data.data.sessionId + ';'};
            post(server_url + '/logout', {}, header, 'json');
        }
    });

    /*
     * Endpoint: /publisher/apis/authenticate
     * Method: POST
     * Response: error message
     * test: message "username/password is incorrect"
     */
    it('Test invalid Username', function () {
        var username = 'asdefkcn';
        var password = conf.authConfiguration.password;
        var server_url = conf.StoreConfigurations.url;
        var url = server_url + '/authenticate';

        try {
            var response = post(url, {"password": password, "username": username}, {}, 'json');
        } catch (e) {
            log.debug(e);
        } finally {
            expect(response.data.data).toBe(undefined);
            expect(response.data.error).toEqual("username/password is incorrect");
        }
    });

    /*
     * Endpoint: /publisher/apis/authenticate
     * Method: POST
     * Response: error message
     * test: message "username/password is incorrect"
     */
    it('Test invalid password', function () {
        var username = conf.authConfiguration.username;
        var password = 'aaaaaaaaaaaaaa';
        var server_url = conf.StoreConfigurations.url;
        var url = server_url + '/authenticate';

        try {
            var response = post(url, {"password": password, "username": username}, {}, 'json');
        } catch (e) {
            log.debug(e);
        } finally {
            expect(response.data.data).toBe(undefined);
            expect(response.data.error).toEqual("username/password is incorrect");
        }
    });

    /*
     * Endpoint: /publisher/apis/authenticate
     * Method: POST
     * Response: error message
     * test: message "username/password is incorrect"
     */
    it('Test invalid username:password', function () {
        var username = 'fjewk';
        var password = 'kfewhjkw';
        var server_url = conf.StoreConfigurations.url;
        var url = server_url + '/authenticate';

        try {
            var response = post(url, {"password": password, "username": username}, {}, 'json');
        } catch (e) {
            log.debug(e);
        } finally {
            expect(response.data.data).toBe(undefined);
            expect(response.data.error).toEqual("username/password is incorrect");
        }
    });

    /*
     * Endpoint: /publisher/apis/authenticate
     * Method: POST
     * Response: error message
     * test: message "Username and Password must be provided"
     */
    it('Test empty username:password', function () {
        var username = '';
        var password = '';
        var server_url = conf.StoreConfigurations.url;
        var url = server_url + '/authenticate';

        try {
            var response = post(url, {"password": password, "username": username}, {}, 'json');
        } catch (e) {
            log.debug(e);
        } finally {
            expect(response.data.data).toBe(undefined);
            expect(response.data.error).toEqual("Username and Password must be provided");
        }
    });

});