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

import org.openqa.selenium.By;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.es.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.es.ui.integration.util.BaseUITestCase;
import org.wso2.es.ui.integration.util.ESUtil;
import org.wso2.es.ui.integration.util.ESWebDriver;

import static org.testng.Assert.assertEquals;

/**
 * Create a new asset in publisher and publish it to store
 * Check if it can be seen store side and verify details
 */
public class ESPublishToStore extends BaseUITestCase {

    private ResourceAdminServiceClient resourceAdminServiceClient;
    private String assetVersion = "1.0.0";
    private String assetCreatedTime = "12";
    private String assetUrl = "http://test";
    private String assetDescription = "for store";
    private String lcComment = "done";

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = new ESWebDriver();
        baseUrl = getWebAppURL();
        assetName = "Publishing Asset";
        driver.get(baseUrl + "/publisher/asts/gadget/list");
        providerName = userInfo.getUserName();
        resourcePath = "/_system/governance/gadgets/" + providerName + "/" + assetName + "/" +
                assetVersion;
        AutomationContext automationContext = new AutomationContext("ES",
                TestUserMode.SUPER_TENANT_ADMIN);
        backendURL = automationContext.getContextUrls().getBackEndUrl();
        resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL,
                userInfo.getUserName(), userInfo.getPassword());
        ESUtil.login(driver, baseUrl, publisherApp, userInfo.getUserName(), userInfo.getPassword());
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
        driver.findElement(By.name("overview_version")).sendKeys(assetVersion);
        driver.findElement(By.name("overview_createdtime")).clear();
        driver.findElement(By.name("overview_createdtime")).sendKeys(assetCreatedTime);
        driver.findElement(By.name("overview_url")).clear();
        driver.findElement(By.name("overview_url")).sendKeys(assetUrl);
        driver.findElement(By.name("overview_description")).clear();
        driver.findElement(By.name("overview_description")).sendKeys(assetDescription);
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
        driver.findElement(By.id("commentModalText")).sendKeys(lcComment);
        driver.findElement(By.id("commentModalSave")).click();

        driver.get(driver.getCurrentUrl());
        driver.findElement(By.id("Published")).click();
        driver.findElement(By.id("commentModalText")).clear();
        driver.findElement(By.id("commentModalText")).sendKeys(lcComment);
        driver.findElement(By.id("commentModalSave")).click();
        //navigate to store to check the published gadget
        driver.get(baseUrl + "/store");
        driver.findElementPoll(By.xpath("//a[contains(.,'" + assetName + "')]"), 5);
        assertEquals(assetName, driver.findElement(By.cssSelector("h4")).getText());
        driver.findElement(By.cssSelector("div.asset-author-category > ul > li")).click();
        assertEquals(assetName, driver.findElement(By.cssSelector("h3")).getText());
        assertEquals(assetDescription, driver.findElement(By.cssSelector("p")).getText());
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        //delete gadget and logout
        resourceAdminServiceClient.deleteResource(resourcePath);
        driver.get(baseUrl + "/publisher/logout");
        driver.quit();
    }

}
