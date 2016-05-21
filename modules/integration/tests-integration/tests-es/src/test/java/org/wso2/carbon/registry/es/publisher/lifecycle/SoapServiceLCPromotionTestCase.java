/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */
package org.wso2.carbon.registry.es.publisher.lifecycle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wink.client.ClientResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.es.utils.ESTestBaseTest;
import org.wso2.es.integration.common.utils.GenericRestClient;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.xml.xpath.XPathExpressionException;

/**
 * This class tests LC promotion steps for a Soap Service.
 */
public class SoapServiceLCPromotionTestCase extends ESTestBaseTest {

    private static final Log log = LogFactory.getLog(SoapServiceLCPromotionTestCase.class);
    private static final String LIFE_CYCLE_NAME = "ServiceLifeCycle";
    private TestUserMode userMode;
    String jSessionId;
    String assetId;
    String assetName;
    String cookieHeader;
    GenericRestClient genericRestClient;
    Map<String, String> headerMap;
    String publisherUrl;
    String resourcePath;
    private Map<String, String> queryParamMap;

    @Factory(dataProvider = "userModeProvider")
    public SoapServiceLCPromotionTestCase(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(userMode);
        genericRestClient = new GenericRestClient();
        headerMap = new HashMap<>();
        resourcePath = FrameworkPathUtil.getSystemResourceLocation()
                       + "artifacts" + File.separator + "GREG" + File.separator;
        publisherUrl = automationContext.getContextUrls()
                .getSecureServiceUrl().replace("services", "publisher/apis");
        queryParamMap = new HashMap<>();
        queryParamMap.put("type", "soapservice");
        setTestEnvironment();
    }

    @BeforeMethod(alwaysRun = true)
    public void reInitEnvironment() throws XPathExpressionException, JSONException {
        setTestEnvironment();
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Create Soap Service in Publisher")
    public void createSoapServiceAsset() throws JSONException, IOException {

        String soapTemplate = readFile(resourcePath + "json" + File.separator + "soapservice-sample.json");
        assetName = "soapserviceLC";
        String dataBody = String.format(soapTemplate, assetName, "soapserviceLC", "1.0.0",null);
        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/assets",
                                                         MediaType.APPLICATION_JSON,
                                                         MediaType.APPLICATION_JSON, dataBody
                        , queryParamMap, headerMap, cookieHeader);
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        Assert.assertTrue((response.getStatusCode() == 201),
                          "Wrong status code ,Expected 201 Created ,Received " +
                          response.getStatusCode());
        assetId = (String)obj.get("id");
        Assert.assertNotNull(assetId, "Empty asset resource id available" +
                                      response.getEntity(String.class));
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Soap Service LC check list item check",
          dependsOnMethods = {"createSoapServiceAsset"})
    public void evaluateLCStateTransition() throws IOException, JSONException {
        String state1 = "Testing";
        String state2 = "Production";
        checkLCCheckItemsOnSoapService();
        changeLCStateSoapService(state1);
        checkLCCheckItemsOnSoapService();
        changeLCStateSoapService(state2);
    }


    public void checkLCCheckItemsOnSoapService() throws JSONException, IOException {
        queryParamMap.put("lifecycle", LIFE_CYCLE_NAME);
        JSONObject LCStateobj = getLifeCycleState(assetId, "soapservice");
        JSONObject dataObj = LCStateobj.getJSONObject("data");
        JSONArray checkItems = dataObj.getJSONArray("checkItems");
        ClientResponse responseCheck;
        for (int i = 0; i < checkItems.length(); i++) {
            Assert.assertEquals(((JSONObject) checkItems.get(i)).getString("isVisible"), "true");
            responseCheck =
                    checkLifeCycleCheckItem(cookieHeader, i);
            Assert.assertTrue(responseCheck.getStatusCode() == 200);
        }


    }

    public void changeLCStateSoapService(String state) throws JSONException, IOException {
        String stateChangeMessage = " State changed successfully to " + state+"!";
        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/assets/" + assetId + "/state",
                                                         MediaType.APPLICATION_FORM_URLENCODED,
                                                         MediaType.APPLICATION_JSON,
                                                         "nextState="+state+"&comment=Completed"
                        , queryParamMap, headerMap, cookieHeader);
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        String status = obj.get("status").toString();
        Assert.assertEquals(status, stateChangeMessage);


    }

    private void setTestEnvironment() throws JSONException, XPathExpressionException {
        JSONObject objSessionPublisher =
                new JSONObject(authenticate(publisherUrl, genericRestClient,
                                            automationContext.getSuperTenant().getTenantAdmin().getUserName(),
                                            automationContext.getSuperTenant().getTenantAdmin().getPassword())
                                       .getEntity(String.class));
        jSessionId = objSessionPublisher.getJSONObject("data").getString("sessionId");
        cookieHeader = "JSESSIONID=" + jSessionId;
        Assert.assertNotNull(jSessionId, "Invalid JSessionID received");
    }

    private JSONObject getLifeCycleState(String assetId, String assetType) throws JSONException {
        ClientResponse response =
                genericRestClient.geneticRestRequestGet
                        (publisherUrl + "/asset/" + assetId + "/state"
                                , queryParamMap, headerMap, cookieHeader);
        return new JSONObject(response.getEntity(String.class));
    }

    private ClientResponse checkLifeCycleCheckItem(String managerCookieHeader, int itemId) {
        return genericRestClient.geneticRestRequestPost(publisherUrl + "/asset/" + assetId + "/update-checklist",
                                                        MediaType.APPLICATION_JSON,
                                                        MediaType.APPLICATION_JSON,
                                                        "{\"checklist\":[{\"index\":" + itemId + ",\"checked\":true}]}"
                , queryParamMap, headerMap, managerCookieHeader);
    }

    @DataProvider
    private static TestUserMode[][] userModeProvider() {
        return new TestUserMode[][]{
                new TestUserMode[]{TestUserMode.SUPER_TENANT_ADMIN}
//                new TestUserMode[]{TestUserMode.TENANT_USER},
        };
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws RegistryException, JSONException {
        Map<String, String> queryParamMap = new HashMap<>();
        deleteAssetById(publisherUrl, genericRestClient, cookieHeader, assetId, queryParamMap);
    }
}
