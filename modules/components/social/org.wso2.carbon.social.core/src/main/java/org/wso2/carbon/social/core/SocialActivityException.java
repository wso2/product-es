package org.wso2.carbon.social.core;

@SuppressWarnings("serial")
public class SocialActivityException extends Exception {

	public SocialActivityException(String errorMessage) {
		super(errorMessage);
	}

	public SocialActivityException(String errorMessage, Throwable e) {
		super(errorMessage, e);
	}

}
