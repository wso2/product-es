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
import org.testng.annotations.*;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.es.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.es.ui.integration.util.BaseUITestCase;
import org.wso2.es.ui.integration.util.ESUtil;
import org.wso2.es.ui.integration.util.ESWebDriver;

import static org.testng.Assert.*;

/**
 * Add and Edit asset test for Tenant:Tenant Admin & Tenant User
 */
public class ESPublisherTenantAddEditAssetTestCase extends BaseUITestCase {

    private ResourceAdminServiceClient resourceAdminServiceClient;
    private TestUserMode userMode;
    private static final String ASSET_VERSION = "1.0.0";
    private static final String ASSET_CREATED_TIME = "12";
    private static final String ASSET_URL_1 = "http://test";
    private static final String ASSET_URL_2 = "http://wso2.com/";
    private static final String ASSET_DESCRIPTION_1 = "for store";
    private static final String ASSET_DESCRIPTION_2 = "Edited Test description";
    private static final String ASSET_CATEGORY_1 = "Google";
    private static final String ASSET_CATEGORY_2 = "WSO2";
    private static final int MAX_POLL_COUNT = 30;
    private String assetName;

    @Factory(dataProvider = "userMode")
    public ESPublisherTenantAddEditAssetTestCase(TestUserMode userMode, String assetName) {
        this.userMode = userMode;
        this.assetName = assetName;
    }

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init(userMode);
        currentUserName = userInfo.getUserName();
        currentUserPwd = userInfo.getPassword();
        driver = new ESWebDriver(BrowserManager.getWebDriver());
        baseUrl = getStorePublisherUrl();
        AutomationContext automationContext = new AutomationContext(PRODUCT_GROUP_NAME, TestUserMode.TENANT_ADMIN);
        adminUserName = automationContext.getContextTenant().getTenantAdmin().getUserName();
        adminUserPwd = automationContext.getContextTenant().getTenantAdmin().getPassword();
        backendURL = automationContext.getContextUrls().getBackEndUrl();
        resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL, adminUserName, adminUserPwd);
        this.providerName = currentUserName.split("@")[0];
        this.resourcePath = GADGET_REGISTRY_BASE_PATH + providerName + "/" + assetName + "/" + ASSET_VERSION;
        ESUtil.login(driver, baseUrl, PUBLISHER_APP, currentUserName, currentUserPwd);
    }

    @Test(groups = "wso2.es.publisher", description = "Testing adding a new asset")
    public void testAddAsset() throws Exception {
        driver.get(baseUrl + PUBLISHER_GADGET_LIST_PAGE);
        driver.findElement(By.linkText("Add")).click();
        driver.findElement(By.name("overview_provider")).clear();
        driver.findElement(By.name("overview_provider")).sendKeys(providerName);
        driver.findElement(By.name("overview_name")).clear();
        driver.findElement(By.name("overview_name")).sendKeys(assetName);
        driver.findElement(By.name("overview_version")).clear();
        driver.findElement(By.name("overview_version")).sendKeys(ASSET_VERSION);
        driver.findElement(By.name("overview_createdtime")).clear();
        driver.findElement(By.name("overview_createdtime")).sendKeys(ASSET_CREATED_TIME);
        new Select(driver.findElement(By.name("overview_category"))).selectByVisibleText(ASSET_CATEGORY_1);
        driver.findElement(By.name("overview_url")).clear();
        driver.findElement(By.name("overview_url")).sendKeys(ASSET_URL_1);
        driver.findElement(By.name("overview_description")).clear();
        driver.findElement(By.name("overview_description")).sendKeys(ASSET_DESCRIPTION_1);
        driver.findElement(By.id("btn-create-asset")).click();

        driver.findElementPoll(By.linkText(assetName), MAX_POLL_COUNT);
        //check if the created gadget is shown
        assertTrue(isElementPresent(By.linkText(assetName)), "Adding an asset failed for user:" + currentUserName);
    }

    @Test(groups = "wso2.es.publisher", description = "Testing editing an asset",
            dependsOnMethods = "testAddAsset")
    public void testEditAsset() throws Exception {
        driver.get(baseUrl + PUBLISHER_GADGET_LIST_PAGE);
        driver.findElement(By.linkText(assetName)).click();
        driver.findElement(By.linkText("Edit")).click();
        new Select(driver.findElement(By.name("overview_category"))).selectByVisibleText(ASSET_CATEGORY_2);
        driver.findElement(By.name("overview_url")).clear();
        driver.findElement(By.name("overview_url")).sendKeys(ASSET_URL_2);
        driver.findElement(By.name("overview_description")).clear();
        driver.findElement(By.name("overview_description")).sendKeys(ASSET_DESCRIPTION_2);
        driver.findElement(By.id("editAssetButton")).click();
        closeAlertAndGetItsText();

        //check updated info
        driver.findElement(By.linkText("Overview")).click();
        assertEquals(assetName, driver.findElement(By.cssSelector("h4")).getText());
        assertEquals(driver.findElement(By.xpath("//div[@id='view']/div[2]/div/div/div[2]/table[2]/tbody/tr/td[2]"))
                .getText(), providerName, "Incorrect provider");
        assertEquals(driver.findElement(By.xpath("//div[@id='view']/div[2]/div/div/div[2]/table[2]/tbody/tr[2]/td[2]"))
                .getText(), assetName, "Incorrect asset name");
        assertEquals(driver.findElement(By.xpath("//div[@id='view']/div[2]/div/div/div[2]/table[2]/tbody/tr[3]/td[2]"))
                .getText(), ASSET_VERSION, "Incorrect version");
        assertEquals(driver.findElement(By.xpath("//div[@id='view']/div[2]/div/div/div[2]/table[2]/tbody/tr[5]/td[2]"))
                .getText(), "WSO2", "Incorrect Category");
        assertEquals(driver.findElement(By.xpath("//div[@id='view']/div[2]/div/div/div[2]/table[2]/tbody/tr[6]/td[2]"))
                .getText(), ASSET_URL_2, "Incorrect URL");
        assertEquals(driver.findElement(By.xpath("//div[@id='view']/div[2]/div/div/div[2]/table[2]/tbody/tr[7]/td[2]"))
                .getText(), ASSET_DESCRIPTION_2, "Incorrect description");
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
        return new Object[][]{{TestUserMode.TENANT_ADMIN, "Add Edit asset - TenantAdmin"},
                {TestUserMode.TENANT_USER, "Add Edit asset - TenantUser"}};
    }

}
