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
package org.wso2.es.ui.integration.test.store;

import org.openqa.selenium.By;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.es.ui.integration.util.BaseUITestCase;
import org.wso2.es.ui.integration.util.ESUtil;
import org.wso2.es.ui.integration.util.ESWebDriver;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Accesses pages using a tenant that does not exist
 * 1. Checks if the homepage (top-assets) returns a 404 error
 * 2. Checks if the asset listing page returns a 404 error
 * 3. Checks if an asset details page returns a 404 error
 */
public class ESStoreTUAccessInvalidTenantTestCase extends BaseUITestCase {



    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = new ESWebDriver(BrowserManager.getWebDriver());
        baseUrl = getWebAppURL();
    }

    /**
     * The method returns the store url of the homepage
     * @return A string url to the store homepage
     */
    public String resolveStoreUrl() {
        String tenantDomain = ESUtil.getTenantQualifiedUrl(NONE_EXIST_TENANT_DOMAIN);
        return baseUrl + STORE_URL +tenantDomain;
    }

    @Test(groups = "wso2.es.store", description = "Test if accessing the homepage of foo.com returns a 404 error")
    public void testAnonHomePage() throws Exception {
        driver.get(resolveStoreUrl());
        checkErrorPresent();
    }


    @Test(groups = "wso2.es.store", description = "Test if accessing the asset listing page of foo.com returns a 404 error")
    public void testAssetListingPage() throws Exception {
        driver.get(resolveStoreUrl() + "/assets/gadget/list");
        checkErrorPresent();
    }

    private void checkErrorPresent() {
        assertEquals(ERROR_404, driver.findElement(By.xpath("//h1[contains(text()," +
                "'Error 404')]")).getText(), "Error 404 error code is missing");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }
}
