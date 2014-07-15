/*
* *
* * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
* *
* * Licensed under the Apache License, Version 2.0 (the "License");
* * you may not use this file except in compliance with the License.
* * You may obtain a copy of the License at
* *
* * http://www.apache.org/licenses/LICENSE-2.0
* *
* * Unless required by applicable law or agreed to in writing, software
* * distributed under the License is distributed on an "AS IS" BASIS,
* * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* * See the License for the specific language governing permissions and
* * limitations under the License.
*
*/
var encoder = require("/modules/base64.js");
var conf = require("conf.json");

describe('Authentication - Store API', function () {
       

        it('Test valid username:password', function () {
            var username = conf.authConfiguration.username;
            var password = conf.authConfiguration.password;
            var server_url = conf.StoreConfigurations.url;

            var code = username + ":" + password;
            var auth = encoder.encode(code);
            var header = {"Authorization":"Basic "+auth};
            var url = server_url+'v2/assets/gadget';
            var data ={};
           
            try {
                var response = get(url, data, header ,"json");
            } catch (e) {
                //log.error(e);
            } finally {
                expect(response).not.toBe(null);
                expect(response).not.toBe(undefined);
            }
           
        });

        it('Test valid username:password response data', function () {
            var username = conf.authConfiguration.username;
            var password = conf.authConfiguration.password;
            var server_url = conf.StoreConfigurations.url;

            var code = username + ":" + password;
            var auth = encoder.encode(code);
            var header = {"Authorization":"Basic "+auth};
            var url = server_url+'v2/assets/gadget';
            var data ={};
            
            try {
                var response = get(url, data, header ,"json");
            } catch (e) {
                //log.error(e);
            } finally {
                expect(response.data).not.toBe(null);
            }
           
        });


        it('Test invalid Username', function () {
            var username = 'asdefkcn';
            var password = conf.authConfiguration.password;
            var server_url = conf.StoreConfigurations.url;

            var code = username + ":" + password;
            var auth = encoder.encode(code);
            var header = {"Authorization":"Basic "+auth};
            var url = server_url+'v2/assets/gadget';
            var data ={};

           
            try {
                var response = get(url, data, header ,"json");
            } catch (e) {
                //log.error(e);
            } finally {
                expect(response).toBe(undefined);
            }
            
        });

        it('Test invalid password', function () {
            var username = conf.authConfiguration.username;
            var password = '';
            var server_url = conf.StoreConfigurations.url;

            var code = username + ":" + password;
            var auth = encoder.encode(code);
            var header = {"Authorization":"Basic "+auth};
            var url = server_url+'v2/assets/gadget';
            var data ={};

            try {
                var response = get(url, data, header ,"json");
            } catch (e) {
                //log.error(e);
            } finally {
                expect(response).toBe(undefined);
            }
        });

        it('Test invalid username:password', function () {
            var username = 'fjewk';
            var password = 'kfewhjkw';
            var server_url = conf.StoreConfigurations.url;

            var code = username + ":" + password;
            var auth = encoder.encode(code);
            var header = {"Authorization":"Basic "+auth};
            var url = server_url+'v2/assets/gadget';
            var data ={};

            try {
                var response = get(url, data, header ,"json");
            } catch (e) {
                //log.error(e);
            } finally {
                expect(response).toBe(undefined);
            }
        });
});