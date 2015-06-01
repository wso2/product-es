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

package org.wso2.es.ui.integration.test.publisher;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.Select;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.es.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.es.ui.integration.util.BaseUITestCase;
import org.wso2.es.ui.integration.util.ESUtil;
import org.wso2.es.ui.integration.util.ESWebDriver;
import static org.testng.Assert.*;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;

/**
 * Add and Edit asset test for Super tenant:Super Admin & Super User
 */
public class ESPublisherAddEditAssetTestCase extends BaseUITestCase {

    private TestUserMode userMode;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private static final String ASSET_VERSION = "1.0.0";
    private static final String ASSET_URL_1 = "http://test";
    private static final String ASSET_URL_2 = "http://wso2.com/";
    private static final String ASSET_DESCRIPTION_1 = "for store";
    private static final String ASSET_DESCRIPTION_2 = "Edited Test description";
    private static final String ASSET_CATEGORY_1 = "Google";
    private static final String ASSET_CATEGORY_2 = "WSO2";
    private static final int MAX_POLL_COUNT = 30;
    private String assetName;

    @Factory(dataProvider = "userMode")
    public ESPublisherAddEditAssetTestCase(TestUserMode userMode, String assetName) {
        this.userMode = userMode;
        this.assetName = assetName;
    }

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init(userMode);
        currentUserName = userInfo.getUserName().split("@")[0];
        currentUserPwd = userInfo.getPassword();
        resourcePath = GADGET_REGISTRY_BASE_PATH + currentUserName + "/" + assetName + "/" + ASSET_VERSION;
        driver = new ESWebDriver(BrowserManager.getWebDriver());
        baseUrl = getWebAppURL();
        AutomationContext automationContext = new AutomationContext(PRODUCT_GROUP_NAME, TestUserMode.SUPER_TENANT_ADMIN);
        adminUserName = automationContext.getSuperTenant().getTenantAdmin().getUserName();
        adminUserPwd = automationContext.getSuperTenant().getTenantAdmin().getPassword();
        backendURL = automationContext.getContextUrls().getBackEndUrl();
        resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL, adminUserName, adminUserPwd);
        ESUtil.login(driver, baseUrl, PUBLISHER_APP, currentUserName, currentUserPwd);
    }

    @Test(groups = "wso2.es.publisher", description = "Testing adding a new asset")
    public void testAddAsset() throws Exception {
        driver.get(baseUrl + PUBLISHER_GADGET_LIST_PAGE);
        driver.findElement(By.linkText("ADD GADGET")).click();
        driver.findElement(By.name("overview_name")).clear();
        driver.findElement(By.name("overview_name")).sendKeys(assetName);
        driver.findElement(By.name("overview_version")).clear();
        driver.findElement(By.name("overview_version")).sendKeys(ASSET_VERSION);
        new Select(driver.findElement(By.name("overview_category"))).selectByVisibleText(ASSET_CATEGORY_1);
        driver.findElement(By.name("overview_url")).clear();
        driver.findElement(By.name("overview_url")).sendKeys(ASSET_URL_1);
        driver.findElement(By.name("overview_description")).clear();
        driver.findElement(By.name("overview_description")).sendKeys(ASSET_DESCRIPTION_1);
        assertTrue(isElementPresent(driver, By.name("images_thumbnail")));
        driver.findElement(By.name("images_thumbnail")).sendKeys(FrameworkPathUtil.getReportLocation()
                +"/../src/test/resources/images/thumbnail.jpg");
        driver.findElement(By.name("images_banner")).sendKeys(FrameworkPathUtil.getReportLocation()
                +"/../src/test/resources/images/banner.jpg");
        driver.findElement(By.id("btn-create-asset")).click();

        driver.findElementPoll(By.linkText(assetName), MAX_POLL_COUNT);
        //check if the created gadget is shown
        assertTrue(isElementPresent(driver, By.linkText(assetName)), "Adding an asset failed for user:" + currentUserName);
        driver.findElement(By.linkText(assetName)).click();
        assertEquals(currentUserName, driver.findElement(By.xpath("//div[@id='collapseOverview']/div[1]/div[2]")).getText(),
                "Incorrect provider");
        assertEquals(assetName, driver.findElement(By.xpath("//div[@id='collapseOverview']/div[2]/div[2]")).getText(),
                "Incorrect asset name");
        assertEquals(ASSET_VERSION, driver.findElement(By.xpath("//div[@id='collapseOverview']/div[3]/div[2]")).getText(),
                "Incorrect version");
        assertEquals(ASSET_CATEGORY_1, driver.findElement(By.xpath("//div[@id='collapseOverview']/div[4]/div[2]")).getText());
        assertEquals(ASSET_URL_1, driver.findElement(By.xpath("//div[@id='collapseOverview']/div[5]/div[2]")).getText(),
                "Incorrect URL");
        assertEquals(ASSET_DESCRIPTION_1, driver.findElement(By.xpath("//div[@id='collapseOverview']/div[6]/div[2]")).getText(),
                "Incorrect description");
    }

    @Test(groups = "wso2.es.publisher", description = "Testing editing an asset", dependsOnMethods = "testAddAsset")
    public void testEditAsset() throws Exception {
        driver.get(baseUrl + PUBLISHER_GADGET_LIST_PAGE);
        driver.findElement(By.linkText(assetName)).click();
        driver.findElement(By.linkText("EDIT")).click();
        new Select(driver.findElement(By.name("overview_category"))).selectByVisibleText(ASSET_CATEGORY_2);
        driver.findElement(By.name("overview_url")).clear();
        driver.findElement(By.name("overview_url")).sendKeys(ASSET_URL_2);
        driver.findElement(By.name("overview_description")).clear();
        driver.findElement(By.name("overview_description")).sendKeys(ASSET_DESCRIPTION_2);
        driver.findElement(By.id("editAssetButton")).click();
        closeAlertAndGetItsText(driver, true);

        //check updated info
        driver.findElement(By.linkText("Overview")).click();
        assertEquals(assetName, driver.findElement(By.xpath("//h1[contains(.,'"+assetName+"')]")).getText());
        assertEquals(currentUserName, driver.findElement(By.name("overview_provider")).getAttribute("value"),
                "Incorrect provider");
        //assertEquals(
        assertEquals(assetName, driver.findElement(By.name("overview_name")).getAttribute("value"),
                "Incorrect asset name");
        assertEquals(ASSET_VERSION, driver.findElement(By.name("overview_version")).getAttribute("value"),
                "Incorrect version");
        assertEquals(ASSET_CATEGORY_2, driver.findElement(By.name("overview_category")).getAttribute("value"),
                "Incorrect category");
        assertEquals(ASSET_URL_2, driver.findElement(By.name("overview_url")).getAttribute("value"),
                "Incorrect URL");
        assertEquals(ASSET_DESCRIPTION_2, driver.findElement(By.name("overview_description")).getAttribute("value"),
                "Incorrect description");
    }



    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        //delete resources and logout
        resourceAdminServiceClient.deleteResource(resourcePath);
        driver.get(baseUrl + PUBLISHER_LOGOUT_URL);
        driver.quit();
    }

    @DataProvider(name = "userMode")
    private static Object[][] userModeProvider() {
        return new Object[][]{{TestUserMode.SUPER_TENANT_ADMIN, "Add Edit asset - SuperAdmin"},
                {TestUserMode.SUPER_TENANT_USER, "Add Edit asset - SuperUser"}};
    }

}
