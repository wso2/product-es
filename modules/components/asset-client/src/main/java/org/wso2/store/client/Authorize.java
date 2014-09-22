package org.wso2.store.client;


////{"code" : 200, "message" : "Logged in successfully", "data" : {"sessionId" : "4F1A2351985E8987880A1B4465FE7D81"}}
public class Authorize {
	
	private Integer code;
	private String message;
	private AuthorizeData data;
	

	public Integer getCode() {
		return code;
	}
	public void setCode(Integer code) {
		this.code = code;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public AuthorizeData getData() {
		return data;
	}
	public void setData(AuthorizeData data) {
		this.data = data;
	}

}
