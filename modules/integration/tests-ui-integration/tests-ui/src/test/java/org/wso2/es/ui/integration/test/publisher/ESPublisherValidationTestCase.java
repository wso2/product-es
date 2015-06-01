package org.wso2.es.ui.integration.test.publisher;

import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.es.ui.integration.util.BaseUITestCase;
import org.wso2.es.ui.integration.util.ESUtil;
import org.wso2.es.ui.integration.util.ESWebDriver;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class ESPublisherValidationTestCase extends BaseUITestCase {
    private static final int MAX_WAIT_TIME = 30;
    private static final String ASSET_NAME = "sampleGadget";
    private static final String ASSET_VERSION = "1.0.0";
    private static final String ASSET_URL = "http://www.test.com";
    private static final String ASSET_URL_1 = "http/www.test.com";
    private static final String ASSET_URL_EDIT = "htp:/www.test.com";
    private static final String ASSET_DESCRIPTION = "for store";
    private static final String ASSET_CATEGORY = "Google";
    private static final int MAX_POLL_COUNT = 30;


    @BeforeClass
    public void setUp() throws Exception {
        super.init();
        currentUserName = userInfo.getUserName();
        currentUserPwd = userInfo.getPassword();
        super.init();
        driver = new ESWebDriver(BrowserManager.getWebDriver());
        baseUrl = getWebAppURL();
        wait = new WebDriverWait(driver, MAX_WAIT_TIME);
        ESUtil.login(driver, baseUrl, PUBLISHER_APP, currentUserName, currentUserPwd);
    }

    @Test(groups = "wso2.es.publisher", description = "Testing adding a new asset")
    public void testAddAsset() throws Exception {
        driver.get(baseUrl + PUBLISHER_GADGET_LIST_PAGE);
        driver.findElement(By.id("Addgadget")).click();
        driver.findElement(By.name("overview_name")).clear();
        driver.findElement(By.name("overview_name")).sendKeys(ASSET_NAME);
        driver.findElement(By.name("overview_version")).clear();
        driver.findElement(By.name("overview_version")).sendKeys(ASSET_VERSION);
        new Select(driver.findElement(By.name("overview_category"))).selectByVisibleText(ASSET_CATEGORY);
        driver.findElement(By.name("overview_url")).clear();
        driver.findElement(By.name("overview_url")).sendKeys(ASSET_URL);
        driver.findElement(By.name("overview_description")).clear();
        driver.findElement(By.name("overview_description")).sendKeys(ASSET_DESCRIPTION);
        assertTrue(isElementPresent(driver, By.name("images_thumbnail")));
        driver.findElement(By.name("images_thumbnail")).sendKeys(FrameworkPathUtil.getReportLocation()
                + "/../src/test/resources/images/thumbnail.jpg");
        driver.findElement(By.name("images_banner")).sendKeys(FrameworkPathUtil.getReportLocation()
                + "/../src/test/resources/images/banner.jpg");
        driver.findElement(By.id("btn-create-asset")).click();

        driver.findElementPoll(By.linkText(ASSET_NAME), MAX_POLL_COUNT);
        //check if the created gadget is shown
        assertTrue(isElementPresent(driver, By.linkText(ASSET_NAME)), "Adding an asset failed for user:" + currentUserName);
        driver.findElement(By.linkText(ASSET_NAME)).click();
        assertEquals(currentUserName, driver.findElement(By.xpath("//div[@id='collapseOverview']/div[1]/div[2]")).getText(),
                "Incorrect provider");
        assertEquals(ASSET_NAME, driver.findElement(By.xpath("//div[@id='collapseOverview']/div[2]/div[2]")).getText(),
                "Incorrect asset name");
        assertEquals(ASSET_VERSION, driver.findElement(By.xpath("//div[@id='collapseOverview']/div[3]/div[2]")).getText(),
                "Incorrect version");
        assertEquals(ASSET_CATEGORY, driver.findElement(By.xpath("//div[@id='collapseOverview']/div[4]/div[2]")).getText());
        assertEquals(ASSET_URL, driver.findElement(By.xpath("//div[@id='collapseOverview']/div[5]/div[2]")).getText(),
                "Incorrect URL");
        assertEquals(ASSET_DESCRIPTION, driver.findElement(By.xpath("//div[@id='collapseOverview']/div[6]/div[2]")).getText(),
                "Incorrect description");
    }

    @Test(groups = "wso2.es.publisher", description = "Testing adding a new asset and validate name field", dependsOnMethods = "testAddAsset")
    public void testFieldValidate() throws Exception {

        driver.get(baseUrl + PUBLISHER_GADGET_LIST_PAGE);
        driver.findElement(By.id("Addgadget")).click();
        driver.findElement(By.name("overview_name")).clear();
        driver.findElement(By.name("overview_name")).sendKeys(ASSET_NAME);
        driver.findElement(By.name("overview_version")).clear();

        assertEquals("The name already taken", driver.findElement(By.id("overview_name-error")).getText(),
                "Could not find the text The name already taken");
    }
    @Test(groups = "wso2.es.publisher", description = "Testing editing the asset and validate url field", dependsOnMethods = "testAddAsset")
    public void testEditUrlValidate() throws Exception {
        driver.get(baseUrl + PUBLISHER_GADGET_LIST_PAGE);
        driver.findElement(By.linkText(ASSET_NAME)).click();
        driver.findElement(By.id("Edit")).click();
        driver.findElement(By.name("overview_url")).clear();
        driver.findElement(By.name("overview_url")).sendKeys(ASSET_URL_EDIT);
        driver.findElement(By.name("overview_description")).clear();

        assertEquals("please provide valid url (http://www.abc.com)", driver.findElement(By.id("overview_url-error")).getText(),
                "Could not find the text please provide valid url");
    }
    @Test(groups = "wso2.es.publisher", description = "Testing adding a new asset and validate url field")
    public void testUrlValidate() throws Exception {
        driver.get(baseUrl + PUBLISHER_GADGET_LIST_PAGE);
        driver.findElement(By.id("Addgadget")).click();
        driver.findElement(By.name("overview_name")).clear();
        driver.findElement(By.name("overview_name")).sendKeys(ASSET_NAME);
        driver.findElement(By.name("overview_version")).clear();
        driver.findElement(By.name("overview_version")).sendKeys(ASSET_VERSION);
        new Select(driver.findElement(By.name("overview_category"))).selectByVisibleText(ASSET_CATEGORY);
        driver.findElement(By.name("overview_url")).clear();
        driver.findElement(By.name("overview_url")).sendKeys(ASSET_URL_1);
        driver.findElement(By.name("overview_description")).clear();

        assertEquals("please provide valid url (http://www.abc.com)", driver.findElement(By.id("overview_url-error")).getText(),
                "Could not find the text please provide valid url");
    }
    @Test(groups = "wso2.es.publisher", description = "Testing adding a new asset and validate required field")
    public void testFieldRequiredValidate() throws Exception {
        driver.get(baseUrl + PUBLISHER_GADGET_LIST_PAGE);
        driver.findElement(By.id("Addgadget")).click();
        driver.findElement(By.name("overview_name")).clear();
        driver.findElement(By.name("overview_name")).sendKeys();
        driver.findElement(By.name("overview_version")).clear();
        driver.findElement(By.name("overview_version")).sendKeys(ASSET_VERSION);
        new Select(driver.findElement(By.name("overview_category"))).selectByVisibleText(ASSET_CATEGORY);
        driver.findElement(By.name("overview_url")).clear();
        driver.findElement(By.name("overview_url")).sendKeys(ASSET_URL);
        driver.findElement(By.name("overview_description")).clear();
        driver.findElement(By.name("overview_description")).sendKeys(ASSET_DESCRIPTION);
        assertTrue(isElementPresent(driver, By.name("images_thumbnail")));
        driver.findElement(By.name("images_thumbnail")).sendKeys(FrameworkPathUtil.getReportLocation()
                + "/../src/test/resources/images/thumbnail.jpg");
        driver.findElement(By.name("images_banner")).sendKeys(FrameworkPathUtil.getReportLocation()
                + "/../src/test/resources/images/banner.jpg");
        driver.findElement(By.id("btn-create-asset")).click();

        assertEquals("This field is required.", driver.findElement(By.id("overview_name-error")).getText(),
                "Could not find the text This field is required.");
    }
    @AfterClass
    public void tearDown() throws Exception {
        driver.get(baseUrl + PUBLISHER_LOGOUT_URL);
        driver.quit();
    }

}

