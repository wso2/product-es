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
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.context.beans.Tenant;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.es.ui.integration.util.BaseUITestCase;
import org.wso2.es.ui.integration.util.ESUtil;
import org.wso2.es.ui.integration.util.ESWebDriver;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * The test first logs in the tenant admin for wso2.com and
 * then checks the behaviour of the Store Home Page
 * 1. Checks if the homepage loads (top-assets page)
 * 2. Checks if the gadget and site links are present
 * 3. Checks if the gadget listing page is rendered
 * 4. Checks if the site listing page is rendered
 */
public class ESStoreTenantUserTUHomePageTestCase extends BaseUITestCase {
    protected static final int MAX_POLL_COUNT = 30;
    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = new ESWebDriver(BrowserManager.getWebDriver());
        baseUrl = getWebAppURL();
        login();
    }

    public void login() throws Exception {
        super.init(TestUserMode.TENANT_ADMIN);
        currentUserName = userInfo.getUserName();
        currentUserPwd = userInfo.getPassword();
        buildTenantDetails(TestUserMode.TENANT_ADMIN);
        ESUtil.login(driver, baseUrl, STORE_APP, currentUserName, currentUserPwd);
    }

    public String resolveStoreUrl()  {
        String tenantDomain = tenantDetails.getDomain();
        return baseUrl + STORE_URL + ESUtil.getTenantQualifiedUrl(tenantDomain);
    }

    @Test(groups = "wso2.es.store", description = "Test accessing homepage with a logged in tenant user using " +
            "the tenant url(t/wso2.com)")
    public void testLoggedInHomePage() throws Exception {
        driver.get(resolveStoreUrl());
        assertTrue(isElementPresent(driver, By.cssSelector("a.brand")), "Home Page error: Logo missing");
        assertEquals("Gadget", driver.findElement(By.xpath("//div[@id='container-search']/div/div/div/div/a[1]/li"))
                .getText(), "Home Page error: Gadget menu missing");
        assertEquals("Site", driver.findElement(By.xpath("//div[@id='container-search']/div/div/div/div/a[2]/li"))
                .getText(), "Home Page error: Site menu missing");
    }

    @Test(groups="wso2.es.store" , description = "Test accessing asset listing page with a logged in tenant" +
            "user using the tenant url(t/wso2.com)")
    public void testAssetListingPage() throws Exception{
        driver.get(resolveStoreUrl()+"/assets/gadget/list");
        assertTrue(isElementPresent(driver, By.cssSelector("a.brand")), "Home Page error: Logo missing");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }
}
