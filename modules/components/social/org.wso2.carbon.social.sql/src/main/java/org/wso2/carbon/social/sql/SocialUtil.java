/*
 * Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.social.sql;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.social.core.SocialActivityException;
import org.wso2.carbon.utils.CarbonUtils;

public class SocialUtil {
	private static final Log log = LogFactory.getLog(SocialUtil.class);

	public static String getTenantDomain() {
		String tenantDomainName = CarbonContext.getThreadLocalCarbonContext()
				.getTenantDomain();
		return tenantDomainName;

	}

	public static int getActivityLimit(int limit) {

		if (limit > Constants.MAXIMUM_ACTIVITY_SELECT_COUNT || limit == 0) {
			if (log.isDebugEnabled()) {
				log.debug("Provided limit: " + limit
						+ " exceeds default max limit: "
						+ Constants.MAXIMUM_ACTIVITY_SELECT_COUNT);
			}
			return Constants.MAXIMUM_ACTIVITY_SELECT_COUNT;
		} else {
			return limit;
		}
	}

	public static String getPreviousActivityID(String PreviousActivityID) {
		if (PreviousActivityID.isEmpty()) {
			return PreviousActivityID;
		} else {
			return null;
		}
	}

	public static String getSocialAdaptorImplClass() throws IOException,
			SocialActivityException {
		String configPath = CarbonUtils.getCarbonHome() + File.separator
				+ "repository" + File.separator + "conf" + File.separator
				+ "social.xml";
		File socialXML = new File(configPath);
		try {
			InputStream inStream = new FileInputStream(socialXML);
			OMElement root = OMXMLBuilderFactory.createOMBuilder(inStream)
					.getDocumentElement();

			OMElement QueryAdaptorClass = root.getFirstChildWithName(new QName(
					"QueryAdaptorClass"));

			if (QueryAdaptorClass == null) {
				throw new SocialActivityException(
						"No <QueryAdaptorClass> element found within "
								+ configPath);
			}

			if (log.isDebugEnabled()) {
				log.debug("QueryAdaptorClass in use is: "
						+ QueryAdaptorClass.getText());
			}

			inStream.close();
			return QueryAdaptorClass.getText();

		} catch (FileNotFoundException e) {
			log.error("Unable to find the social.xml configuration file in "
					+ configPath, e);
			throw e;
		} catch (IOException e) {
			log.error("Error occured while reading the configuration file.", e);
			throw e;
		}
	}

	@SuppressWarnings("rawtypes")
	public static Object getQueryAdaptorObject(Class cls)
			throws SocialActivityException {
		Object obj = null;
		String queryAdapterObjectErrorMessage = "Unable to get Query Adapter object";
		try {
			obj = cls.newInstance();
			return obj;
		} catch (InstantiationException e) {
			log.error(queryAdapterObjectErrorMessage + e.getMessage());
			throw new SocialActivityException(e.getMessage(), e);
		} catch (IllegalAccessException e) {
			log.error(queryAdapterObjectErrorMessage + e.getMessage());
			throw new SocialActivityException(e.getMessage(), e);
		}

	}

	@SuppressWarnings("rawtypes")
	public static Class loadQueryAdaptorClass() throws SocialActivityException {
		Class<?> cls = null;
		String loadQueryErrorMessage = "Unable to load Query Adapter class.";
		try {
			cls = Class.forName(SocialUtil.getSocialAdaptorImplClass());
			return cls;
		} catch (ClassNotFoundException e) {
			log.error(loadQueryErrorMessage + e.getMessage());
			throw new SocialActivityException(e.getMessage(), e);
		} catch (IOException e) {
			log.error(loadQueryErrorMessage + e.getMessage());
			throw new SocialActivityException(e.getMessage(), e);
		}

	}

}
