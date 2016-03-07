/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.es.ui.integration.test.store;

import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.es.ui.integration.util.BaseUITestCase;
import org.wso2.es.ui.integration.util.ESUtil;
import org.wso2.es.ui.integration.util.ESWebDriver;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class ESStoreSocialSharingTestCase extends BaseUITestCase{
    private static final int MAX_WAIT_TIME = 30;
    public static final String LOCALHOST = "localhost:9763";
    private static String correctFacebookLink = "https://facebook.com/sharer.php?u=http://localhost:9763/store/t/carbon.super/assets/gadget/details/";
    private static String correctGplusLink = "https://plus.google.com/share?url=http://localhost:9763/store/t/carbon.super/assets/gadget/details/";
    private static String correctTwitterLink = "https://twitter.com/intent/tweet?";
    private static String correctTwitterLink2 = "/store/t/carbon.super/assets/gadget/details/";
    private static String correctDiggLink = "https://digg.com/submit?url=http://localhost:9763/store/t/carbon.super/assets/gadget/details/";
    private String gadgetId = "";

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        currentUserName = userInfo.getUserName();
        currentUserPwd = userInfo.getPassword();
        super.init();
        driver = new ESWebDriver(BrowserManager.getWebDriver());
        baseUrl = getWebAppURL();
        wait = new WebDriverWait(driver, MAX_WAIT_TIME);
        ESUtil.login(driver, baseUrl, STORE_APP, currentUserName, currentUserPwd);

        driver.get(baseUrl + "/store/assets/gadget/list");
        driver.findElement(By.xpath("//a[contains(.,'Line Plus Bar Chart')]")).click();
        String[] gadgetUrlSplit = driver.getCurrentUrl().split("/");
        gadgetId = gadgetUrlSplit[gadgetUrlSplit.length-1];
        correctGplusLink += gadgetId;
        correctFacebookLink += gadgetId;
        correctTwitterLink2 += gadgetId;
        correctDiggLink += gadgetId;
    }

    @Test(groups = "wso2.es.store", description = "Testing facebook link in share tab")
    public void testFacebookSharing() throws Exception {
        driver.get(baseUrl + "/store/assets/gadget/list");
        driver.findElement(By.xpath("//a[contains(.,'Line Plus Bar Chart')]")).click();
        assertEquals(driver.findElement(By.linkText("Share")).getText(), "Share");
        driver.findElement(By.linkText("Share")).click();
        assertEquals(driver.findElement(By.xpath("//h4[contains(.,'Social Sites')]")).getText(), "Social Sites");

        WebElement element = driver.findElement(By.xpath("//img[@src='/store/themes/store/img/facebook.png']"));
        String facebookLink = element.findElement(By.xpath("..")).getAttribute("href");
        assertTrue(matchUrl(correctFacebookLink, facebookLink), "Facebook sharing is wrong");
    }

    @Test(groups = "wso2.es.store", description = "Testing Google plus link in share tab")
    public void testGPlusSharing() throws Exception {
        driver.get(baseUrl + "/store/assets/gadget/list");
        driver.findElement(By.xpath("//a[contains(.,'Line Plus Bar Chart')]")).click();
        assertEquals(driver.findElement(By.linkText("Share")).getText(), "Share");
        driver.findElement(By.linkText("Share")).click();
        assertEquals(driver.findElement(By.xpath("//h4[contains(.,'Social Sites')]")).getText(), "Social Sites");

        WebElement element = driver.findElement(By.xpath("//img[@src='/store/themes/store/img/google.png']"));
        String gplusLink = element.findElement(By.xpath("..")).getAttribute("href");
        gplusLink = URLDecoder.decode(gplusLink, "UTF-8");
        assertTrue(matchUrl(correctGplusLink, gplusLink), "GPlus sharing is wrong");
    }

    @Test(groups = "wso2.es.store", description = "Testing Twitter link in share tab")
    public void testTwitterSharing() throws Exception {
        driver.get(baseUrl + "/store/assets/gadget/list");
        driver.findElement(By.xpath("//a[contains(.,'Line Plus Bar Chart')]")).click();
        assertEquals(driver.findElement(By.linkText("Share")).getText(), "Share");
        driver.findElement(By.linkText("Share")).click();
        assertEquals(driver.findElement(By.xpath("//h4[contains(.,'Social Sites')]")).getText(), "Social Sites");

        driver.findElement(By.xpath("//img[@src='/store/themes/store/img/twitter.png']")).click();
        switchWindow();
        String twitterLink = driver.getCurrentUrl();
        twitterLink = URLDecoder.decode(twitterLink, "UTF-8");
        System.out.println(twitterLink);
        assertTrue(twitterLink.contains(correctTwitterLink), "Twitter sharing is wrong");
        assertTrue(twitterLink.contains(correctTwitterLink2), "Twitter sharing is wrong");
    }

    public boolean matchUrl(String withLocalhostURL, String realURL) {
        int localhostIndex = withLocalhostURL.indexOf(LOCALHOST);
        String firstPart = Pattern.quote(withLocalhostURL.substring(0, localhostIndex));
        String secoundPart = Pattern.quote(withLocalhostURL.substring(localhostIndex + LOCALHOST.length()));
        Pattern p = Pattern.compile(firstPart + ".*" + secoundPart);
        Matcher m = p.matcher(realURL);
        return m.matches();
    }

    public void switchWindow() {
        String currentWindow = driver.getWindowHandle();
        for (String nextWindow : driver.getWindowHandles()) {
            if (!nextWindow.equals(currentWindow)) {
                driver.switchTo().window(nextWindow);
            }
        }
    }

    @Test(groups = "wso2.es.store", description = "Testing Digg link in share tab")
    public void testDiggSharing() throws Exception {
        driver.get(baseUrl + "/store/assets/gadget/list");
        driver.findElement(By.xpath("//a[contains(.,'Line Plus Bar Chart')]")).click();
        assertEquals(driver.findElement(By.linkText("Share")).getText(), "Share");
        driver.findElement(By.linkText("Share")).click();
        assertEquals(driver.findElement(By.xpath("//h4[contains(.,'Social Sites')]")).getText(), "Social Sites");

        driver.findElement(By.xpath("//img[@src='/store/themes/store/img/diggit.png']")).click();
        switchWindow();
        String diggLink = driver.getCurrentUrl();

        diggLink = URLDecoder.decode(diggLink, "UTF-8");
        assertTrue(matchUrl(correctDiggLink, diggLink), "Digg sharing is wrong");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.get(baseUrl + STORE_LOGOUT_URL);
        driver.quit();
    }

}
