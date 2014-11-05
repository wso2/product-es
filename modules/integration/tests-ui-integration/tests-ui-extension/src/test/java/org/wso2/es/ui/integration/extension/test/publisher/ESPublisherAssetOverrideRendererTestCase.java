/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.es.ui.integration.extension.test.publisher;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.es.ui.integration.extension.util.BaseUITestCase;
import org.wso2.es.ui.integration.extension.util.ESUtil;
import org.wso2.es.ui.integration.extension.util.ESWebDriver;

public class ESPublisherAssetOverrideRendererTestCase extends BaseUITestCase {

    @BeforeClass(alwaysRun = true)
    public void setUp() throws Exception {
        super.init();
        driver = new ESWebDriver();
        baseUrl = getWebAppURL();
        ESUtil.login(driver, baseUrl, publisherApp, userInfo.getUserName(), userInfo.getPassword());
    }

    @Test(groups = "wso2.es", description = "")
    public void testESPublisherAssetOverrideRendererTestCase() throws Exception {
        driver.get(baseUrl + "/publisher/asts/servicex/lifecycle");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws Exception {
        driver.get("/publisher/logout");
        driver.quit();
    }

}
