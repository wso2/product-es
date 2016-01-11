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

package org.wso2.es.ui.integration.extension.test.publisher;

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
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;


import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Overriding an existing partial under extension model
 */
public class ESPublisherAssetOverridePartialTestCase extends BaseUITestCase {

    private static final String VERSION = "1.0.0";
    private static final String ASSET_NAME = "Servicex 1";
    private static final String SCOPES = "test";
    private static final String TYPES = "new";
    private static final int MAX_POLL_COUNT = 30;
    private String updateUrl = null;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = new ESWebDriver(BrowserManager.getWebDriver());
        baseUrl = getWebAppURL();
        currentUserName = userInfo.getUserName();
        ESUtil.login(driver, baseUrl, PUBLISHER_APP, currentUserName, userInfo.getPassword());
        resourcePath = "/_system/governance/servicesx/" + currentUserName + "/" + ASSET_NAME + "/" + VERSION;
        AutomationContext automationContext = new AutomationContext(PRODUCT_GROUP_NAME, TestUserMode.SUPER_TENANT_ADMIN);
        adminUserName = automationContext.getSuperTenant().getTenantAdmin().getUserName();
        adminUserPwd = automationContext.getSuperTenant().getTenantAdmin().getPassword();
        backendURL = automationContext.getContextUrls().getBackEndUrl();
        resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL, adminUserName, adminUserPwd);
        driver.get(baseUrl + "/publisher/assets/servicex/list");
        driver.findElement(By.id("Addservicex")).click();
        driver.findElement(By.name("overview_name")).clear();
        driver.findElement(By.name("overview_name")).sendKeys(ASSET_NAME);
        driver.findElement(By.name("overview_version")).clear();
        driver.findElement(By.name("overview_version")).sendKeys(VERSION);
        driver.findElement(By.name("overview_scopes")).clear();
        driver.findElement(By.name("overview_scopes")).sendKeys(SCOPES);
        driver.findElement(By.name("overview_types")).clear();
        driver.findElement(By.name("overview_types")).sendKeys(TYPES);

        assertTrue(isElementPresent(driver, By.name("images_thumbnail")));
        driver.findElement(By.name("images_thumbnail")).sendKeys(FrameworkPathUtil.getReportLocation()
                                                                 +"/../src/test/resources/images/thumbnail.jpg");

        driver.findElement(By.id("btn-create-asset")).click();
        driver.findElementPoll(By.linkText(ASSET_NAME), MAX_POLL_COUNT);
        driver.findElement(By.linkText(ASSET_NAME)).click();
        updateUrl = driver.getCurrentUrl().replace("details","update");
    }

    @Test(groups = "wso2.es.extensions", description = "Test overriding a partial in extensions")
    public void testESPublisherAssetOverridePartialTestCase() throws Exception {
        driver.get(updateUrl);
        assertTrue(isElementPresent(driver, By.id("assetOverriddenListingH1")));
        assertEquals(driver.findElement(By.id("assetOverriddenListingH1")).getText(), "New Asset Update Partial of Publisher");
    }

    @Test(groups = "wso2.es.extensions", description = "Test overriding listAssetTableBody partial in extensions")
    public void testESPublisherAssetOverrideListAssetBodyPartialTestCase() throws Exception {
        driver.get(baseUrl + "/publisher/assets/servicex/list");
        assertTrue(isElementPresent(driver, By.className("assetOverriddenListingElement")));
        assertEquals(driver.findElement(By.className("assetOverriddenListingElement")).getText(), "New Asset List Partial of Publisher");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.get(baseUrl + PUBLISHER_LOGOUT_URL);
        resourceAdminServiceClient.deleteResource(resourcePath);
        driver.quit();
    }

}
