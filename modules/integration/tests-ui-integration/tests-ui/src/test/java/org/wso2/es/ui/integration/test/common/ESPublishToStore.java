/*
 * Copyright (c) 2014, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.es.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.es.integration.common.utils.ESIntegrationUITest;
import org.wso2.es.ui.integration.util.ESUtil;
import org.wso2.es.ui.integration.util.ESWebDriver;
import static org.testng.Assert.assertEquals;

/**
 * Create a new asset in publisher and publish it to store
 * Check if it can be seen store side and verify details
 */
public class ESPublishToStore extends ESIntegrationUITest {
    private ESWebDriver driver;
    private String baseUrl;
    private boolean acceptNextAlert = true;
    private String webApp = "publisher";
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
        providerName = userInfo.getUserName();
        resourcePath = "/_system/governance/gadgets/" + this.providerName + "/" + this.assetName
                + "/1.0.0";
        AutomationContext automationContext = new AutomationContext("ES",
                TestUserMode.SUPER_TENANT_ADMIN);
        backendURL = automationContext.getContextUrls().getBackEndUrl();
        resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL,
                userInfo.getUserName(), userInfo.getPassword());
        ESUtil.login(driver, baseUrl, webApp, userInfo.getUserName(), userInfo.getPassword());
    }

    @Test(groups = "wso2.es.common", description = "Testing Publishing an asset to store")
    public void testESPublishToStore() throws Exception {
        //Add a new gadget with info
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
        //publish the gadget to store
        driver.findElement(By.cssSelector("a.btn")).click();
        driver.findElementPoll(By.linkText(assetName), 30);
        driver.findElement(By.linkText("Publishing Asset")).click();
        driver.findElement(By.linkText("Life Cycle")).click();

        driver.findElement(By.id("In-Review")).click();
        driver.findElement(By.id("commentModalText")).clear();
        driver.findElement(By.id("commentModalText")).sendKeys("ok");
        driver.findElement(By.id("commentModalSave")).click();

        driver.get(driver.getCurrentUrl());
        driver.findElement(By.id("Published")).click();
        driver.findElement(By.id("commentModalText")).clear();
        driver.findElement(By.id("commentModalText")).sendKeys("ok");
        driver.findElement(By.id("commentModalSave")).click();
        //navigate to store to check the published gadget
        driver.get(baseUrl + "/store");
        driver.findElementPoll(By.xpath("//a[contains(.,'Publishing Asset')]"), 5);
        assertEquals(assetName, driver.findElement(By.cssSelector("h4")).getText());
        driver.findElement(By.cssSelector("div.asset-author-category > ul > li")).click();
        assertEquals(assetName, driver.findElement(By.cssSelector("h3")).getText());
        assertEquals("to store", driver.findElement(By.cssSelector("p")).getText());
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        //delete gadget and logout
        resourceAdminServiceClient.deleteResource(resourcePath);
        driver.get(baseUrl + "/publisher/logout");
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
