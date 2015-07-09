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
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.annotations.Test;
import org.wso2.es.ui.integration.util.ESUtil;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests the homepage of a tenant when it is accessed
 * by an anonymous user
 * 1. Checks if the homepage (top-assets) is rendered
 * 2. Checks if the gadget and site links are present
 * 3. Checks if the gadget and site listing pages are rendered
 */
public class ESStoreAnonTenantHomePageTestCase extends ESStoreAnonHomePageTestCase {
    @Override
    public String resolveStoreUrl() {
        String tenantDomain = "wso2.com";//TODO:Obtain this from the anon context
        return baseUrl + STORE_URL + ESUtil.getTenantQualifiedUrl(tenantDomain);
    }

    @Override
    @Test(groups = "wso2.es.store", description = "Test if the homepage loads when using /t/domain as anon user")
    public void testAnonHomePage() throws Exception {
        //test appearance
        driver.get(resolveStoreUrl());
        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector("span.btn-asset")));
        assertTrue(isElementPresent(driver, By.cssSelector(".app-logo")), "Home Page error: Logo missing");
        driver.findElement(By.cssSelector("span.btn-asset")).click();
        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.linkText("Gadget")));
        assertTrue(isElementPresent(driver,By.linkText("Gadget")), "Home Page error: Gadget menu missing");
        assertTrue(isElementPresent(driver,By.linkText("Site")), "Home Page error: Site menu missing");

    }

    @Test(groups = "wso2.es.store", description = "Test if the asset listing page loads when using /t/domain as anon " +
            "user")
    public void testAnonAssetListingPage() throws Exception {
        driver.get(baseUrl + STORE_GADGET_LIST_PAGE);
        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector(".ctrl-wr-asset")));
        assertTrue(isElementPresent(driver, By.cssSelector(".app-logo")), "Home Page error: Logo missing");
        assertTrue(isElementPresent(driver, By.cssSelector(".ctrl-wr-asset")), "Home Page error: Gadgets list is not populated");

    }

    @Override
    @Test(groups = "wso2.es.store", description = "Test if the navigation menu works")
    public void testAnonNavigationTop() throws Exception {
    }

    @Override
    @Test(groups = "wso2.es.store", description = "Test if the navigation links work")
    public void testAnonNavigationLinks() throws Exception {
    }
}
