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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
 * LC state transition test
 * check state changes based on transitions
 */
public class ESPublisherLCTransitionTestCase extends BaseUITestCase {

    private static final Log LOG = LogFactory.getLog(ESPublisherLCTransitionTestCase.class);
    private static final String ASSET_VERSION = "1.2.3";
    private static final String ASSET_TYPE = "gadget";
    private static final String CREATED_TIME = "12";
    private static final String LC_COMMENT = "test";
    private static final int MAX_POLL_COUNT = 30;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private String assetName;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        assetName = "LC Test Asset";
        currentUserName = userInfo.getUserName();
        currentUserPwd = userInfo.getPassword();
        resourcePath = GADGET_REGISTRY_BASE_PATH + currentUserName + "/" + assetName + "/" + ASSET_VERSION;
        driver = new ESWebDriver(BrowserManager.getWebDriver());
        wait = new WebDriverWait(driver, MAX_POLL_COUNT);
        baseUrl = getWebAppURL();
        ESUtil.login(driver, baseUrl, PUBLISHER_APP, currentUserName, currentUserPwd);
        driver.get(baseUrl + PUBLISHER_GADGET_LIST_PAGE);
        //add new gadget
        AssetUtil.addNewAsset(driver, baseUrl, ASSET_TYPE, currentUserName, assetName, ASSET_VERSION, CREATED_TIME);
        if (isAlertPresent()) {
            String modalText = closeAlertAndGetItsText();
            LOG.warn("modal dialog appeared" + modalText);
        }
        AutomationContext automationContext = new AutomationContext(PRODUCT_GROUP_NAME, TestUserMode.SUPER_TENANT_ADMIN);
        backendURL = automationContext.getContextUrls().getBackEndUrl();
        adminUserName = automationContext.getSuperTenant().getTenantAdmin().getUserName();
        adminUserPwd = automationContext.getSuperTenant().getTenantAdmin().getPassword();
        resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL, adminUserName, adminUserPwd);
    }

    @Test(groups = "wso2.es.publisher", description = "Testing LC transition")
    public void testLc() throws Exception {
        //do a lc transition and check states
        driver.findElementPoll(By.linkText(assetName), MAX_POLL_COUNT);
        driver.findElement(By.linkText(assetName)).click();
        driver.findElement(By.linkText("Life Cycle")).click();
        driver.findElement(By.id("In-Review")).click();

        driver.findElement(By.id("commentModalText")).clear();
        driver.findElement(By.id("commentModalText")).sendKeys(LC_COMMENT);
        driver.findElement(By.id("commentModalSave")).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.xpath
                ("//table[@id='lc-history']/tbody/tr/td[2]"), "admin changed the asset from Created to In-Review"));
        assertEquals("admin changed the asset from Created to In-Review",
                driver.findElement(By.xpath("//table[@id='lc-history']/tbody/tr/td[2]")).getText());
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        //delete resources and logout
        driver.get(baseUrl + PUBLISHER_LOGOUT_URL);
        resourceAdminServiceClient.deleteResource(resourcePath);
        driver.quit();
    }

}