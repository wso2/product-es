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
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.es.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.es.ui.integration.util.AssetUtil;
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
    private static final String ASSET_VERSION = "1.0.0";
    private static final String ASSET_TYPE1 = "gadget";
    private static final String ASSET_TYPE2 = "site";
    private String assetName;
    private String resourcePathSite;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = new ESWebDriver(BrowserManager.getWebDriver());
        baseUrl = getWebAppURL();
        buildTenantDetails(TestUserMode.SUPER_TENANT_ADMIN);
        login();


        assetName = "Asset Recent";

        resourcePath = GADGET_REGISTRY_BASE_PATH + currentUserName + "/" + assetName + "/" + ASSET_VERSION;
        resourcePathSite = SITE_REGISTRY_BASE_PATH + currentUserName + "/" + assetName + "/" + ASSET_VERSION;

        //navigate to publisher and add and publish a new gadget to support sort by created time
        driver.get(baseUrl + PUBLISHER_URL);
        AssetUtil.addNewAsset(driver, baseUrl, ASSET_TYPE1, assetName, ASSET_VERSION, "", "", "");
        driver.findElementPoll(By.linkText(assetName),MAX_POLL_COUNT);
        driver.findElement(By.linkText(assetName)).click();
        AssetUtil.publishAssetToStore(driver, assetName);

        driver.get(baseUrl + PUBLISHER_URL);
        AssetUtil.addNewAsset(driver, baseUrl, ASSET_TYPE2, assetName, ASSET_VERSION, "", "", "");
        driver.findElementPoll(By.linkText(assetName),MAX_POLL_COUNT);
        driver.findElement(By.linkText(assetName)).click();
        AssetUtil.publishAssetToStore(driver, assetName);


        driver.get(baseUrl + PUBLISHER_LOGOUT_URL);
        //navigate to store and wait for the new gadget to be visible in store
        driver.get(baseUrl + STORE_GADGET_LIST_PAGE);
        driver.findElementPoll(By.xpath("//a[contains(.,'Asset Recent')]"), MAX_POLL_COUNT);

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
        assertTrue(isElementPresent(driver, By.cssSelector(".app-logo")), "Home Page error: Logo missing");
        assertTrue(isElementPresent(driver, By.id("popoverExampleTwo")), "Home Page error: Asset menu missing");
        assertTrue(isElementPresent(driver, By.linkText("Recent Gadgets")), "Home Page error: Recent Gadgets links missing");
        assertTrue(isElementPresent(driver, By.linkText("Recent Sites")), "Home Page error: Recent Sites links missing");
        assertTrue(isElementPresent(driver, By.id("search")), "Home Page error: Search missing");
    }

    @Test(groups = "wso2.es.store", description = "Test the navigation from top menu when accessing the homepage with " +
            "a super tenant user using the tenant url(/t/carbon.super)")
    public void testLoggedInNavigationTop() throws Exception {
        driver.get(resolveStoreUrl());
        WebDriverWait wait = new WebDriverWait(driver, 60);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("popoverExampleTwo")));
        driver.findElement(By.id("popoverExampleTwo")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Gadget")));
        driver.findElement(By.linkText("Gadget")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText(LINE_CHART )));
        driver.findElementPoll(By.linkText(LINE_CHART ), MAX_POLL_COUNT);
        assertEquals(LINE_CHART, driver.findElement(By.linkText(LINE_CHART))
                .getText(), "Gadgets Menu not working");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("popoverExampleTwo")));
        driver.findElement(By.id("popoverExampleTwo")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Site")));
        driver.findElement(By.linkText("Site")).click();
        driver.findElementPoll(By.linkText(AMAZON), MAX_POLL_COUNT);
        assertEquals(AMAZON, driver.findElement(By.linkText(AMAZON)).getText(),
                     "Sites Menu not working");
        driver.findElement(By.cssSelector("h2.app-title")).click();
    }

    @Test(groups = "wso2.es.store", description = "Test navigation page links when accessing the homepage with a " +
            "super tenant user using the tenant url(/t/carbon.super)")
    public void testLoggedInNavigationLinks() throws Exception {
        driver.get(resolveStoreUrl());
        driver.findElement(By.cssSelector("div.app-logo a")).click();
        driver.findElement(By.linkText("Recent Gadgets")).click();
        driver.findElementPoll(By.linkText(assetName), MAX_POLL_COUNT);
        assertEquals(assetName, driver.findElement(By.cssSelector(".assets-container section div.ctrl-wr-asset:first-child a.ast-name")).getText(),
                     "Recent Gadgets link not working");

        driver.findElement(By.cssSelector("div.app-logo a")).click();
        driver.findElement(By.linkText("Recent Sites")).click();
        driver.findElementPoll(By.linkText(assetName), MAX_POLL_COUNT);
        assertEquals(assetName, driver.findElement(By.cssSelector(".assets-container section div.ctrl-wr-asset:first-child a.ast-name")).getText(),
                     "Recent Sites link not working");
    }

    @Test(groups = "wso2.es.store", description= "Test if a logged in super tenant user can navigate to the asset " +
            "listing page using the tenant url(/t/carbon.super)")
    public void testAssetListingPage() throws Exception {
        driver.get(baseUrl + STORE_GADGET_LIST_PAGE);
        assertTrue(isElementPresent(driver, By.cssSelector("div.app-logo a")), "Asset listing page error: Logo missing");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        AutomationContext automationContext = new AutomationContext(PRODUCT_GROUP_NAME, TestUserMode.SUPER_TENANT_ADMIN);
        adminUserName = automationContext.getSuperTenant().getTenantAdmin().getUserName();
        adminUserPwd = automationContext.getSuperTenant().getTenantAdmin().getPassword();
        String backendURL = automationContext.getContextUrls().getBackEndUrl();
        resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL, adminUserName, adminUserPwd);
        resourceAdminServiceClient.deleteResource(resourcePath);
        resourceAdminServiceClient.deleteResource(resourcePathSite);
        driver.quit();
    }
}
