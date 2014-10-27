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

package org.wso2.es.ui.integration.test.publisher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.Select;
import org.testng.annotations.*;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.es.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.es.integration.common.utils.ESIntegrationUITest;
import org.wso2.es.ui.integration.util.ESUtil;
import org.wso2.es.ui.integration.util.ESWebDriver;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.*;

public class ESPublisherTenantAddEditAssetTestCase extends ESIntegrationUITest {
    private static final Log log = LogFactory.getLog(ESPublisherTenantAddEditAssetTestCase.class);

    private ESWebDriver driver;
    private String baseUrl;
    private String webApp = "publisher";
    private boolean acceptNextAlert = true;
    private StringBuffer verificationErrors = new StringBuffer();

    private String assetName;
    private String providerName;

    private String adminUserName = "admin";
    private String adminUserPwd = "admin";

    private String currentUserName;
    private String currentUserPwd;
    private String resourcePath;

    private String backendURL;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private String resourceLocation;

    private String email = "esmailsample@gmail.com";
    private String emailPwd = "esMailTest";

    private String failText = "Unable to add the asset";


    @Factory(dataProvider = "userMode")
    public ESPublisherTenantAddEditAssetTestCase(String user, String pwd, String assetName) {
        this.currentUserName = user;
        this.currentUserPwd = pwd;
        this.assetName = assetName;
        this.providerName = user.split("@")[0];
        this.resourcePath = "/_system/governance/gadgets/" + this.providerName + "/" + this.assetName + "/1.0.0";
    }

    @BeforeClass(alwaysRun = true, enabled = true)
    public void setUp() throws Exception {
        log.info("************ Starting Add Edit Test Case for Tenant:" + currentUserName + "********");
        super.init();
        driver = new ESWebDriver();
        baseUrl = getWebAppURL();
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        AutomationContext automationContext = new AutomationContext("ES", TestUserMode.SUPER_TENANT_ADMIN);
        backendURL = automationContext.getContextUrls().getBackEndUrl();
        resourceLocation = getResourceLocation();
        resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL, adminUserName, adminUserPwd);

        ESUtil.login(driver, baseUrl, webApp, currentUserName, currentUserPwd);
    }

    @Test(groups = "wso2.es", description = "Testing adding a new asset", enabled = true)
    public void testAddAsset() throws Exception {
        log.info("----------------------------- Add Asset Test ----------------------------------------");
        driver.get(baseUrl + "/publisher/asts/gadget/list");
        driver.findElement(By.linkText("Add")).click();
        driver.findElement(By.name("overview_provider")).clear();
        driver.findElement(By.name("overview_provider")).sendKeys(providerName);
        driver.findElement(By.name("overview_name")).clear();
        driver.findElement(By.name("overview_name")).sendKeys(assetName);
        driver.findElement(By.name("overview_version")).clear();
        driver.findElement(By.name("overview_version")).sendKeys("1.0.0");
        driver.findElement(By.name("overview_createdtime")).clear();
        driver.findElement(By.name("overview_createdtime")).sendKeys("12");
        new Select(driver.findElement(By.name("overview_category"))).selectByVisibleText("Google");
        driver.findElement(By.name("overview_url")).clear();
        driver.findElement(By.name("overview_url")).sendKeys("https://www.google.com");
        driver.findElement(By.name("overview_description")).clear();
        driver.findElement(By.name("overview_description")).sendKeys("Test description");
        driver.findElement(By.id("btn-create-asset")).click();

        driver.findElementPoll(By.linkText(assetName),30);
//        boolean isSuccessful;
//        if (isAlertPresent()) {
//            isSuccessful = false;
//        } else {
//            do {
//                driver.get(baseUrl + "/publisher/asts/gadget/list");
//            } while (!isElementPresent(By.linkText(assetName)));
//            isSuccessful = true;
//        }
        assertTrue(isElementPresent(By.linkText(assetName)), "Adding an asset failed for user:" + currentUserName);
//        assertTrue(isSuccessful, "Adding an asset failed for user:" + currentUserName);

    }

    @Test(groups = "wso2.es", description = "Testing editing an asset", dependsOnMethods = "testAddAsset",
            enabled = true)
    public void testEditAsset() throws Exception {
        log.info("----------------------------- Edit Asset Test ----------------------------------------");
        driver.get(baseUrl + "/publisher/asts/gadget/list");
        driver.findElement(By.linkText(assetName)).click();
        driver.findElement(By.linkText("Edit")).click();
        new Select(driver.findElement(By.name("overview_category"))).selectByVisibleText("WSO2");
        driver.findElement(By.name("overview_url")).clear();
        driver.findElement(By.name("overview_url")).sendKeys("http://wso2.com/");
        driver.findElement(By.name("overview_description")).clear();
        driver.findElement(By.name("overview_description")).sendKeys("Edited Test description");
        driver.findElement(By.id("editAssetButton")).click();
        closeAlertAndGetItsText(driver, true);

        driver.findElement(By.linkText("Overview")).click();
        assertEquals(assetName, driver.findElement(By.cssSelector("h4")).getText());
        assertEquals(driver.findElement(By.xpath
                ("//div[@id='view']/div[2]/div/div/div[2]/table[2]/tbody/tr/td[2]")).getText(), providerName,
                "Incorrect provider");
        assertEquals(driver.findElement(By.xpath
                ("//div[@id='view']/div[2]/div/div/div[2]/table[2]/tbody/tr[2]/td[2]")).getText(), assetName,
                "Incorrect asset name");
        assertEquals(driver.findElement(By.xpath
                ("//div[@id='view']/div[2]/div/div/div[2]/table[2]/tbody/tr[3]/td[2]")).getText(), "1.0.0",
                "Incorrect version");
        assertEquals(driver.findElement(By.xpath
                ("//div[@id='view']/div[2]/div/div/div[2]/table[2]/tbody/tr[5]/td[2]")).getText(), "WSO2",
                "Incorrect Category");
        assertEquals(driver.findElement(By.xpath
                ("//div[@id='view']/div[2]/div/div/div[2]/table[2]/tbody/tr[6]/td[2]")).getText(),
                "http://wso2.com/", "Incorrect URL");
        assertEquals(driver.findElement(By.xpath
                ("//div[@id='view']/div[2]/div/div/div[2]/table[2]/tbody/tr[7]/td[2]")).getText(),
                "Edited Test description", "Incorrect description");
    }

    @AfterClass(alwaysRun = true, enabled = true)
    public void tearDown() throws Exception {
        resourceAdminServiceClient.deleteResource(resourcePath);
        ESUtil.logout(driver, baseUrl, "publisher", providerName);
        ESUtil.deleteAllEmail(resourceLocation + File.separator + "notifications" + File.separator + "smtp" +
                ".properties", emailPwd, email);
        driver.close();
        driver.quit();
        String verificationErrorString = verificationErrors.toString();
        if (!"".equals(verificationErrorString)) {
            fail(verificationErrorString);
        }
        log.info("************ Finishing Add Edit Test Case for Tenant:" + currentUserName + "********");
    }

    @DataProvider(name = "userMode")
    public static Object[][] userInfoProvider() {
        return new Object[][]{{"admin@wso2.com", "admin", "Add Edit asset - TenantAdmin"}, {"testuser11@wso2.com",
                "testuser11", "Add Edit asset - TenantUser"}};
    }

    private boolean isElementPresent(By by) {
        try {
            driver.findElement(by);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    private boolean isAlertPresent() {
        try {
            driver.switchTo().alert();
            return true;
        } catch (NoAlertPresentException e) {
            return false;
        }
    }

    private static String closeAlertAndGetItsText(WebDriver driver, boolean acceptNextAlert) {
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
