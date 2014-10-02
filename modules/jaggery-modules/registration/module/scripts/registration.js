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
(function() {

	var log = new Log();

	var ws = require('ws');
	var request = new ws.WSRequest();
	var options = new Array();
	this.urlWs = "https://localhost:9443/services/UserAdmin/";



	this.addNewUser = function(skey, userName, password) {
		//log.info("module " + skey + userName + password)
		options.useSOAP = 1.2;
		options.useWSA = 1.0;
		options.action = "urn:addUser";
		options["HTTPHeaders"] = [{
			name : "Cookie",
			value : "JSESSIONID=" + skey
		}];
		var payload = '<mgt:addUser xmlns:mgt="http://mgt.user.carbon.wso2.org"><mgt:userName>' + userName + '</mgt:userName><mgt:password>' + password + '</mgt:password></mgt:addUser>';
		var result;

		try {
			request.open(options, this.urlWs, false);
			request.send(payload);
			return {
				"error" : false,
				"addNewUser" : true,
				"userName" : userName
			};
		} catch (e) {
			log.error(e.toString());
			return {
				"error" : true,
				"errorMsg" : e.toString()
			};
		}

	};



	this.changeMyPassword = function(skey, oldPassword, newPassword) {
		//log.info("module " + skey + oldPassword + newPassword)
		options.useSOAP = 1.2;
		options.useWSA = 1.0;
		options.action = "urn:changePasswordByUser";
		options["HTTPHeaders"] = [{
			name : "Cookie",
			value : "JSESSIONID=" + skey
		}];
		var payload = '<mgt:changePasswordByUser xmlns:mgt="http://mgt.user.carbon.wso2.org"><mgt:oldPassword>' + oldPassword + '</mgt:oldPassword><mgt:newPassword>' + newPassword + '</mgt:newPassword></mgt:changePasswordByUser>';
		var result;

		try {
			request.open(options, this.urlWs, false);
			request.send(payload);
			return {
				"error" : false,
				"changePassword" : true
			};
		} catch (e) {
			log.error(e.toString());
			return {
				"error" : true,
				"errorMsg" : e.toString()
			};
		}

	};
	
})();
