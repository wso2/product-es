package org.wso2.es.ui.integration.test.publisher.extensions;

import org.openqa.selenium.Alert;
import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;
import static org.testng.Assert.*;
import org.openqa.selenium.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.openqa.selenium.support.ui.Select;
import org.wso2.es.ui.integration.util.ESUtil;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.es.integration.common.utils.ESIntegrationUITest;


public class ESStoreAddedAssetTestCase extends ESIntegrationUITest {
    private WebDriver driver;
    private String baseUrl;
    private String webApp = "publisher";
    private boolean acceptNextAlert = true;
 
  @BeforeClass(alwaysRun = true)
  public void setUp() throws Exception {
        super.init();
        driver = BrowserManager.getWebDriver();
        baseUrl = getWebAppURL();
        ESUtil.login(driver, baseUrl, webApp);
  }

  @Test(groups = "wso2.es", description = "")
  public void testESStoreAddedAssetTestCase() throws Exception {
    driver.get(baseUrl + "/store/asts/gadget/list");
    driver.findElement(By.xpath("//div[@id='container-search']/div/div/div/div/a[3]/li")).click();
    assertTrue(isElementPresent(By.id("assetListingPageH1")));
 }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        ESUtil.logout(driver, baseUrl,webApp);
        driver.quit();
    }

    private boolean isElementPresent(By by) {
        try {
            driver.findElement(by);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
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
