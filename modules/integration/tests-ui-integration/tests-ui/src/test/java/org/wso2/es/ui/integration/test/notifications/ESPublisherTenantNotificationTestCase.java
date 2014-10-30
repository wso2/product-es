package org.wso2.es.ui.integration.test.notifications;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.*;
import org.testng.annotations.*;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.es.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.es.integration.common.utils.ESIntegrationUITest;
import org.wso2.es.ui.integration.util.AssetUtil;
import org.wso2.es.ui.integration.util.ESUtil;
import org.wso2.es.ui.integration.util.ESWebDriver;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class ESPublisherTenantNotificationTestCase extends ESIntegrationUITest {
    private ESWebDriver driver;
    private String baseUrl;
    private StringBuffer verificationErrors = new StringBuffer();
    private String publisherApp = "publisher";
    private String resourceLocation;
    private String backendURL;
    private ResourceAdminServiceClient resourceAdminServiceClient;

    private String assetName;
    private TestUserMode userMode;
    private String email = "esmailsample@gmail.com";
    private String emailPwd = "esMailTest";

    private String currentUserName;
    private String currentUserPwd;
    private String adminUserName;
    private String adminUserPwd;
    private String providerName;

    private String resourcePath;

    private String LCNotificationSubject;
    private String updateNotificationSubject;

    @Factory(dataProvider = "userMode")
    public ESPublisherTenantNotificationTestCase(TestUserMode testUserMode, String assetName) {
        this.userMode = testUserMode;
        this.assetName = assetName;
    }

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init(userMode);
        driver = new ESWebDriver();
        currentUserName = userInfo.getUserName();
        currentUserPwd = userInfo.getPassword();
        baseUrl = getStorePublisherUrl();
        AutomationContext automationContext = new AutomationContext("ES", TestUserMode.TENANT_ADMIN);
        adminUserName = automationContext.getContextTenant().getTenantAdmin().getUserName();
        adminUserPwd = automationContext.getContextTenant().getTenantAdmin().getPassword();
        resourceLocation = getResourceLocation();
        backendURL = automationContext.getContextUrls().getBackEndUrl();
        resourceAdminServiceClient = new ResourceAdminServiceClient(backendURL, adminUserName, adminUserPwd);
        providerName = currentUserName.split("@")[0];
        resourcePath = "/_system/governance/gadgets/" + this.providerName + "/" + this.assetName + "/1.0.0";
        LCNotificationSubject = "[StoreLifecycleStateChange] at path: " + this.resourcePath;
        updateNotificationSubject = "[StoreAssetUpdate] at path: " + this.resourcePath;

        ESUtil.loginToAdminConsole(driver, baseUrl, adminUserName, adminUserPwd);
        ESUtil.setupUserProfile(driver, baseUrl, currentUserName, "firstName", "lastName", "esmailsample@gmail.com");
        ESUtil.login(driver, baseUrl, publisherApp, currentUserName, currentUserPwd);
        AssetUtil.addNewAsset(driver, baseUrl, "gadget", providerName, assetName, "1.0.0", "12");
    }


    @Test(groups = "wso2.es.notification", description = "Testing mails for LC state " +
            "change event")
    public void testLCNotification() throws Exception {
        driver.findElementPoll(By.linkText(assetName), 30);
        boolean hasMail = ESUtil.containsEmail(resourceLocation + File.separator + "notifications" + File
                .separator + "smtp.properties", emailPwd, email, LCNotificationSubject);
        assertTrue(hasMail, "LC Notification failed for user:" + currentUserName);
    }

    @Test(groups = "wso2.es.notification", description = "Testing mails for asset update " +
            "event", dependsOnMethods = "testLCNotification")
    public void testUpdateNotification() throws Exception {
        driver.get(baseUrl + "/publisher/asts/gadget/list");
        AssetUtil.updateAsset(driver, baseUrl, "gadget", assetName, "Test Description");
        driver.get(baseUrl + "/publisher/asts/gadget/list");
        Thread.sleep(10000);//TODO: remove sleep
        boolean hasMail = ESUtil.containsEmail(resourceLocation + File.separator + "notifications" + File
                .separator + "smtp.properties", emailPwd, email, updateNotificationSubject);
        assertTrue(hasMail, "Asset Update Notification failed for user:" + currentUserName);
    }

    @AfterClass(groups = "wso2.es.notification", alwaysRun = true)
    public void tearDown() throws Exception {
        resourceAdminServiceClient.deleteResource(resourcePath);
        ESUtil.logoutFromAdminConsole(driver, baseUrl);
        driver.get(baseUrl + "/publisher/logout");
        ESUtil.deleteAllEmail(resourceLocation + File.separator + "notifications" + File.separator + "smtp" +
                ".properties", emailPwd, email);
        driver.quit();
        String verificationErrorString = verificationErrors.toString();
        if (!"".equals(verificationErrorString)) {
            fail(verificationErrorString);
        }
    }

    @DataProvider(name = "userMode")
    private static Object[][] userModeProvider() {
        return new Object[][]{{TestUserMode.TENANT_ADMIN, "Notification asset - TenantAdmin"},
                {TestUserMode.TENANT_USER, "Notification asset - TenantUser"}};
    }

}