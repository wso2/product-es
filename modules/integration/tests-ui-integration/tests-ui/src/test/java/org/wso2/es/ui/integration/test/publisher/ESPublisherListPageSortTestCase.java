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

package org.wso2.es.ui.integration.test.publisher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.testng.annotations.*;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.es.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.es.ui.integration.util.AssetUtil;
import org.wso2.es.ui.integration.util.BaseUITestCase;
import org.wso2.es.ui.integration.util.ESUtil;
import org.wso2.es.ui.integration.util.ESWebDriver;
import java.io.File;
import static org.testng.Assert.assertEquals;


public class ESPublisherListPageSortTestCase extends BaseUITestCase {
    private static final Log log = LogFactory.getLog(ESPublisherListPageSortTestCase.class);

    private TestUserMode userMode;

    private String normalUserName;
    private String normalUserPwd;

    private ResourceAdminServiceClient resourceAdminServiceClient;
    private String resourceLocation;

    private String email = "esmailsample@gmail.com";
    private String emailPwd = "esMailTest";
    private String nameSortAsset1 = "Bar Chart";
    private String nameSortAsset2 = "WSO2 Jira";
    private String version1 = "1.0.0";
    private String version2 = "2.0.0";
    private String assetType = "gadget";
    private String createdTime = "12";

    @Factory(dataProvider = "userMode")
    public ESPublisherListPageSortTestCase(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init(userMode);
        assetName = "Sort Asset";
        currentUserName = userInfo.getUserName().split("@")[0];
        currentUserPwd = userInfo.getPassword();
        driver = new ESWebDriver();
        baseUrl = getWebAppURL();
        AutomationContext automationContext = new AutomationContext("ES",
                TestUserMode.SUPER_TENANT_ADMIN);
        adminUserName = automationContext.getSuperTenant().getTenantAdmin().getUserName();
        adminUserPwd = automationContext.getSuperTenant().getTenantAdmin().getPassword();
        normalUserName = automationContext.getSuperTenant().getTenantUser("user1").getUserName()
                .split("@")[0];
        normalUserPwd = automationContext.getSuperTenant().getTenantUser("user1").getPassword();
        resourcePath = "/_system/governance/gadgets/" + normalUserName + "/" + assetName + "/" +
                version2;
        String backendURL = automationContext.getContextUrls().getBackEndUrl();
        resourceLocation = getResourceLocation();
        resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL, adminUserName,
                adminUserPwd);
        smtpPropertyLocation = resourceLocation + File.separator + "notifications" + File
                .separator + "smtp.properties";
        if (currentUserName.equals(adminUserName)) {
            ESUtil.login(driver, baseUrl, publisherApp, normalUserName, normalUserPwd);
            AssetUtil.addNewAsset(driver, baseUrl, assetType, normalUserName, assetName, version2,
                    createdTime);
            if (isAlertPresent()) {
                String alert = closeAlertAndGetItsText();
                log.warn(alert + ": modal box appeared");
            }
            driver.get(baseUrl + "/publisher/logout");
            driver.get(driver.getCurrentUrl());
        }
        ESUtil.login(driver, baseUrl, publisherApp, currentUserName, currentUserPwd);
    }

    @Test(groups = "wso2.es.publisher", description = "Test sort by name")
    public void testListPageSortByName() throws Exception {
        driver.get(baseUrl + "/publisher");
        driver.findElementPoll(By.linkText(assetName), 30);
        driver.findElement(By.partialLinkText("NAME")).click();
        assertEquals(nameSortAsset1, driver.findElement(By.xpath
                ("//tbody[@id='list-asset-table-body']/tr[1]/td[2]")).getText(),
                "Sort on name failed");
        assertEquals(nameSortAsset2, driver.findElement(By.xpath
                ("//tbody[@id='list-asset-table-body']/tr[14]/td[2]")).getText(),
                "Sort on name failed");
    }

    @Test(groups = "wso2.es.publisher", description = "Test sort by version",
            dependsOnMethods = "testListPageSortByName")
    public void testListPageSortByVersion() throws Exception {
        driver.findElement(By.linkText("VERSION")).click();
        assertEquals(version1, driver.findElement(By.xpath
                ("//tbody[@id='list-asset-table-body']/tr[1]/td[3]")).getText(),
                "Sort on version failed");
        assertEquals(version2, driver.findElement(By.xpath
                ("//tbody[@id='list-asset-table-body']/tr[14]/td[3]")).getText(),
                "Sort on version failed");
    }

    @Test(groups = "wso2.es.publisher", description = "Test sort by owner",
            dependsOnMethods = "testListPageSortByName")
    public void testListPageSortByOwner() throws Exception {
        driver.findElement(By.linkText("OWNER")).click();
        assertEquals(adminUserName, driver.findElement(By.xpath
                ("//tbody[@id='list-asset-table-body']/tr[1]/td[4]")).getText(),
                "Sort on owner failed");
        assertEquals(normalUserName, driver.findElement(By.xpath
                ("//tbody[@id='list-asset-table-body']/tr[14]/td[4]")).getText(),
                "Sort on owner failed");
    }

    @Test(groups = "wso2.es.publisher", description = "Test sort by created time",
            dependsOnMethods = "testListPageSortByName")
    public void testListPageSortByCreatedTime() throws Exception {
        driver.findElement(By.linkText("CREATED")).click();
        assertEquals(assetName, driver.findElement(By.xpath
                ("//tbody[@id='list-asset-table-body']/tr[1]/td[2]")).getText(),
                "Sort on created time failed");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        if (currentUserName.equals(normalUserName)) {
            resourceAdminServiceClient.deleteResource(resourcePath);
        }
        driver.get(baseUrl + "/publisher/logout");
        driver.get(driver.getCurrentUrl());
        ESUtil.deleteAllEmail(smtpPropertyLocation, emailPwd, email);
        driver.quit();
    }

    @DataProvider(name = "userMode")
    private static TestUserMode[][] userModeProvider() {
        return new TestUserMode[][]{
                new TestUserMode[]{TestUserMode.SUPER_TENANT_ADMIN},
                new TestUserMode[]{TestUserMode.SUPER_TENANT_USER},
        };
    }

}
