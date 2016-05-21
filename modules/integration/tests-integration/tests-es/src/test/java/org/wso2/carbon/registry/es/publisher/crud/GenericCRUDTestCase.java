/*
*Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.carbon.registry.es.publisher.crud;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wink.client.ClientResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.*;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.registry.es.utils.ESTestBaseTest;
import org.wso2.es.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.es.integration.common.utils.GenericRestClient;

import javax.ws.rs.core.MediaType;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

@SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
public class GenericCRUDTestCase extends ESTestBaseTest {
    private static final Log log = LogFactory.getLog(GenericCRUDTestCase.class);
    private TestUserMode userMode;
    String jSessionId;
    String assetId = null;
    String customAssetId = null;
    String assetName;
    String cookieHeader;
    GenericRestClient genericRestClient;
    Map<String, String> headerMap;
    String publisherUrl;
    String resourcePath;

    @Factory(dataProvider = "userModeProvider")
    public GenericCRUDTestCase(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(userMode);
        genericRestClient = new GenericRestClient();
        headerMap = new HashMap<>();
        resourcePath = FrameworkPathUtil.getSystemResourceLocation()
                + "artifacts" + File.separator + "GREG" + File.separator;
        publisherUrl = publisherContext.getContextUrls()
                .getSecureServiceUrl().replace("services", "publisher/apis");
        setTestEnvironment();
    }

    @BeforeMethod(alwaysRun = true)
    public void reInitEnvironment() throws XPathExpressionException, JSONException {
        JSONObject objSessionPublisher =
                new JSONObject(authenticate(publisherUrl, genericRestClient,
                        automationContext.getSuperTenant().getTenantAdmin().getUserName(),
                        automationContext.getSuperTenant().getTenantAdmin().getPassword())
                        .getEntity(String.class));
        jSessionId = objSessionPublisher.getJSONObject("data").getString("sessionId");
        cookieHeader = "JSESSIONID=" + jSessionId;
    }

    private void setTestEnvironment() throws Exception {
        assertTrue(addNewRxtConfiguration("event_lc.rxt", "event_lc.rxt"),"Adding new custom event_lc.rxt failure encountered ");
        JSONObject objSessionPublisher =
                new JSONObject(authenticate(publisherUrl, genericRestClient,
                        automationContext.getSuperTenant().getTenantAdmin().getUserName(),
                        automationContext.getSuperTenant().getTenantAdmin().getPassword())
                        .getEntity(String.class));
        jSessionId = objSessionPublisher.getJSONObject("data").getString("sessionId");
        cookieHeader = "JSESSIONID=" + jSessionId;
        //refresh the publisher landing page to deploy new rxt type
        refreshPublisherLandingPage(publisherUrl, genericRestClient, cookieHeader);
        assertNotNull(jSessionId, "Invalid JSessionID received");
    }


    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Create Defined Asset(soapservice) without required field in Publisher")
    public void createAssetWithoutRequiredField() throws JSONException, IOException {
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("type", "soapservice");
        String soapTemplate = readFile(resourcePath + "json" + File.separator + "soapservice-sample.json");
        assetName = "bbb";
        String dataBody = String.format(soapTemplate, assetName, "bbb", "1.0.0", null);
        JSONObject jsonObject = new JSONObject(dataBody);
        jsonObject.remove("overview_version");
        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/assets",
                        MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, jsonObject.toString()
                        , queryParamMap, headerMap, cookieHeader);
        JSONObject resObject = new JSONObject(response.getEntity(String.class));
        if (response.getStatusCode() == 201) {
            assetId = (String)resObject.get("id");
        }
        assertTrue((response.getStatusCode() == 500),
                "Wrong status code ,Expected 500 Internal Server Error ,Received " +
                        response.getStatusCode()
        );
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Check Option Field Values in Publisher")
    public void checkOptionFieldValues() throws IOException, JSONException {
        ClientResponse response = getAssetCreatePage("evlc");
        assertTrue((response.getStatusCode() == 200),
                "Wrong status code ,Expected 200 OK ,Received " +
                        response.getStatusCode()
        );
        String createPage = response.getEntity(String.class);
        String [] formGroup = createPage.split("<div class=\"form-group\">");
        for (String form: formGroup) {
            if (form.contains("rules_gender")) {
                assertTrue(form.contains("male"));
                assertTrue(form.contains("female"));
                break;
            }
        }
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Check Dynamically Populated values in Publisher")
    public void checkDynamicPopulatorValues() throws IOException, JSONException {
        ClientResponse response = getAssetCreatePage("evlc");
        assertTrue((response.getStatusCode() == 200),
                "Wrong status code ,Expected 200 OK ,Received " +
                        response.getStatusCode()
        );
        String createPage = response.getEntity(String.class);
        String [] formGroup = createPage.split("<div class=\"form-group\">");
        for (String form: formGroup) {
            if (form.contains("serviceLifecycle_lifecycleName")) {
                assertTrue(form.contains("None"));
                assertTrue(form.contains("ServerLifeCycle"));
                assertTrue(form.contains("EndpointLifeCycle"));
                assertTrue(form.contains("ServiceLifeCycle"));
                break;
            }
        }
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Check Default Values in Publisher")
    public void checkDefaultValues() throws IOException, JSONException {
        ClientResponse response = getAssetCreatePage("evlc");
        assertTrue((response.getStatusCode() == 200),
                "Wrong status code ,Expected 200 OK ,Received " +
                        response.getStatusCode()
        );
        String createPage = response.getEntity(String.class);
        String [] formGroup = createPage.split("<div class=\"form-group\">");
        for (String form: formGroup) {
            if (form.contains("details_venue")) {
                assertTrue(form.contains("value=\"Colombo\""));
                break;
            }
        }
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Create Custom Asset without required field in Publisher")
    public void createCustomAssetWithoutRequiredField() throws IOException, JSONException {
        Map<String, String> queryParamMap = new HashMap<>();
        queryParamMap.put("type", "evlc");
        String evlcTemplate = readFile(resourcePath + "json" + File.separator + "evlc-sample.json");
        assetName = "fff";
        String dataBody = String.format(evlcTemplate, assetName, "16/11/2015", "PG", "male", "07772223334", "none", "none");
        JSONObject jsonObject = new JSONObject(dataBody);
        jsonObject.remove("details_date");
        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/assets",
                        MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, jsonObject.toString()
                        , queryParamMap, headerMap, cookieHeader);
        JSONObject resObject = new JSONObject(response.getEntity(String.class));
        if (response.getStatusCode() == 201) {
            customAssetId = (String) resObject.get("id");
        }
        assertTrue((response.getStatusCode() == 400),
                "Wrong status code ,Expected 400 Bad Request ,Received " +
                        response.getStatusCode()
        );
    }

    private ClientResponse getAssetCreatePage(String shortName) {
        Map<String, String> queryParamMap = new HashMap<>();
        return genericRestClient.geneticRestRequestGet(publisherUrl.replace("/apis","/assets/") + shortName + "/create",
                queryParamMap, "text/html", headerMap, cookieHeader);
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws Exception {
        Map<String, String> queryParamMap = new HashMap<>();
        if (assetId != null) {
            queryParamMap.put("type", "soapservice");
            deleteAssetById(publisherUrl, genericRestClient, cookieHeader, assetId, queryParamMap);
        }
        if (customAssetId != null) {
            queryParamMap.put("type", "evlc");
            deleteAssetById(publisherUrl, genericRestClient, cookieHeader, customAssetId, queryParamMap);
        }
        assertTrue(deleteCustomRxtConfiguration("event_lc.rxt"),"Deleting of added custom event_lc.rxt encountered a failure");
    }

    @DataProvider
    private static TestUserMode[][] userModeProvider() {
        return new TestUserMode[][]{
                new TestUserMode[]{TestUserMode.SUPER_TENANT_ADMIN}
//                new TestUserMode[]{TestUserMode.TENANT_USER},
        };
    }
}
