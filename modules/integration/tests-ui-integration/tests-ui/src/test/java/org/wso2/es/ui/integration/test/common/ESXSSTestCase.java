/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
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

package org.wso2.es.ui.integration.test.common;

import org.openqa.selenium.NoAlertPresentException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.extensions.selenium.BrowserManager;
import org.wso2.es.ui.integration.util.BaseUITestCase;
import org.wso2.es.ui.integration.util.ESWebDriver;

import static org.testng.Assert.assertFalse;

/**
 * Created by lasanthas on 10/21/15.
 */
public class ESXSSTestCase extends BaseUITestCase {

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = new ESWebDriver(BrowserManager.getWebDriver());
        baseUrl = getWebAppURL();
    }

    @Test(groups = "wso2.es.common", description = "Testing XSS in user registration")
    public void testRegisterUserXSSTestCase() throws Exception {

        String xssString = "XSS";

        String url = getWebAppURLHttps() + "/sso/register_new_user?" +
                "relyingParty=\"><script>alert(\"" + xssString + "\")</script>";
        driver.get(url);

        boolean xssSuccess = false;

        try {
            String result = closeAlertAndGetItsText(driver, false);

            if (result.equals(xssString))
                xssSuccess = true;

        } catch (NoAlertPresentException ex) {
        }

        assertFalse(xssSuccess, "Possible cross site scripting detected.");
    }

    @Test(groups = "wso2.es.store", description = "Test XSS in Gadgets Page")
    public void testListGadgetsXSSTestCase() throws Exception {

        String xssString = "XSS";

        String url = baseUrl + "/store/assets/gadget/list?" +
                "sortBy=overview_name&sort=}</script><script>alert('" + xssString + "')</script>";
        driver.get(url);

        boolean xssSuccess = false;

        try {
            String result = closeAlertAndGetItsText(driver, false);

            if (result.equals(xssString))
                xssSuccess = true;
        } catch (NoAlertPresentException ex) {
        }

        assertFalse(xssSuccess, "Possible cross site scripting detected");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.quit();
    }
}
