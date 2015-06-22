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
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.es.ui.integration.util.BaseUITestCase;
import org.wso2.es.ui.integration.util.ESUtil;
import org.wso2.es.ui.integration.util.ESWebDriver;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests if the homepage of the Store is accessible when
 * navigating it to it as a logged in user using the tenant url (t/carbon.super)
 * 1. Checks if the homepage (top-assets) is loaded
 * 2. Checks if the asset listing page is loaded
 */
public class ESStoreCarbonSuperUserTUHomePageTestCase extends BaseUITestCase {

    protected static final String LINE_CHART = "Line Chart";
    protected static final String LINE_PLUS_BAR_CHART = "Line Plus Bar Chart";
    protected static final String AMAZON = "Amazon";
    protected static final int MAX_POLL_COUNT = 30;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = new ESWebDriver(BrowserManager.getWebDriver());
        baseUrl = getWebAppURL();
        buildTenantDetails(TestUserMode.SUPER_TENANT_ADMIN);
        login();
    }

    public void login() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        currentUserName = userInfo.getUserName();
        currentUserPwd = userInfo.getPassword();
        ESUtil.login(driver, baseUrl, STORE_APP, currentUserName, currentUserPwd);
    }

    public String resolveStoreUrl() {
        String tenantDomain = tenantDetails.getDomain();
        return baseUrl + STORE_URL + ESUtil.getTenantQualifiedUrl(tenantDomain);
    }

    @Test(groups = "wso2.es.store", description = "Test if the homepage can be accessed by a logged in super tenant user" +
            "using the tenant url(/t/carbon.super)")
    public void testLoggedInHomePage() throws Exception {
        driver.get(resolveStoreUrl());
        assertTrue(isElementPresent(driver, By.cssSelector("a.brand")), "Home Page error: Logo missing");
        assertEquals("Gadget", driver.findElement(By.xpath("//div[@id='container-search']/div/div/div/div/a[1]/li"))
                .getText(), "Home Page error: Gadget menu missing");
        assertEquals("Site", driver.findElement(By.xpath("//div[@id='container-search']/div/div/div/div/a[2]/li"))
                .getText(), "Home Page error: Site menu missing");
        assertEquals("Recent Gadgets", driver.findElement(By.linkText("Recent Gadgets")).getText(),
                "Home Page error: Recent Gadgets links missing");
        assertEquals("Recent Sites", driver.findElement(By.linkText("Recent Sites")).getText(),
                "Home Page error: Recent Sites links missing");
        assertTrue(isElementPresent(driver, By.id("search")), "Home Page error: Search missing");
        assertTrue(isElementPresent(driver, By.cssSelector("div.span3.store-right > div.row > div.span3")),
                "Home Page error: Recent Added side list missing");
    }

    @Test(groups = "wso2.es.store", description = "Test the navigation from top menu when accessing the homepage with " +
            "a super tenant user using the tenant url(/t/carbon.super)")
    public void testLoggedInNavigationTop() throws Exception {
        driver.get(resolveStoreUrl());
        driver.findElement(By.xpath("//div[@id='container-search']/div/div/div/div/a[1]/li")).click();
        driver.findElementPoll(By.xpath("//h4[contains(.,'" + LINE_PLUS_BAR_CHART + "')]"), MAX_POLL_COUNT);
        assertEquals(LINE_PLUS_BAR_CHART, driver.findElement(By.xpath("//h4[contains(.,'" + LINE_PLUS_BAR_CHART + "')]"))
                .getText(), "Gadgets Menu not working");
        driver.findElement(By.xpath("//div[@id='container-search']/div/div/div/div/a[2]/li")).click();
        driver.findElementPoll(By.xpath("//h4[contains(.,'" + AMAZON + "')]"), MAX_POLL_COUNT);
        assertEquals(AMAZON, driver.findElement(By.xpath("//h4[contains(.,'" + AMAZON + "')]")).getText(),
                "Sites Menu not working");
        driver.findElement(By.cssSelector("a.brand")).click();
    }

    @Test(groups = "wso2.es.store", description = "Test navigation page links when accessing the homepage with a " +
            "super tenant user using the tenant url(/t/carbon.super)")
    public void testLoggedInNavigationLinks() throws Exception {
        driver.get(resolveStoreUrl());
        driver.findElement(By.cssSelector("a.brand")).click();
        driver.findElement(By.linkText("Recent Gadgets")).click();
        driver.findElementPoll(By.xpath("//h4[contains(.,'" + LINE_PLUS_BAR_CHART + "')]"), MAX_POLL_COUNT);
        assertEquals(LINE_CHART, driver.findElement(By.xpath("//h4[contains(.,'" + LINE_CHART + "')]")).getText(),
                "Recent Gadgets link not working");
        driver.findElement(By.cssSelector("a.brand")).click();
        driver.findElement(By.linkText("Recent Sites")).click();
        driver.findElementPoll(By.xpath("//h4[contains(.,'" + AMAZON + "')]"), MAX_POLL_COUNT);
        assertEquals(AMAZON, driver.findElement(By.xpath("//h4[contains(.,'" + AMAZON + "')]")).getText(),
                "Recent Sites link not working");
    }

    @Test(groups = "wso2.es.store", description= "Test if a logged in super tenant user can navigate to the asset " +
            "listing page using the tenant url(/t/carbon.super)")
    public void testAssetListingPage() throws Exception {
        driver.get(resolveStoreUrl()+"/assets/gadget/list");
        assertTrue(isElementPresent(driver, By.cssSelector("a.brand")), "Asset listing page error: Logo missing");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }
}
