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

package org.wso2.es.ui.integration.test.common;

import org.openqa.selenium.By;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
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
    private static final String ASSET_NAME = "Publishing Asset";
    private static final String ASSET_VERSION = "1.0.0";
    private static final String ASSET_CREATED_TIME = "12";
    private static final String ASSET_URL = "http://test";
    private static final String ASSET_DESCRIPTION = "for store";
    private static final String LC_COMMENT = "done";
    private static final int MAX_POLL_COUNT = 30;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = new ESWebDriver(BrowserManager.getWebDriver());
        baseUrl = getWebAppURL();
        driver.get(baseUrl + PUBLISHER_GADGET_LIST_PAGE);
        providerName = userInfo.getUserName();
        resourcePath = GADGET_REGISTRY_BASE_PATH + providerName + "/" + ASSET_NAME + "/" + ASSET_VERSION;
        AutomationContext automationContext = new AutomationContext(PRODUCT_GROUP_NAME, TestUserMode.SUPER_TENANT_ADMIN);
        backendURL = automationContext.getContextUrls().getBackEndUrl();
        resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL,userInfo.getUserName(),
                userInfo.getPassword());
        ESUtil.login(driver, baseUrl, PUBLISHER_APP, userInfo.getUserName(), userInfo.getPassword());
    }

    @Test(groups = "wso2.es.common", description = "Testing Publishing an asset to store")
    public void testESPublishToStore() throws Exception {
        //Add a new gadget with info
        driver.findElement(By.linkText("Add")).click();
        driver.findElement(By.name("overview_provider")).clear();
        driver.findElement(By.name("overview_provider")).sendKeys(userInfo.getUserName());
        driver.findElement(By.name("overview_name")).clear();
        driver.findElement(By.name("overview_name")).sendKeys(ASSET_NAME);
        driver.findElement(By.name("overview_version")).clear();
        driver.findElement(By.name("overview_version")).sendKeys(ASSET_VERSION);
        driver.findElement(By.name("overview_createdtime")).clear();
        driver.findElement(By.name("overview_createdtime")).sendKeys(ASSET_CREATED_TIME);
        driver.findElement(By.name("overview_url")).clear();
        driver.findElement(By.name("overview_url")).sendKeys(ASSET_URL);
        driver.findElement(By.name("overview_description")).clear();
        driver.findElement(By.name("overview_description")).sendKeys(ASSET_DESCRIPTION);
        driver.findElement(By.id("btn-create-asset")).click();
        if (isAlertPresent()) {
            closeAlertAndGetItsText();
        }
        //publish the gadget to store
        driver.findElement(By.cssSelector("a.btn")).click();
        driver.findElementPoll(By.linkText(ASSET_NAME), MAX_POLL_COUNT);
        driver.findElement(By.linkText(ASSET_NAME)).click();
        driver.findElement(By.linkText("Life Cycle")).click();

        driver.findElement(By.id("In-Review")).click();
        driver.findElement(By.id("commentModalText")).clear();
        driver.findElement(By.id("commentModalText")).sendKeys(LC_COMMENT);
        driver.findElement(By.id("commentModalSave")).click();

        driver.get(driver.getCurrentUrl());
        driver.findElement(By.id("Published")).click();
        driver.findElement(By.id("commentModalText")).clear();
        driver.findElement(By.id("commentModalText")).sendKeys(LC_COMMENT);
        driver.findElement(By.id("commentModalSave")).click();
        //navigate to store to check the published gadget
        driver.get(baseUrl + STORE_URL);
        driver.findElementPoll(By.xpath("//a[contains(.,'" + ASSET_NAME + "')]"), 5);
        assertEquals(ASSET_NAME, driver.findElement(By.cssSelector("h4")).getText());
        driver.findElement(By.cssSelector("div.asset-author-category > ul > li")).click();
        assertEquals(ASSET_NAME, driver.findElement(By.cssSelector("h3")).getText());
        assertEquals(ASSET_DESCRIPTION, driver.findElement(By.cssSelector("p")).getText());
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        //delete gadget and logout
        resourceAdminServiceClient.deleteResource(resourcePath);
        driver.get(baseUrl + PUBLISHER_LOGOUT_URL);
        driver.quit();
    }

}
