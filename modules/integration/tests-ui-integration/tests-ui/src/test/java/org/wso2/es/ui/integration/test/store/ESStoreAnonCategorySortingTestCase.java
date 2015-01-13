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

/**
 * Category and sorting test for Anonymous store
 */
public class ESStoreAnonCategorySortingTestCase extends BaseUITestCase {

    private ResourceAdminServiceClient resourceAdminServiceClient;
    private String popularAsset1;
    private String popularAsset2;
    private static final String REVIEW_1 = "cool!";
    private static final String REVIEW_2 = "awesome!";
    private static final String RATING_1 = "4";
    private static final String RATING_2 = "2";
    private static final String ASSET_VERSION = "1.0.0";
    private static final String ASSET_CREATED_TIME = "12";
    private static final String ASSET_TYPE = "gadget";
    private static final String BAR_CHART = "Bar Chart";
    private static final String BUBBLE_CHART = "Bubble Chart";
    private static final int MAX_POLL_COUNT = 30;
    private static final int MAX_WAIT_TIME = 30;
    private static final int TEMPLATE_COUNT = 6;
    private static final int WSO2_COUNT = 5;
    private static final int GOOGLE_COUNT = 3;

    private String assetName;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        assetName = "Asset Recent";
        driver = new ESWebDriver(BrowserManager.getWebDriver());
        wait = new WebDriverWait(driver, MAX_WAIT_TIME);
        baseUrl = getWebAppURL();
        currentUserName = userInfo.getUserName();
        currentUserPwd = userInfo.getPassword();

        resourcePath = GADGET_REGISTRY_BASE_PATH + currentUserName + "/" + assetName + "/" + ASSET_VERSION;
        AutomationContext automationContext = new AutomationContext(PRODUCT_GROUP_NAME,
                TestUserMode.SUPER_TENANT_ADMIN);
        String backendURL = automationContext.getContextUrls().getBackEndUrl();
        resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL, currentUserName,
                currentUserPwd);

        //Rating two assets to check sorting on popularity
        driver.get(baseUrl + STORE_GADGET_LIST_PAGE);
        popularAsset1 = driver.findElement(By.xpath
                ("//div[@id='assets-container']/div/div[4]/div/div/a/h4")).getText();
        driver.findElement(By.cssSelector("h4")).click();
        driver.findElement(By.linkText("User Reviews")).click();
        driver.switchTo().frame(driver.findElement(By.id("socialIfr")));
        driver.switchTo().defaultContent();
        ESUtil.login(driver, baseUrl, STORE_APP, currentUserName, currentUserPwd);
        driver.get(baseUrl + STORE_GADGET_LIST_PAGE);
        driver.findElement(By.xpath("//div[@id='assets-container']/div/div[4]/div/div/a/h4")).click();
        AssetUtil.addRatingsAndReviews(driver, REVIEW_1, RATING_1);
        driver.get(baseUrl + STORE_GADGET_LIST_PAGE);
        popularAsset2 = driver.findElement(By.xpath
                ("//div[@id='assets-container']/div[2]/div[3]/div/div/a/h4")).getText();
        driver.findElement(By.xpath("//div[@id='assets-container']/div[2]/div[3]/div/div/a/h4")).click();
        AssetUtil.addRatingsAndReviews(driver, REVIEW_2, RATING_2);

        //navigate to publisher and add and publish a new gadget to support sort by created time
        driver.get(baseUrl + PUBLISHER_URL);
        AssetUtil.addNewAsset(driver, baseUrl, ASSET_TYPE, currentUserName, assetName,
                ASSET_VERSION, ASSET_CREATED_TIME);
        driver.findElementPoll(By.linkText(assetName), MAX_POLL_COUNT);
        AssetUtil.publishAssetToStore(driver, assetName);
        driver.get(baseUrl + PUBLISHER_LOGOUT_URL);
        //navigate to store and wait for the new gadget to be visible in store
        driver.get(baseUrl + STORE_GADGET_LIST_PAGE);
        driver.findElementPoll(By.xpath("//a[contains(.,'Asset Recent')]"), MAX_POLL_COUNT);
    }

    @Test(groups = "wso2.es.store", description = "Testing sorting on popularity")
    public void testStoreSortOnPopularity() throws Exception {
        driver.get(baseUrl + STORE_GADGET_LIST_PAGE);
        //sort by popularity
        driver.findElement(By.cssSelector("i.icon-star")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("h4"), popularAsset1));
        assertEquals(driver.findElement(By.cssSelector("h4")).getText(), popularAsset1, "Popularity Sort failed");
        assertEquals(driver.findElement(By.xpath
                ("//div[@id='assets-container']/div/div[2]/div/div/a/h4")).getText(), popularAsset2,
                "Popularity Sort failed");
    }

    @Test(groups = "wso2.es.store", description = "Testing sorting on alphabetical order")
    public void testStoreSortOnAlphabeticalOrder() throws Exception {
        driver.get(baseUrl + STORE_GADGET_LIST_PAGE);
        //sort by alphabetical order
        driver.findElement(By.cssSelector("i.icon-sort-alphabetical")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.xpath
                ("//div[@id='assets-container']/div/div[3]/div/div/a/h4"), BUBBLE_CHART));
        assertEquals(assetName, driver.findElement(By.cssSelector("h4")).getText(), "Alphabetical Sort failed");
        assertEquals(BAR_CHART, driver.findElement(By.xpath
                ("//div[@id='assets-container']/div/div[2]/div/div/a/h4")).getText(), "Alphabetical Sort failed");
    }

    @Test(groups = "wso2.es.store", description = "Testing sorting on created time")
    public void testStoreSortOnCreatedTime() throws Exception {
        driver.get(baseUrl + STORE_GADGET_LIST_PAGE);
        //sort by created time
        driver.findElement(By.cssSelector("i.icon-calendar")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("h4"), assetName));
        assertEquals(assetName, driver.findElement(By.cssSelector("h4")).getText(), "Recent Sort failed");
    }

    @Test(groups = "wso2.es.store", description = "Testing category Google")
    public void testCategoryGoogle() throws Exception {
        driver.get(baseUrl + STORE_GADGET_LIST_PAGE);
        //google category
        driver.findElement(By.cssSelector("i.icon-caret-down")).click();
        driver.findElement(By.linkText("Google")).click();
        assertEquals(GOOGLE_COUNT, driver.findElements(By.cssSelector("div.asset-details")).size(), "Google Category wrong");
    }

    @Test(groups = "wso2.es.store", description = "Testing category WSO2")
    public void testCategoryWSO2() throws Exception {
        driver.get(baseUrl + STORE_GADGET_LIST_PAGE);
        driver.findElement(By.cssSelector("i.icon-caret-down")).click();
        driver.findElement(By.linkText("WSO2")).click();
        assertEquals(WSO2_COUNT, driver.findElements(By.cssSelector("div.asset-details")).size(), "WSO2 Category wrong");
    }

    @Test(groups = "wso2.es.store", description = "Testing category template")
    public void testCategoryTemplate() throws Exception {
        driver.get(baseUrl + STORE_GADGET_LIST_PAGE);
        driver.findElement(By.cssSelector("i.icon-caret-down")).click();
        driver.findElement(By.linkText("Templates")).click();
        assertEquals(TEMPLATE_COUNT, driver.findElements(By.cssSelector("div.span3.asset")).size(),
                "Templates Category wrong");
    }

    @Test(groups = "wso2.es.store", description = "Testing category drop down")
    public void testCategoryMenu() throws Exception {
        driver.get(baseUrl + STORE_GADGET_LIST_PAGE);
        driver.findElement(By.cssSelector("i.icon-caret-down")).click();
        driver.findElement(By.linkText("Templates")).click();
        assertEquals("Templates", driver.findElement(By.cssSelector("div.breadcrumb-head")).getText(),
                "Category drop down not showing selected category ");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        resourceAdminServiceClient.deleteResource(resourcePath);
        driver.quit();
    }
}
