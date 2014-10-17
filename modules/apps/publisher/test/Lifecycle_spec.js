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
var authenticate, sessionId;
var username = conf.authConfiguration.username;
var password = conf.authConfiguration.password;
var server_url = conf.StoreConfigurations.url;

/**
 * Groups Test cases for Lifecycle GET endpoints
 */
describe('Lifecycle GET - Publisher API', function () {

    /*
     * Endpoint: /publisher/apis/lifecycles
     * Method: GET
     * Response: success message
     * test: message return
     */
    it('Test get lifecycle list', function () {
        var url = server_url + '/lifecycles';
        authenticate = post(server_url + '/authenticate', {"password": password, "username": username }, {}, 'json');
        var header = {'Cookie': "JSESSIONID=" + authenticate.data.data.sessionId + ";"};
        try {
            var response = get(url, {}, header, 'json');
        } catch (e) {
            log.debug(e);
        } finally {
            post(server_url + '/logout', {}, header, 'json');
            expect(response.data.data).not.toBe(undefined);
        }
    });

    /*
     * Endpoint: /publisher/apis/lifecycles
     * Method: GET
     * Response: success message
     * test: message return
     */
    it('Test get lifecycle definition by name', function () {
        var url = server_url + '/lifecycles/SampleLifeCycle2';
        authenticate = post(server_url + '/authenticate', {"password": password, "username": username }, {}, 'json');
        var header = {'Cookie': "JSESSIONID=" + authenticate.data.data.sessionId + ";"};
        try {
            var response = get(url, {}, header, 'json');
        } catch (e) {
            log.debug(e);
        } finally {
            post(server_url + '/logout', {}, header, 'json');
            expect(response.data.data).not.toBe(undefined);

        }
    });

    /*
     * Endpoint: /publisher/apis/lifecycles
     * Method: GET
     * Response: success message
     * test: message return
     */
    it('Test get lifecycle state details by lifecyclestate name', function () {
        var url = server_url + '/lifecycles/SampleLifeCycle2/Created';
        authenticate = post(server_url + '/authenticate', {"password": password, "username": username }, {}, 'json');
        var header = {'Cookie': "JSESSIONID=" + authenticate.data.data.sessionId + ";"};
        try {
            var response = get(url, {}, header, 'json');
        } catch (e) {
            log.error(e);
        }
        finally {
            post(server_url + '/logout', {}, header, 'json');
            expect(response.data.data.nextStates[0].state).toEqual('In-Review');
        }
    });

});
    

        
