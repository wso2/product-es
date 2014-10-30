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
import org.openqa.selenium.Alert;

import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;

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
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.es.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.es.integration.common.utils.ESIntegrationUITest;
import org.wso2.es.ui.integration.util.AssetUtil;
import org.wso2.es.ui.integration.util.ESUtil;
import org.wso2.es.ui.integration.util.ESWebDriver;

public class ESPublisherLCTransitionTestCase extends ESIntegrationUITest {
    private static final Log log = LogFactory.getLog(ESPublisherLCTransitionTestCase.class);
    private ESWebDriver driver;
    private WebDriverWait wait;
    private String baseUrl;
    private boolean acceptNextAlert = true;
    private StringBuffer verificationErrors = new StringBuffer();
    private String webApp = "publisher";
    private String currentUserName;
    private String currentUserPwd;
    private String assetName = "LC Test Asset";
    private String assetVersion = "1.2.3";
    private String resourcePath;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private String backendURL;

    private String adminUserName;
    private String adminUserPwd;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        this.currentUserName = userInfo.getUserName();
        this.currentUserPwd = userInfo.getPassword();
        resourcePath = "/_system/governance/gadgets/" + currentUserName + "/" + assetName + "/" + assetVersion;
        driver = new ESWebDriver();
        wait = new WebDriverWait(driver, 30);
        baseUrl = getWebAppURL();
        ESUtil.login(driver, baseUrl, webApp, currentUserName, currentUserPwd);
        driver.get(baseUrl + "/publisher/asts/gadget/list");
        AssetUtil.addNewAsset(driver, baseUrl, "gadget", currentUserName, assetName, assetVersion, "12");
        if(isAlertPresent()){
            String modalText = closeAlertAndGetItsText();
            log.warn("modal dialog appeared" + modalText);
        }
        AutomationContext automationContext = new AutomationContext("ES", TestUserMode.SUPER_TENANT_ADMIN);
        backendURL = automationContext.getContextUrls().getBackEndUrl();
        adminUserName = automationContext.getSuperTenant().getTenantAdmin().getUserName();
        adminUserPwd = automationContext.getSuperTenant().getTenantAdmin().getPassword();
        resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL, adminUserName, adminUserPwd);
    }

    @Test(groups = "wso2.es.publisher", description = "Testing LC transition")
    public void testLc() throws Exception {
        driver.findElementPoll(By.linkText(assetName),30);
        driver.findElement(By.linkText(assetName)).click();
        driver.findElement(By.linkText("Life Cycle")).click();
        driver.findElement(By.id("In-Review")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("commentModalLabel"), "Add a comment"));

        driver.findElement(By.id("commentModalText")).clear();
        driver.findElement(By.id("commentModalText")).sendKeys("test");
        driver.findElement(By.id("commentModalSave")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.xpath("//table[@id='lc-history']/tbody/tr/td" +
                "[2]"), "admin changed the asset from Created to In-Review"));
        assertEquals("admin changed the asset from Created to In-Review",
                driver.findElement(By.xpath("//table[@id='lc-history']/tbody/tr/td[2]")).getText());
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.get(baseUrl + "/publisher/logout");
        resourceAdminServiceClient.deleteResource(resourcePath);
        driver.quit();
        String verificationErrorString = verificationErrors.toString();
        if (!"".equals(verificationErrorString)) {
            fail(verificationErrorString);
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