/*
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.wso2.es.ui.integration.test.publisher.permissions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.es.ui.integration.util.BaseUITestCase;

/**
 * Tests the /list permission
 *
 * Note: The logged in user should not have the admin role
 *
 * Case #1:
 *  Precondition:
 *      - User should have the /list permission
 *  Should be true:
 *      - The asset type should be listed in the asset list box
 *      - The asset list page should be accessible
 * Case #2:
 *  Precondition:
 *      - User should not have the /list permission
 *  Should be true:
 *      - The asset type should not appear in the asset list box
 *      - The asset list page should not be accessible and should display a 401 error message
 */
public class ESPublisherListPermissionTestCase extends BaseUITestCase {
    private static final Log LOG = LogFactory.getLog(ESPublisherListPermissionTestCase.class);
    private TestUserMode userMode;

    public ESPublisherListPermissionTestCase() {
        LOG.info("Constructor called "+ESPublisherListPermissionTestCase.class);
    }

    @BeforeMethod(alwaysRun = true)
    public void setUp() throws Exception {
        LOG.info("setup method called "+ESPublisherListPermissionTestCase.class);
    }

//    @DataProvider(name = "userMode")
//    private static Object[][] userModeProvider() {
//        return new Object[][]{{TestUserMode.TENANT_ADMIN, "Add Edit asset - TenantAdmin"},
//                {TestUserMode.TENANT_USER, "Add Edit asset - TenantUser"}};
//    }
}
