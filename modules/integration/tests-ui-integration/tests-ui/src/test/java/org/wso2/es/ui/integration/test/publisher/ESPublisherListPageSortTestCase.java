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
import org.testng.annotations.*;
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
 * ES publisher list page sort test
 * check sorting on different parameters
 */
public class ESPublisherListPageSortTestCase extends BaseUITestCase {

    private static final Log LOG = LogFactory.getLog(ESPublisherListPageSortTestCase.class);
    private TestUserMode userMode;
    private String normalUserName;
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private static final String NAME_SORT_ASSET_1 = "Bar Chart";
    private static final String NAME_SORT_ASSET_2 = "WSO2 Jira";
    private static final String VERSION_1 = "1.0.0";
    private static final String VERSION_2 = "2.0.0";
    private static final String ASSET_TYPE = "gadget";
    private static final String CREATED_TIME = "12";
    private static final String USER1 = "user1";
    private static final int MAX_POLL_COUNT = 30;
    private static final String ASSET_NAME = "Sort Asset";

    @Factory(dataProvider = "userMode")
    public ESPublisherListPageSortTestCase(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init(userMode);
        currentUserName = userInfo.getUserName().split("@")[0];
        currentUserPwd = userInfo.getPassword();
        driver = new ESWebDriver(BrowserManager.getWebDriver());
        baseUrl = getWebAppURL();
        AutomationContext automationContext = new AutomationContext(PRODUCT_GROUP_NAME, TestUserMode.SUPER_TENANT_ADMIN);
        adminUserName = automationContext.getSuperTenant().getTenantAdmin().getUserName();
        adminUserPwd = automationContext.getSuperTenant().getTenantAdmin().getPassword();
        normalUserName = automationContext.getSuperTenant().getTenantUser(USER1).getUserName().split("@")[0];
        String normalUserPwd = automationContext.getSuperTenant().getTenantUser(USER1).getPassword();
        resourcePath = GADGET_REGISTRY_BASE_PATH + normalUserName + "/" + ASSET_NAME + "/" + VERSION_2;
        String backendURL = automationContext.getContextUrls().getBackEndUrl();
        resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL, adminUserName, adminUserPwd);
        if (currentUserName.equals(adminUserName)) {
            ESUtil.login(driver, baseUrl, PUBLISHER_APP, normalUserName, normalUserPwd);
            AssetUtil.addNewAsset(driver, baseUrl, ASSET_TYPE, normalUserName, ASSET_NAME, VERSION_2, CREATED_TIME);
            if (isAlertPresent()) {
                String alert = closeAlertAndGetItsText();
                LOG.warn(alert + ": modal box appeared");
            }
            driver.get(baseUrl + PUBLISHER_LOGOUT_URL);
            driver.get(driver.getCurrentUrl());
        }
        ESUtil.login(driver, baseUrl, PUBLISHER_APP, currentUserName, currentUserPwd);
    }

    @Test(groups = "wso2.es.publisher", description = "Test sort by name")
    public void testListPageSortByName() throws Exception {
        driver.get(baseUrl + PUBLISHER_URL);
        driver.findElementPoll(By.linkText(ASSET_NAME), MAX_POLL_COUNT);
        driver.findElement(By.partialLinkText("NAME")).click();
        assertEquals(NAME_SORT_ASSET_1, driver.findElement(By.xpath("//tbody[@id='list-asset-table-body']/tr[1]/td[2]"))
                .getText(), "Sort on name failed");
        assertEquals(NAME_SORT_ASSET_2, driver.findElement(By.xpath("//tbody[@id='list-asset-table-body']/tr[14]/td[2]"))
                .getText(), "Sort on name failed");
    }

    @Test(groups = "wso2.es.publisher", description = "Test sort by version",
            dependsOnMethods = "testListPageSortByName")
    public void testListPageSortByVersion() throws Exception {
        driver.findElement(By.linkText("VERSION")).click();
        assertEquals(VERSION_1, driver.findElement(By.xpath("//tbody[@id='list-asset-table-body']/tr[1]/td[3]"))
                .getText(), "Sort on version failed");
        assertEquals(VERSION_2, driver.findElement(By.xpath("//tbody[@id='list-asset-table-body']/tr[14]/td[3]"))
                .getText(), "Sort on version failed");
    }

    @Test(groups = "wso2.es.publisher", description = "Test sort by owner",
            dependsOnMethods = "testListPageSortByName")
    public void testListPageSortByOwner() throws Exception {
        driver.findElement(By.linkText("OWNER")).click();
        assertEquals(adminUserName, driver.findElement(By.xpath("//tbody[@id='list-asset-table-body']/tr[1]/td[4]"))
                .getText(), "Sort on owner failed");
        assertEquals(normalUserName, driver.findElement(By.xpath("//tbody[@id='list-asset-table-body']/tr[14]/td[4]"))
                .getText(), "Sort on owner failed");
    }

    @Test(groups = "wso2.es.publisher", description = "Test sort by created time",
            dependsOnMethods = "testListPageSortByName")
    public void testListPageSortByCreatedTime() throws Exception {
        driver.findElement(By.linkText("CREATED")).click();
        assertEquals(ASSET_NAME, driver.findElement(By.xpath("//tbody[@id='list-asset-table-body']/tr[1]/td[2]"))
                .getText(), "Sort on created time failed");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        if (currentUserName.equals(normalUserName)) {
            resourceAdminServiceClient.deleteResource(resourcePath);
        }
        driver.get(baseUrl + PUBLISHER_LOGOUT_URL);
        driver.get(driver.getCurrentUrl());
        driver.quit();
    }

    @DataProvider(name = "userMode")
    private static TestUserMode[][] userModeProvider() {
        return new TestUserMode[][]{{TestUserMode.SUPER_TENANT_ADMIN},
                {TestUserMode.SUPER_TENANT_USER}};
    }

}
