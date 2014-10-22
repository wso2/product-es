/*

	* Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
	* WSO2 Inc. licenses this file to you under the Apache License,
	* Version 2.0 (the "License"); you may not use this file except
	* in compliance with the License.
	* You may obtain a copy of the License at
	* http://www.apache.org/licenses/LICENSE-2.0
	* Unless required by applicable law or agreed to in writing,
	* software distributed under the License is distributed on an
	* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
	* KIND, either express or implied. See the License for the
	* specific language governing permissions and limitations
	* under the License.
	*/

package org.wso2.store.client;

import com.google.code.commons.cli.annotations.CliOption;

public class CliOptions {
	
	@CliOption(opt = "host", hasArg = true)
	private String hostName = Constants.DEFAULT_HOST_NAME;

	@CliOption(opt = "port", hasArg = true)
	private String port = Constants.DEFAULT_PORT;
	
	@CliOption(opt = "user", hasArg = true)
	private String userName = Constants.DEFAULT_USER;
	
	@CliOption(opt = "pwd", hasArg = true)
	private String pwd = Constants.DEFAULT_PWD;
	
	@CliOption(opt = "context", hasArg = true)
	private String context = Constants.DEFAULT_CONTEXT;
	
	@CliOption(opt = "location", hasArg = true)
	private String location ;
	

	

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}


}
