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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.es.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.es.ui.integration.util.BaseUITestCase;
import org.wso2.es.ui.integration.util.ESUtil;
import org.wso2.es.ui.integration.util.ESWebDriver;

import static org.testng.Assert.assertEquals;

/**
 * This class contains tests relates to search functionality of assets list page
 */
public class ESStoreSearchGadgetListTestCase extends BaseUITestCase {

    private static final Log log = LogFactory.getLog(ESStoreSearchGadgetListTestCase.class);
    private static String assetName = "Sample Asset";
    private static String assetVersion = "1.2.3";
    private static String assetAuthor = "testAuthor";
    private static String assetCreatedTime = "123";
    private static String assetCategory = "WSO2";
    private static String assetURL = "www.example.com";
    private static String assetDescription = "this is a sample asset";
    private static String resourcePath = "/_system/governance/gadgets/" + assetAuthor + "/"
            + assetName + "/" + assetVersion;

    private ResourceAdminServiceClient resourceAdminServiceClient;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = new ESWebDriver(BrowserManager.getWebDriver());
        baseUrl = getWebAppURL();
        adminUserName = userInfo.getUserName();
        adminUserPwd = userInfo.getPassword();
        acceptNextAlert = true;
        wait = new WebDriverWait(driver, MAX_DRIVER_WAIT_TIME_SEC);
        AutomationContext automationContext = new AutomationContext(PRODUCT_GROUP_NAME,
                TestUserMode.SUPER_TENANT_ADMIN);
        adminUserName = automationContext.getSuperTenant().getTenantAdmin().getUserName();
        adminUserPwd = automationContext.getSuperTenant().getTenantAdmin().getPassword();
        backendURL = automationContext.getContextUrls().getBackEndUrl();
        resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL, adminUserName,
                adminUserPwd);
        driver.get(baseUrl + "/" + STORE_APP);
    }

    @Test(groups = "wso2.es.store", description = "Search By Category Template",
            dependsOnMethods = "testESStoreSearchNewlyAddedAssetsName")
    public void testStoreSearchByCategoryTemplate() throws Exception {
        driver.get(baseUrl + "/store/pages/top-assets");
        driver.findElement(By.cssSelector("i.icon-cog")).click();
        driver.findElement(By.cssSelector("i.icon-sort-down")).click();
        driver.findElement(By.id("search")).click();
        new Select(driver.findElement(By.id("overview_category"))).selectByVisibleText("Templates");
        driver.findElement(By.id("search-button2")).click();
        wait.until(ExpectedConditions.not(ExpectedConditions.textToBePresentInElementLocated(By
                .cssSelector("h4"), assetName)));
        assertEquals(6, driver.findElements(By.cssSelector("div.span3.asset")).size(),
                "Number of Template gadgets are incorrect");

    }

    @Test(groups = "wso2.es.store", description = "Search By Category-Google and Provider-Admin",
            dependsOnMethods = "testESStoreSearchNewlyAddedAssetsName")
    public void testESStoreSearchGadgetByProviderAndCategory() throws Exception {
        driver.get(baseUrl + "/store/pages/top-assets?null");
        driver.findElement(By.cssSelector("i.icon-cog")).click();
        driver.findElement(By.cssSelector("i.icon-sort-down")).click();
        driver.findElement(By.id("search")).click();
        driver.findElement(By.name("overview_provider")).clear();
        driver.findElement(By.name("overview_provider")).sendKeys(adminUserName);
        driver.findElement(By.id("search-button2")).click();
//        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("h4"),
//                "Bar Chart"));
        wait.until(ExpectedConditions.not(ExpectedConditions.textToBePresentInElementLocated(By
                .cssSelector("h4"), assetName)));
        assertEquals(2, driver.findElements(By.cssSelector("div.asset-details")).size(),
                "Seach result count does not match");

    }

    @Test(groups = "wso2.es.store", description = "Search By Name- Bar Chart",
            dependsOnMethods = "testESStoreSearchNewlyAddedAssetsName")
    public void testESStoreSearchAssetsByName() throws Exception {
        driver.get(baseUrl + "/store/pages/top-assets?null");
        driver.findElement(By.cssSelector("a.brand")).click();
        driver.findElement(By.cssSelector("i.icon-cog")).click();
        driver.findElement(By.cssSelector("i.icon-sort-down")).click();
        driver.findElement(By.id("search")).click();
        driver.findElement(By.name("overview_name")).clear();
        driver.findElement(By.name("overview_name")).sendKeys("Bar Chart");
        driver.findElement(By.id("search-button2")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("h4"),
                "Bar Chart"));
        assertEquals("Bar Chart", driver.findElement(By.cssSelector("h4")).getText());
    }

    @Test(groups = "wso2.es.store", description = "Search Unavailable Name",
            dependsOnMethods = "testESStoreSearchNewlyAddedAssetsName")
    public void testESStoreSearchUnAvailableAssetsName() throws Exception {
        driver.get(baseUrl + "/store/pages/top-assets?null");
        driver.findElement(By.cssSelector("a.brand")).click();
        driver.findElement(By.cssSelector("i.icon-cog")).click();
        driver.findElement(By.cssSelector("i.icon-sort-down")).click();
        driver.findElement(By.id("search")).click();
        driver.findElement(By.name("overview_name")).clear();
        driver.findElement(By.name("overview_name")).sendKeys("Line Chart");
        driver.findElement(By.id("search-button2")).click();
//      wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("div.empty-assert")));
        assertEquals("We couldn't find anything for you.",
                driver.findElement(By.cssSelector("div.empty-assert")).getText());
    }

    @Test(groups = "wso2.es.store", description = "Search Unavailable Version",
            dependsOnMethods = "testESStoreSearchNewlyAddedAssetsName")
    public void testStoreSearchUnAvailableVersion() throws Exception {
        driver.get(baseUrl + "/store/pages/top-assets");
        driver.findElement(By.cssSelector("i.icon-cog")).click();
        driver.findElement(By.cssSelector("i.icon-sort-down")).click();
        driver.findElement(By.id("search")).click();
        driver.findElement(By.name("overview_version")).clear();
        driver.findElement(By.name("overview_version")).sendKeys("9.9.9");
        driver.findElement(By.id("search-button2")).click();
//      wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("div.empty-assert")));
        assertEquals("We couldn't find anything for you.",
                driver.findElement(By.cssSelector("div.empty-assert")).getText());
    }

    @Test(groups = "wso2.es.store", description = "Search Unavailable Author",
            dependsOnMethods = "testESStoreSearchNewlyAddedAssetsName")
    public void testStoreSearchUnAvailableAuthor() throws Exception {
        driver.findElement(By.xpath("//div[@id='container-search']/div/div/div/div/a/li")).click();
        driver.findElement(By.cssSelector("i.icon-sort-down")).click();
        driver.findElement(By.id("search")).click();
        driver.findElement(By.name("overview_provider")).clear();
        driver.findElement(By.name("overview_provider")).sendKeys("unavailable");
        driver.findElement(By.id("search-button2")).click();
//        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("div.empty-assert")));
        assertEquals("We couldn't find anything for you.",
                driver.findElement(By.cssSelector("div.empty-assert")).getText());
    }

    @Test(groups = "wso2.es.store", description = "Add asset")//,
    //dependsOnMethods = "testESStoreSearchAssetsByName")
    public void testAddAsset() throws Exception {
        ESUtil.login(driver, baseUrl, "publisher", userInfo.getUserName(),
                userInfo.getPassword());
        driver.get(baseUrl + "/publisher/asts/gadget/list");
        driver.findElement(By.linkText("Add")).click();
//        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("h4"), "Overview"));
        driver.findElement(By.name("overview_provider")).clear();
        driver.findElement(By.name("overview_provider")).sendKeys(assetAuthor);
        driver.findElement(By.name("overview_name")).clear();
        driver.findElement(By.name("overview_name")).sendKeys(assetName);
        driver.findElement(By.name("overview_version")).clear();
        driver.findElement(By.name("overview_version")).sendKeys(assetVersion);
        driver.findElement(By.name("overview_createdtime")).clear();
        driver.findElement(By.name("overview_createdtime")).sendKeys(assetCreatedTime);
        new Select(driver.findElement(By.name("overview_category")))
                .selectByVisibleText(assetCategory);
        driver.findElement(By.name("overview_url")).clear();
        driver.findElement(By.name("overview_url")).sendKeys(assetURL);
        driver.findElement(By.name("overview_description")).clear();
        driver.findElement(By.name("overview_description")).sendKeys(assetDescription);
        driver.findElement(By.id("btn-create-asset")).click();
        if (isAlertPresent()) {
            String alert = closeAlertAndGetItsText();
            log.warn(alert + ": modal box appeared");
        }
        driver.findElementPoll(By.linkText(assetName), 10);
        driver.findElement(By.cssSelector("a.btn")).click();
        driver.findElement(By.linkText(assetName)).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("h4"),
                assetName));
        driver.findElement(By.linkText("Life Cycle")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("div.pull-left"),
                "Lifecycle - " + assetName));
        driver.findElement(By.id("In-Review")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("commentModalLabel"),
                "Add a comment"));
        driver.findElement(By.id("commentModalText")).clear();
        driver.findElement(By.id("commentModalText")).sendKeys("ok");
        driver.findElement(By.id("commentModalSave")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("state"), "IN-REVIEW"));
        assertEquals(driver.findElement(By.id("state")).getText(), "IN-REVIEW",
                "Not Promoted to In-Review");
        driver.get(driver.getCurrentUrl());
        driver.findElement(By.id("Published")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("commentModalLabel"),
                "Add a comment"));
        driver.findElement(By.id("commentModalText")).clear();
        driver.findElement(By.id("commentModalText")).sendKeys("ok");
        driver.findElement(By.id("commentModalSave")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("state"), "PUBLISHED"));
        assertEquals(driver.findElement(By.id("state")).getText(), "PUBLISHED",
                "Not Promoted to Published");
    }

    @Test(groups = "wso2.es.store", description = "Search by newly added asset Name",
            dependsOnMethods = "testAddAsset")
    public void testESStoreSearchNewlyAddedAssetsName() throws Exception {
        driver.get(baseUrl + "/store/pages/top-assets");
        driver.findElement(By.cssSelector("a.brand")).click();
        driver.findElement(By.cssSelector("i.icon-cog")).click();
        driver.findElement(By.cssSelector("i.icon-sort-down")).click();
        driver.findElement(By.id("search")).click();
        driver.findElement(By.name("overview_name")).clear();
        driver.findElement(By.name("overview_name")).sendKeys(assetName);
        new Select(driver.findElement(By.id("overview_category"))).selectByVisibleText(assetCategory);
        driver.findElement(By.id("search-button2")).click();
        driver.findElementPoll(By.linkText(assetName), 10);
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("h4"), assetName));
        assertEquals(assetName, driver.findElement(By.cssSelector("h4")).getText(),
                "Newly added gadget is not found in the result of search by name : " + assetName);

    }

    @Test(groups = "wso2.es.store", description = "Search by newly added asset Version",
            dependsOnMethods = "testAddAsset")
    public void testESStoreSearchNewlyAddedAssetsVersion() throws Exception {
        driver.get(baseUrl + "/store/pages/top-assets");
        driver.findElement(By.cssSelector("i.icon-cog")).click();
        driver.findElement(By.cssSelector("i.icon-sort-down")).click();
        driver.findElement(By.id("search")).click();
        driver.findElement(By.name("overview_version")).clear();
        driver.findElement(By.name("overview_version")).sendKeys(assetVersion);
        new Select(driver.findElement(By.id("overview_category"))).selectByVisibleText(assetCategory);
        driver.findElement(By.id("search-button2")).click();
        driver.findElementPoll(By.linkText(assetName), 10);
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("h4"), assetName));
        assertEquals(assetName, driver.findElement(By.cssSelector("h4")).getText(),
                "Newly added gadget is not found in the result of search by version : " +
                        assetVersion);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("img")));
        driver.findElement(By.cssSelector("img")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.linkText("Description"),
                "Description"));
        assertEquals("Version 1.2.3", driver.findElement(By.cssSelector("small")).getText(),
                "Newly added gadget's version is incorrect in the store");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        resourceAdminServiceClient.deleteResource(resourcePath);
        driver.quit();
    }

}
