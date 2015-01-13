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
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.es.ui.integration.util.BaseUITestCase;
import org.wso2.es.ui.integration.util.ESUtil;
import org.wso2.es.ui.integration.util.ESWebDriver;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Overriding an existing partial under extension model
 */
public class ESPublisherAssetOverridePartialTestCase extends BaseUITestCase {

    private static final String NAME_SPACE = "admin";
    private static final String VERSION = "1.0.0";
    private static final String CREATED_TIME = "12";
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
        ESUtil.login(driver, baseUrl, PUBLISHER_APP, userInfo.getUserName(), userInfo.getPassword());

        driver.get(baseUrl + "/publisher/asts/servicex/list");
        driver.findElement(By.linkText("Add")).click();
        driver.findElement(By.name("overview_provider")).clear();
        driver.findElement(By.name("overview_provider")).sendKeys(NAME_SPACE);
        driver.findElement(By.name("overview_name")).clear();
        driver.findElement(By.name("overview_name")).sendKeys(ASSET_NAME);
        driver.findElement(By.name("overview_version")).clear();
        driver.findElement(By.name("overview_version")).sendKeys(VERSION);
        driver.findElement(By.name("overview_createdtime")).clear();
        driver.findElement(By.name("overview_createdtime")).sendKeys(CREATED_TIME);
        driver.findElement(By.name("overview_scopes")).clear();
        driver.findElement(By.name("overview_scopes")).sendKeys(SCOPES);
        driver.findElement(By.name("overview_types")).clear();
        driver.findElement(By.name("overview_types")).sendKeys(TYPES);
        driver.findElement(By.id("btn-create-asset")).click();
        driver.findElementPoll(By.linkText(ASSET_NAME), MAX_POLL_COUNT);
        driver.findElement(By.linkText(ASSET_NAME)).click();
        updateUrl = driver.getCurrentUrl().replace("details","update");
    }

    @Test(groups = "wso2.es.extensions", description = "Test overriding a partial in extensions")
    public void testESPublisherAssetOverridePartialTestCase() throws Exception {
        driver.get(updateUrl);
        assertTrue(isElementPresent(By.id("assetOverriddenListingH1")));
        assertEquals(driver.findElement(By.id("assetOverriddenListingH1")).getText(), "New Asset Update Partial of Publisher");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.get(baseUrl + PUBLISHER_LOGOUT_URL);
        driver.quit();
    }

}
