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

package org.wso2.carbon.social.db.adapter.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;

/**
 * Registering {@link SocialActivityService}
 * 
 * @scr.component name="org.wso2.carbon.social.component" immediate="true"
 **/
public class AdapterComponent {

	private static Log log = LogFactory.getLog(AdapterComponent.class);

	protected void activate(ComponentContext context) {
		BundleContext bundleContext = context.getBundleContext();
		bundleContext.registerService(AdapterComponent.class,
				new AdapterComponent(), null);
		if (log.isDebugEnabled()) {
			log.debug("Social Activity service is activated with Adaptor implementation");
		}

	}
}

