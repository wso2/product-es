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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.NativeObject;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.social.core.JSONUtil;

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
		if (PreviousActivityID != null && PreviousActivityID != "") {
			return PreviousActivityID;
		} else {
			return null;
		}
	}

	public static boolean isValidRating(NativeObject activity) {
		int rating = Integer.parseInt(JSONUtil.getProperty(activity,
				Constants.OBJECT_JSON_PROP, Constants.RATING));
		if (rating > 0) {
			return true;
		} else {
			return false;
		}
	}

	public static String getVerb(NativeObject activity) {

		return JSONUtil.getProperty(activity, Constants.VERB_JSON_PROP);

	}

}
