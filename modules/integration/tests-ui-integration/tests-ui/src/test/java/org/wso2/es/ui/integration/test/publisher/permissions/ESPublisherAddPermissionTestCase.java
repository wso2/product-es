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
 * Tests the /add permission
 * <p/>
 * Note: The logged in user should not have the admin role
 * <p/>
 * Case #1:
 * Precondition:
 * - User should have the /add permission
 * <p/>
 * Should be true:
 * - The add button should be visible when navigating to the listing page of the asset type
 * - The add page should be accessible
 * - The user should be able to create an asset of the given type
 * <p/>
 * Case #2:
 * Precondition:
 * - User should not have the /add permission
 * <p/>
 * Should be tre:
 * - The add button should not be visible when navigating to the listing page of the asset type
 * - The add page should not be accessible
 */

public class ESPublisherAddPermissionTestCase extends BaseUITestCase {
    private static final Log LOG = LogFactory.getLog(ESPublisherAddPermissionTestCase.class);
    private TestUserMode userMode;


    public ESPublisherAddPermissionTestCase() {
        LOG.info("Constructor called " + ESPublisherAddPermissionTestCase.class);
    }

    @BeforeMethod(alwaysRun = true)
    public void setUp() throws Exception {
        LOG.info("setup method called " + ESPublisherAddPermissionTestCase.class);
    }
}
