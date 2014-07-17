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

describe('Assets Life-Cycle Retrival - Store API', function () {
       

        it('Test retrieve lifecycle by asset id', function () {
            var username = conf.authConfiguration.username;
            var password = conf.authConfiguration.password;
            var server_url = conf.StoreConfigurations.url;

            var code = username + ":" + password;
            var auth = encoder.encode(code);
            var header = {"Authorization":"Basic "+auth};
            var assetId = conf.assetConfiguration.id;

            var url = server_url+'v2/assets/'+assetId+'/lifecycle';
            var data ={};
            
            try {
                var response = get(url, data, header ,"json");
            } catch (e) {
                //log.error(e);
            } finally {
                expect(response.data).not.toBe(null);                
            }
           
        });

        it('Test retrieve lifecycle actions by asset id', function () {
            var username = conf.authConfiguration.username;
            var password = conf.authConfiguration.password;
            var server_url = conf.StoreConfigurations.url;

            var code = username + ":" + password;
            var auth = encoder.encode(code);
            var header = {"Authorization":"Basic "+auth};
            var assetId = conf.assetConfiguration.id;

            var url = server_url+'v2/assets/'+assetId+'/lifecycle/actions';
            var data ={};
            
            try {
                var response = get(url, data, header ,"json");
            } catch (e) {
                //log.error(e);
            } finally {
                expect(response.data).not.toBe(null);
            }
           
        });

        it('Test retrieve LC history by asset id name', function () {
            var username = conf.authConfiguration.username;
            var password = conf.authConfiguration.password;
            var server_url = conf.StoreConfigurations.url;

            var code = username + ":" + password;
            var auth = encoder.encode(code);
            var header = {"Authorization":"Basic "+auth};
            var assetId = conf.assetConfiguration.id;

            var url = server_url+'v2/assets/'+assetId+'/lifecycle/history';
            var data ={};
            
            try {
                var response = get(url, data, header ,"json");
            } catch (e) {
                //log.error(e);
            } finally {
                expect(response.data).not.toBe(null);
            }
           
        });

        it('Test retrieve LC comments by asset id name', function () {
            var username = conf.authConfiguration.username;
            var password = conf.authConfiguration.password;
            var server_url = conf.StoreConfigurations.url;

            var code = username + ":" + password;
            var auth = encoder.encode(code);
            var header = {"Authorization":"Basic "+auth};
            var assetId = conf.assetConfiguration.id;
            var url = server_url+'v2/assets/'+assetId+'/lifecycle/comments';
            var data ={};
            
            try {
                var response = get(url, data, header ,"json");
            } catch (e) {
                //log.error(e);
            } finally {
                expect(response.data).not.toBe(null);
            }
           
        });

        it('Test retrieve asset lifecycle by id', function () {
            var username = conf.authConfiguration.username;
            var password = conf.authConfiguration.password;
            var server_url = conf.StoreConfigurations.url;

            var code = username + ":" + password;
            var auth = encoder.encode(code);
            var header = {"Authorization":"Basic "+auth};
            var assetId = conf.assetConfiguration.id;
            var url = server_url+'/assets/'+assetId+'/lifecycle';
            var data ={};
            
            try {
                var response = get(url, data, header ,"json");
            } catch (e) {
                //log.error(e);
            } finally {
                expect(response.data).not.toBe(null);
            }
           
        });
});

describe('Assets Life-Cycle PUT - Store API', function () {       

        it('Test PUT LC comments for transition by id', function () {
            var username = conf.authConfiguration.username;
            var password = conf.authConfiguration.password;
            var server_url = conf.StoreConfigurations.url;

            var code = username + ":" + password;
            var auth = encoder.encode(code);
            var header = {"Authorization":"Basic "+auth};
            var assetId = conf.assetConfiguration.id;

            var url = server_url+'v2/assets/'+assetId+'/lifecycle/comments';
            var data ={};
           

            try {
                var response = post(url, data, header ,"json");
            } catch (e) {
                //log.error(e);
            } finally {
                expect(response.data).not.toBe(null); 
                //write proper test                
            }
           
        });
    });
        
