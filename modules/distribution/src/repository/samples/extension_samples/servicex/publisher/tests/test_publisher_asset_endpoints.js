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
var configs = require('test_publisher_asset_configs.json');
var baseUrl = configs.baseUrl;
var username = configs.authConfiguration.username;
var password = configs.authConfiguration.password;
describe('ES Publisher Extension - API Tests', function() {
    it('Test calls the new asset extension API endpoint', function() {
        var url = baseUrl.https + '/assets/servicex/apis/new_api';
        var response;
        var header = obtainAuthorizedHeaderForAPICall();
        var log = new Log();
        try {
            response = get(url, {}, header, 'json');
        } catch (e) {
            log.error(e);
        } finally {
            logoutAuthorizedUser(header);
            expect(response.data.message).toBe('new_api');
        }
    });
});
/**
 *
 * @return {{Cookie: string}}
 */
var obtainAuthorizedHeaderForAPICall = function() {
    var authenticate = post(baseUrl.https + '/apis/authenticate', {
        "password": password,
        "username": username
    }, {}, 'json');
    var header = {
        'Cookie': "JSESSIONID=" + authenticate.data.data.sessionId + ";"
    };
    return header
};
/**
 * The function to send logout request to publisher API
 * @param header
 */
var logoutAuthorizedUser = function(header) {
    post(baseUrl.https + '/apis/logout', {}, header, 'json');
};