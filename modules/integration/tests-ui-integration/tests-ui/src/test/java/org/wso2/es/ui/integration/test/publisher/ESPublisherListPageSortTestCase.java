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
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.*;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.es.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.es.integration.common.utils.ESIntegrationUITest;
import org.wso2.es.ui.integration.util.AssetUtil;
import org.wso2.es.ui.integration.util.ESUtil;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;


public class ESPublisherListPageSortTestCase extends ESIntegrationUITest {
    private static final Log log = LogFactory.getLog(ESPublisherListPageSortTestCase.class);

    private WebDriver driver;
    private String baseUrl;
    private String webApp = "publisher";
    private boolean acceptNextAlert = true;
    private StringBuffer verificationErrors = new StringBuffer();
    private TestUserMode userMode;

    private String adminUserName;
    private String adminUserPwd;

    private String normalUserName;
    private String normalUserPwd;

    private String currentUserName;
    private String currentUserPwd;
    private String resourcePath;
    private String assetName = "Sort Asset";

    private ResourceAdminServiceClient resourceAdminServiceClient;
    private String resourceLocation;

    private String email = "esmailsample@gmail.com";
    private String emailPwd = "esMailTest";
    WebDriverWait wait;

    @Factory(dataProvider = "userMode")
    public ESPublisherListPageSortTestCase(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true, enabled = true)
    public void setUp() throws Exception {
        log.info("************ Starting Add Edit Test Case for Tenant:" + currentUserName + "********");
        super.init(userMode);
        this.currentUserName = userInfo.getUserName().split("@")[0];
        this.currentUserPwd = userInfo.getPassword().split("0")[0];
        driver = BrowserManager.getWebDriver();
        baseUrl = getWebAppURL();
        driver.manage().timeouts().implicitlyWait(50, TimeUnit.SECONDS);
        wait = new WebDriverWait(driver, 30);
        AutomationContext automationContext = new AutomationContext("ES", TestUserMode.SUPER_TENANT_ADMIN);
        adminUserName = automationContext.getSuperTenant().getTenantAdmin().getUserName();
        adminUserPwd = automationContext.getSuperTenant().getTenantAdmin().getPassword();
        normalUserName = automationContext.getSuperTenant().getTenantUser("user1").getUserName();
        normalUserPwd = automationContext.getSuperTenant().getTenantUser("user1").getPassword();
        this.resourcePath = "/_system/governance/gadgets/" + this.normalUserName + "/" + this.assetName + "/2.0.0";
        String backendURL = automationContext.getContextUrls().getBackEndUrl();
        resourceLocation = getResourceLocation();
        resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL, adminUserName, adminUserPwd);
        if (currentUserName.equals(adminUserName)) {
            ESUtil.login(driver, baseUrl, webApp, normalUserName, normalUserPwd);
            AssetUtil.addNewAsset(driver, baseUrl, "gadget", normalUserName, assetName, "2.0.0", "12");
            if (isAlertPresent()) {
                String alert = closeAlertAndGetItsText();
                log.warn(alert + ": modal box appeared");
            }
            driver.get(baseUrl + "/publisher/logout");
            wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("h2.form-signin-heading"),
                    "Sign in"));
        }
        ESUtil.login(driver, baseUrl, webApp, currentUserName, currentUserPwd);
    }

    @Test(groups = "wso2.es", description = "Test sort by name, provider, version & created time", enabled = true)
    public void testListPageSort() throws Exception {
        log.info("----------------------------- Asset List Sort Test ----------------------------------------");
        do {
            driver.get(baseUrl + "/publisher/asts/gadget/list");
        } while (!isElementPresent(By.linkText(assetName)));
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.partialLinkText("NAME"), "NAME"));
        try {
            driver.findElement(By.partialLinkText("NAME")).click();
            assertEquals("Bar Chart", driver.findElement(By.xpath("//tbody[@id='list-asset-table-body']/tr[1]/td[2]"))
                    .getText(), "Sort on name failed");
            assertEquals("WSO2 Jira", driver.findElement(By.xpath("//tbody[@id='list-asset-table-body']/tr[14]/td[2]"))
                    .getText(), "Sort on name failed");

            driver.findElement(By.linkText("VERSION")).click();
            assertEquals("1.0.0", driver.findElement(By.xpath("//tbody[@id='list-asset-table-body']/tr[1]/td[3]"))
                    .getText(), "Sort on version failed");
            assertEquals("2.0.0", driver.findElement(By.xpath("//tbody[@id='list-asset-table-body']/tr[14]/td[3]"))
                    .getText(), "Sort on version failed");

            driver.findElement(By.linkText("OWNER")).click();
            assertEquals(adminUserName, driver.findElement(By.xpath("//tbody[@id='list-asset-table-body']/tr[1]/td[4]"))
                    .getText(), "Sort on owner failed");
            assertEquals(normalUserName, driver.findElement(By.xpath
                    ("//tbody[@id='list-asset-table-body']/tr[14]/td[4]"))
                    .getText(), "Sort on owner failed");

            driver.findElement(By.linkText("CREATED")).click();
            assertEquals(assetName, driver.findElement(By.xpath("//tbody[@id='list-asset-table-body']/tr[1]/td[2]"))
                    .getText(), "Sort on created time failed");
        } catch (Error e) {
            verificationErrors.append(e.toString());
        }
    }

    @AfterClass(alwaysRun = true, enabled = true)
    public void tearDown() throws Exception {
        if (currentUserName.equals(normalUserName)) {
            resourceAdminServiceClient.deleteResource(resourcePath);
        }
        driver.get(baseUrl + "/publisher/logout");
        ESUtil.deleteAllEmail(resourceLocation + File.separator + "notifications" + File.separator + "smtp" +
                ".properties", emailPwd, email);
        driver.quit();
        String verificationErrorString = verificationErrors.toString();
        if (!"".equals(verificationErrorString)) {
            fail(verificationErrorString);
        }
        log.info("************ Finishing Add Edit Test Case for Tenant:" + currentUserName + "********");
    }

    @DataProvider(name = "userMode")
    private static TestUserMode[][] userModeProvider() {
        return new TestUserMode[][]{
                new TestUserMode[]{TestUserMode.SUPER_TENANT_ADMIN},
                new TestUserMode[]{TestUserMode.SUPER_TENANT_USER},
        };
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
