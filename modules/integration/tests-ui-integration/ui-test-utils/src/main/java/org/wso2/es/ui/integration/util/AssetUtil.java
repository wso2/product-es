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

package org.wso2.es.ui.integration.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.es.integration.common.utils.ESIntegrationUITest;

/**
 * Utility operations related to assets
 */
public class AssetUtil extends BaseUITestCase {

    private static final Log log = LogFactory.getLog(BaseUITestCase.class);
    private static final int MAX_POLL_COUNT = 30;

    /**
     * Add a new asset
     *
     * @param driver      WebDriver instance
     * @param baseUrl     base url of the server
     * @param assetType   asset type
     * @param assetName   asset name
     * @param version     version
     */
    public static void addNewAsset(WebDriver driver, String baseUrl, String assetType, String assetName, String version, String category, String url, String description) {
        driver.get(baseUrl + "/publisher/assets/" + assetType + "/list");
        WebDriverWait wait = new WebDriverWait(driver, MAX_POLL_COUNT);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("Add"+assetType)));
        driver.findElement(By.id("Add"+assetType)).click();
        //driver.get(baseUrl+PUBLISHER_GADGET_CREATE_PAGE);
        driver.findElement(By.name("overview_name")).clear();
        driver.findElement(By.name("overview_name")).sendKeys(assetName);
        driver.findElement(By.name("overview_version")).clear();
        driver.findElement(By.name("overview_version")).sendKeys(version);
        if(!category.equals("")){
            new Select(driver.findElement(By.name("overview_category"))).selectByVisibleText(category);
        }
        driver.findElement(By.name("overview_url")).clear();
        driver.findElement(By.name("overview_url")).sendKeys(url);
        driver.findElement(By.name("overview_description")).clear();
        driver.findElement(By.name("overview_description")).sendKeys(description);
        driver.findElement(By.name("images_thumbnail")).sendKeys(FrameworkPathUtil.getReportLocation()
                                                                 +"/../src/test/resources/images/thumbnail.jpg");
        driver.findElement(By.name("images_banner")).sendKeys(FrameworkPathUtil.getReportLocation()
                                                              +"/../src/test/resources/images/banner.jpg");
        driver.findElement(By.id("btn-create-asset")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("Add"+assetType)));
    }

    /**
     * Edit an asset
     *
     * @param driver      WebDriver instance
     * @param baseUrl     base url of the server
     * @param assetType   asset type
     * @param assetName   asset name
     * @param description asset description
     * @return the edit response
     */
    public static String updateAsset(WebDriver driver, String baseUrl, String assetType, String assetName,
                                     String description) {
        driver.get(baseUrl + "/publisher/assets/" + assetType + "/list");
        driver.findElement(By.linkText(assetName)).click();
        driver.findElement(By.linkText("Edit")).click();
        driver.findElement(By.name("overview_description")).clear();
        driver.findElement(By.name("overview_description")).sendKeys(description);
        driver.findElement(By.id("editAssetButton")).click();
        return closeAlertAndGetItsText(driver, true);
    }

    /**
     * Change LC state
     *
     * @param driver  WebDriver instance
     * @param toState the new state
     * @param comment comment
     */
    public static void changeLCState(WebDriver driver, String toState, String comment) {
        driver.findElement(By.id(toState)).click();

        driver.findElement(By.id("commentModalText")).clear();
        driver.findElement(By.id("commentModalText")).sendKeys(comment);
        driver.findElement(By.id("commentModalSave")).click();
    }

    /**
     * Add ratings and reviews
     *
     * @param driver    WebDriver instance
     * @param review    review comment
     * @param starCount rating
     */
    public static void addRatingsAndReviews(ESWebDriver driver, String review, String starCount) {
        driver.findElement(By.id("com-body")).clear();
        driver.findElement(By.id("com-body")).sendKeys(review);
        driver.findElement(By.linkText(starCount)).click();
        driver.findElement(By.id("btn-post")).click();
        driver.switchTo().defaultContent();
    }

    /**
     * Publish a new asset to store
     *
     * @param driver    WebDriver instance
     * @param assetName asset name
     */
    public static void publishAssetToStore(WebDriver driver, String assetName) {
        WebDriverWait wait = new WebDriverWait(driver,MAX_POLL_COUNT);
        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.id("LifeCycle")));
        driver.findElement(By.id("LifeCycle")).click();
        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.id("lifecycle-comment")));
        driver.findElement(By.id("lifecycle-comment")).clear();
        driver.findElement(By.id("lifecycle-comment")).sendKeys("Promoting to review");
        driver.findElement(By.id("lcActionPromote")).click();
        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector(".alert-success")));


        driver.findElement(By.id("lifecycle-comment")).clear();
        driver.findElement(By.id("lifecycle-comment")).sendKeys("Promoting to published");
        driver.findElement(By.id("lcActionPromote")).click();
        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector(".alert-success")));
    }

//    /**
//     * Close the alert and return the text
//     *
//     * @param driver      WebDriver instance
//     * @param acceptAlert whether to accept the alert
//     * @return alert text
//     */
//    private static String closeAlertAndGetItsText(WebDriver driver, boolean acceptAlert) {
//        Alert alert = driver.switchTo().alert();
//        String alertText = alert.getText();
//        if (acceptAlert) {
//            alert.accept();
//        } else {
//            alert.dismiss();
//        }
//        return alertText;
//    }
}