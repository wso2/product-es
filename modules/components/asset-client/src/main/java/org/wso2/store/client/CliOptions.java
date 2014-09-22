package org.wso2.store.client;

import javax.swing.Icon;
import com.google.code.commons.cli.annotations.CliOption;

public class CliOptions {
	
	@CliOption(opt = "host", hasArg = true)
	private String hostName = IConstants.DEFAULT_HOST_NAME;

	@CliOption(opt = "port", hasArg = true)
	private String port = IConstants.DEFAULT_PORT;
	
	@CliOption(opt = "user", hasArg = true)
	private String userName = IConstants.DEFAULT_USER;
	
	@CliOption(opt = "pwd", hasArg = true)
	private String pwd = IConstants.DEFAULT_PWD;
	
	@CliOption(opt = "context", hasArg = true)
	private String context = IConstants.DEFAULT_CONTEXT;
	
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
		System.out.println("hostName>>>"+hostName);
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
