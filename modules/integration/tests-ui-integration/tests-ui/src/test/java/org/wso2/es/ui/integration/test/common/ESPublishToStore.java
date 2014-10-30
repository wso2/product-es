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

package org.wso2.es.ui.integration.test.common;

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
import org.wso2.es.ui.integration.util.ESUtil;
import org.wso2.es.ui.integration.util.ESWebDriver;

public class ESPublishToStore extends ESIntegrationUITest {
    private ESWebDriver driver;
    private WebDriverWait wait;
    private String baseUrl;
    private boolean acceptNextAlert = true;
    private String webApp = "publisher";
    private StringBuffer verificationErrors = new StringBuffer();
    private String providerName;
    private String assetName = "Publishing Asset";
    private String resourcePath;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private String backendURL;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = new ESWebDriver();
        baseUrl = getWebAppURL();
        driver.get(baseUrl + "/publisher/asts/gadget/list");
        wait = new WebDriverWait(driver, 30);
        providerName = userInfo.getUserName();
        resourcePath = "/_system/governance/gadgets/" + this.providerName + "/" + this.assetName + "/1.0.0";
        AutomationContext automationContext = new AutomationContext("ES", TestUserMode.SUPER_TENANT_ADMIN);
        backendURL = automationContext.getContextUrls().getBackEndUrl();
        resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL, userInfo.getUserName(),
                userInfo.getPassword());
        ESUtil.login(driver, baseUrl, webApp, userInfo.getUserName(), userInfo.getPassword());
    }

    @Test(groups = "wso2.es.common", description = "Testing Publishing an asset to store")
    public void testESPublishToStore() throws Exception {
        driver.findElement(By.linkText("Add")).click();
        driver.findElement(By.name("overview_provider")).clear();
        driver.findElement(By.name("overview_provider")).sendKeys(userInfo.getUserName());
        driver.findElement(By.name("overview_name")).clear();
        driver.findElement(By.name("overview_name")).sendKeys(assetName);
        driver.findElement(By.name("overview_version")).clear();
        driver.findElement(By.name("overview_version")).sendKeys("1.0.0");
        driver.findElement(By.name("overview_createdtime")).clear();
        driver.findElement(By.name("overview_createdtime")).sendKeys("12");
        driver.findElement(By.name("overview_url")).clear();
        driver.findElement(By.name("overview_url")).sendKeys("http://test");
        driver.findElement(By.name("overview_description")).clear();
        driver.findElement(By.name("overview_description")).sendKeys("to store");
        driver.findElement(By.id("btn-create-asset")).click();
        if (isAlertPresent()) {
            closeAlertAndGetItsText();
        }
        driver.findElement(By.cssSelector("a.btn")).click();
        driver.findElementPoll(By.linkText(assetName), 30);
        driver.findElement(By.linkText("Publishing Asset")).click();
        driver.findElement(By.linkText("Life Cycle")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector("div.pull-left"),
                "Lifecycle - " + assetName));
        driver.findElement(By.id("In-Review")).click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("commentModalLabel"), "Add a comment"));

        driver.findElement(By.id("commentModalText")).clear();
        driver.findElement(By.id("commentModalText")).sendKeys("ok");
        driver.findElement(By.id("commentModalSave")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("state"), "IN-REVIEW"));
        driver.findElement(By.id("Published")).click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("commentModalLabel"), "Add a comment"));

        driver.findElement(By.id("commentModalText")).clear();
        driver.findElement(By.id("commentModalText")).sendKeys("ok");
        driver.findElement(By.id("commentModalSave")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("state"), "PUBLISHED"));
        driver.get(baseUrl + "/store");
        String newName;
        do {
            driver.findElement(By.cssSelector("a.brand")).click();
            newName = driver.findElement(By.cssSelector("h4")).getText();
        } while (!newName.equalsIgnoreCase(assetName));
        try {
            assertEquals(assetName, driver.findElement(By.cssSelector("h4")).getText());
            driver.findElement(By.cssSelector("div.asset-author-category > ul > li")).click();
            assertEquals(assetName, driver.findElement(By.cssSelector("h3")).getText());
            assertEquals("to store", driver.findElement(By.cssSelector("p")).getText());
        } catch (Error e) {
            verificationErrors.append(e.toString());
        }
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        resourceAdminServiceClient.deleteResource(resourcePath);
        driver.get(baseUrl + "/publisher/logout");
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
