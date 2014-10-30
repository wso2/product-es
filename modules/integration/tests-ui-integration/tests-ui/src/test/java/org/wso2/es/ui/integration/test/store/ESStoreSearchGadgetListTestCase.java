/*
 * Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.es.ui.integration.test.store;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.Alert;

import static org.testng.Assert.*;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.openqa.selenium.support.ui.Select;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.es.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.es.ui.integration.util.*;
import org.wso2.es.integration.common.utils.ESIntegrationUITest;

public class ESStoreSearchGadgetListTestCase extends ESIntegrationUITest {
    private ESWebDriver driver;
    private String baseUrl;
    private String webApp = "store";
    private boolean acceptNextAlert = true;
    private static final Log log = LogFactory.getLog(ESStoreSearchGadgetListTestCase.class);
    WebDriverWait wait;

    private static String assetName = "Sample Asset";
    private static String assetVersion = "1.2.3";
    private static String assetAuthor = "testAuthor";
    private static String assetCreatedTime = "123";
    private static String assetCategory = "WSO2";
    private static String assetURL = "www.example.com";
    private static String assetDescription = "this is a sample asset";
    private static String resourcePath = "/_system/governance/gadgets/" + assetAuthor + "/" + assetName + "/" + assetVersion;

    private String adminUserName;// = "admin";//automationcontext
    private String adminUserPwd;// = "admin";
    private String backendURL;

    private ResourceAdminServiceClient resourceAdminServiceClient;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = new ESWebDriver();
        wait = new WebDriverWait(driver, 30);
        baseUrl = getWebAppURL();
        adminUserName = userInfo.getUserName();
        adminUserPwd = userInfo.getPassword();
        AutomationContext automationContext = new AutomationContext("ES", TestUserMode.SUPER_TENANT_ADMIN);
        backendURL = automationContext.getContextUrls().getBackEndUrl();
        resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL, adminUserName, adminUserPwd);
        driver.get(baseUrl + "/" + webApp);
    }

    @Test(groups = "wso2.es.store", description = "Search By Category Template")
    public void testStoreSearchByCategoryTemplate() throws Exception {
        driver.get(baseUrl + "/store/pages/top-assets");
        driver.findElement(By.cssSelector("i.icon-cog")).click();
        driver.findElement(By.cssSelector("i.icon-sort-down")).click();
        driver.findElement(By.id("search")).click();
        new Select(driver.findElement(By.id("overview_category"))).selectByVisibleText("Templates");
        driver.findElement(By.id("search-button2")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("h4"), "Line Plus Bar Chart"));
        assertEquals(6, driver.findElements(By.cssSelector("div.span3.asset")).size(),"Number of Template gadgets" +
                " are incorrect");//TODO message, avoid css selectors

    }

    @Test(groups = "wso2.es.store", description = "Search By Category-Google and Provider-Admin",
            dependsOnMethods = "testStoreSearchByCategoryTemplate")
    public void testESStoreSearchGadgetByProviderAndCategory() throws Exception {
        driver.get(baseUrl + "/store/pages/top-assets?null");
        driver.findElement(By.cssSelector("i.icon-cog")).click();
        driver.findElement(By.cssSelector("i.icon-sort-down")).click();
        driver.findElement(By.id("search")).click();
        driver.findElement(By.name("overview_provider")).clear();
        driver.findElement(By.name("overview_provider")).sendKeys(adminUserName);
        driver.findElement(By.id("search-button2")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("h4"), "Bar Chart"));
        assertEquals(2, driver.findElements(By.cssSelector("div.asset-details")).size(),"Seach result count" +
                " does not match");

    }

    @Test(groups = "wso2.es.store", description = "Search By Name- Bar Chart",
            dependsOnMethods = "testESStoreSearchGadgetByProviderAndCategory")
    public void testESStoreSearchAssetsByName() throws Exception {
        driver.get(baseUrl + "/store/pages/top-assets?null");
        driver.findElement(By.cssSelector("a.brand")).click();
        driver.findElement(By.cssSelector("i.icon-cog")).click();
        driver.findElement(By.cssSelector("i.icon-sort-down")).click();
        driver.findElement(By.id("search")).click();
        driver.findElement(By.name("overview_name")).clear();
        driver.findElement(By.name("overview_name")).sendKeys("Bar Chart");
        driver.findElement(By.id("search-button2")).click();
//        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("h4"), "Bar Chart"));
        assertEquals("Bar Chart", driver.findElement(By.cssSelector("h4")).getText());
    }

    @Test(groups = "wso2.es.store", description = "Search Unavailable Name", dependsOnMethods = "testESStoreSearchAssetsByName")
    public void testESStoreSearchUnAvailableAssetsName() throws Exception {
        driver.get(baseUrl + "/store/pages/top-assets?null");
        driver.findElement(By.cssSelector("a.brand")).click();
        driver.findElement(By.cssSelector("i.icon-cog")).click();
        driver.findElement(By.cssSelector("i.icon-sort-down")).click();
        driver.findElement(By.id("search")).click();
        driver.findElement(By.name("overview_name")).clear();
        driver.findElement(By.name("overview_name")).sendKeys("Line Chart");
        driver.findElement(By.id("search-button2")).click();
//        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("div.empty-assert")));
        assertEquals("We couldn't find anything for you.", driver.findElement(By.cssSelector("div.empty-assert")).getText());
    }

    @Test(groups = "wso2.es.store", description = "Search Unavailable Version", dependsOnMethods = "testESStoreSearchUnAvailableAssetsName")
    public void testStoreSearchUnAvailableVersion() throws Exception {
        driver.get(baseUrl + "/store/pages/top-assets");
        driver.findElement(By.cssSelector("i.icon-cog")).click();
        driver.findElement(By.cssSelector("i.icon-sort-down")).click();
        driver.findElement(By.id("search")).click();
        driver.findElement(By.name("overview_version")).clear();
        driver.findElement(By.name("overview_version")).sendKeys("9.9.9");
        driver.findElement(By.id("search-button2")).click();
//        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("div.empty-assert")));
        assertEquals("We couldn't find anything for you.", driver.findElement(By.cssSelector("div.empty-assert")).getText());
    }

    @Test(groups = "wso2.es.store", description = "Search Unavailable Author", dependsOnMethods = "testStoreSearchUnAvailableVersion")
    public void testStoreSearchUnAvailableAuthor() throws Exception {
        driver.findElement(By.xpath("//div[@id='container-search']/div/div/div/div/a/li")).click();
        driver.findElement(By.cssSelector("i.icon-sort-down")).click();
        driver.findElement(By.id("search")).click();
        driver.findElement(By.name("overview_provider")).clear();
        driver.findElement(By.name("overview_provider")).sendKeys("unavailable");
        driver.findElement(By.id("search-button2")).click();
//        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("div.empty-assert")));
        assertEquals("We couldn't find anything for you.", driver.findElement(By.cssSelector("div.empty-assert")).getText());
    }

    @Test(groups = "wso2.es.store", description = "Add asset", dependsOnMethods = "testStoreSearchByCategoryTemplate")
    public void testAddasset() throws Exception {
        ESUtil.login(driver, baseUrl, "publisher", userInfo.getUserName(), userInfo.getPassword());
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
        new Select(driver.findElement(By.name("overview_category"))).selectByVisibleText(assetCategory);
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
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("h4"), assetName));
        driver.findElement(By.linkText("Life Cycle")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("div.pull-left"), "Lifecycle - " + assetName));
        driver.findElement(By.id("In-Review")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("commentModalLabel"), "Add a comment"));
        driver.findElement(By.id("commentModalText")).clear();
        driver.findElement(By.id("commentModalText")).sendKeys("ok");
        driver.findElement(By.id("commentModalSave")).click();
        assertEquals(driver.findElement(By.id("state")).getText(),"IN-REVIEW");
//        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("state"), "IN-REVIEW"));
        driver.findElement(By.id("Published")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("commentModalLabel"), "Add a comment"));
        driver.findElement(By.id("commentModalText")).clear();
        driver.findElement(By.id("commentModalText")).sendKeys("ok");
        driver.findElement(By.id("commentModalSave")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("state"), "PUBLISHED"));
        assertEquals(driver.findElement(By.id("state")).getText(),"PUBLISHED");
    }

    @Test(groups = "wso2.es.store", description = "Search by newly added asset Name", dependsOnMethods = "testAddasset")
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
        assertEquals(assetName, driver.findElement(By.cssSelector("h4")).getText());

    }

    @Test(groups = "wso2.es.store", description = "Search by newly added asset Version", dependsOnMethods = "testAddasset")
    public void testESStoreSearchNewlyAddedAssetsVesion() throws Exception {
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
        assertEquals(assetName, driver.findElement(By.cssSelector("h4")).getText());
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("img")));
        driver.findElement(By.cssSelector("img")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.linkText("Description"), "Description"));
        assertEquals("Version 1.2.3", driver.findElement(By.cssSelector("small")).getText());
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        resourceAdminServiceClient.deleteResource(resourcePath);
        driver.quit();
    }

    private boolean isAlertPresent() {
        try {
            driver.switchTo().alert();
            return true;
        } catch (NoAlertPresentException e) {
            return false;
        }
    }

    private String closeAlertAndGetItsText() {
        try {
            Alert alert = driver.switchTo().alert();
            String alertText = alert.getText();
            if (acceptNextAlert) {
                alert.accept();
            } else {
                alert.dismiss();
            }
            return alertText;
        } finally {
            acceptNextAlert = true;
        }
    }

}
