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
 * Test appearance and behavior of Store Home page
 */
public class ESStoreAnonHomePageTestCase extends BaseUITestCase {

    protected static final String LINE_CHART = "Line Chart";
    protected static final String BAR_CHART = "Bar Chart";
    protected static final String WSO2 = "WSO2";
    protected static final String AMAZON = "Amazon";
    protected static final int MAX_POLL_COUNT = 30;
    private static final String ASSET_VERSION = "1.0.0";
    private static final String ASSET_TYPE1 = "gadget";
    private static final String ASSET_TYPE2 = "site";
    private String assetName;
    private String resourcePathSite;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        assetName = "Asset Recent";
        super.init();
        driver = new ESWebDriver(BrowserManager.getWebDriver());
        wait = new WebDriverWait(driver,MAX_POLL_COUNT);
        currentUserName = userInfo.getUserName().split("@")[0];
        currentUserPwd = userInfo.getPassword();
        baseUrl = getWebAppURL();
        resourcePath = GADGET_REGISTRY_BASE_PATH + currentUserName + "/" + assetName + "/" + ASSET_VERSION;
        resourcePathSite = SITE_REGISTRY_BASE_PATH + currentUserName + "/" + assetName + "/" + ASSET_VERSION;
        ESUtil.login(driver, baseUrl, PUBLISHER_APP, currentUserName, currentUserPwd);

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
        driver.findElementPoll(By.linkText(assetName), MAX_POLL_COUNT);

    }

    /**
     * The method returns the store url of the homepage
     * @return  A string url to the store homepage
     */
    public String resolveStoreUrl(){
        return baseUrl+STORE_URL;
    }

    @Test(groups = "wso2.es.store", description = "Test Anonymous User Home Page")
    public void testAnonHomePage() throws Exception {
        //test appearance
        driver.get(resolveStoreUrl());
        WebDriverWait wait = new WebDriverWait(driver, 60);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h2.app-title")));
        assertTrue(isElementPresent(driver, By.cssSelector("h2.app-title")), "Home Page error: Logo missing");
        //assertEquals("Sign in", driver.findElement(By.linkText("Sign in")).getText(),
        //             "Home Page error: Sign in button missing");
        //assertTrue(isElementPresent(driver, By.id("btn-register")), "Home Page error: Register button missing");
        assertTrue(isElementPresent(driver, By.id("popoverExampleTwo")), "Home Page error: Gadget menu missing");
        assertTrue(isElementPresent(driver, By.id("search")), "Home Page error: Search missing");
    }

    @Test(groups = "wso2.es.store", description = "Test Anonymous User Recent sliding",
            enabled = false)
    public void testSliding() throws Exception {
        //TODO how to detect sliding is successful?
    }

    @Test(groups = "wso2.es.store", description = "Test Anonymous Navigation from top menu")
    public void testAnonNavigationTop() throws Exception {
        //test menu navigation
        driver.get(resolveStoreUrl());
        WebDriverWait wait = new WebDriverWait(driver, 60);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("popoverExampleTwo")));
        driver.findElement(By.id("popoverExampleTwo")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Gadget")));
        driver.findElement(By.linkText("Gadget")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText(LINE_CHART )));
        driver.findElementPoll(By.linkText(LINE_CHART ), MAX_POLL_COUNT);
        assertEquals(LINE_CHART, driver.findElement(By.linkText(LINE_CHART ))
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

    @Test(groups = "wso2.es.store", description = "Test Anonymous Navigation page links")
    public void testAnonNavigationLinks() throws Exception {
        //test link navigation
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