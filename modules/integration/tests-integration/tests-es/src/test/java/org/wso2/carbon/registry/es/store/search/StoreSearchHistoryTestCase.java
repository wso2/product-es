/*
*Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.es.store.search;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wink.client.ClientResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.*;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.es.utils.ESTestBaseTest;
import org.wso2.es.integration.common.utils.GenericRestClient;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * This class can be used to test search history functionality in top-assets and asset list page
 */

@SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
public class StoreSearchHistoryTestCase extends ESTestBaseTest {
    protected Log log = LogFactory.getLog(StoreSearchHistoryTestCase.class);
    private TestUserMode userMode;
    private String storeUrl;
    String jSessionId;
    String cookieHeader;
    private GenericRestClient genericRestClient;
    private Map<String, String> queryParamMap;
    private Map<String, String> headerMap;

    @Factory(dataProvider = "userModeProvider")
    public StoreSearchHistoryTestCase(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(userMode);
        genericRestClient = new GenericRestClient();
        queryParamMap = new HashMap<>();
        headerMap = new HashMap<>();
        storeUrl = storeContext.getContextUrls().getSecureServiceUrl().replace("services", "store/apis");
        setTestEnvironment();
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws RegistryException {
    }

    private void setTestEnvironment() throws Exception {
        JSONObject objSessionPublisher =
                new JSONObject(authenticate(storeUrl, genericRestClient,
                        automationContext.getSuperTenant().getTenantAdmin().getUserName(),
                        automationContext.getSuperTenant().getTenantAdmin().getPassword())
                        .getEntity(String.class));
        jSessionId = objSessionPublisher.getJSONObject("data").getString("sessionId");
        cookieHeader = "JSESSIONID=" + jSessionId;
        assertNotNull(jSessionId, "Invalid JSessionID received");
    }

    private String getSearchHistory(String type) {
        queryParamMap.clear();
        if (type != null) {
            queryParamMap.put("type", type);
        }
        ClientResponse response = genericRestClient.geneticRestRequestGet
                (storeUrl + "/search-history", queryParamMap, headerMap, cookieHeader);
        assertTrue((response.getStatusCode() == Response.Status.OK.getStatusCode()),
                "Wrong status code ,Expected 200 OK ,But Received " + response.getStatusCode());
        return response.getEntity(String.class);
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Test Search History For Asset Type")
    public void testSearchHistoryForAssetType() throws JSONException, IOException, InterruptedException {
        String serviceName = "ubers";
        String type = "restservice";
        queryParamMap.clear();
        JSONObject dataObject = new JSONObject();
        dataObject.put("type", type);
        dataObject.put("query", "\"name\":\"" + serviceName + "\"");
        ClientResponse response = genericRestClient.geneticRestRequestPost
                (storeUrl + "/search-history",
                        MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, dataObject.toString(), queryParamMap, headerMap, cookieHeader);
        assertTrue((response.getStatusCode() == Response.Status.OK.getStatusCode()),
                "Wrong status code ,Expected 200 OK ,But Received " + response.getStatusCode());
        assertTrue(getSearchHistory(type).contains(serviceName),
                "search history does not contain Rest service name " + serviceName);
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Test Search History For Cross Asset Type")
    public void testSearchHistoryForTopAssets() throws JSONException, IOException, InterruptedException {
        String serviceName = "weather";
        String serviceVersion = "1.0.0";
        queryParamMap.clear();
        JSONObject dataObject = new JSONObject();
        dataObject.put("query", "\"name" + "\":" + "\"" + serviceName + "\"," + "\"version" + "\":" + "\"" + serviceVersion + "\"");
        ClientResponse response = genericRestClient.geneticRestRequestPost
                (storeUrl + "/search-history",
                        MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, dataObject.toString(), queryParamMap, headerMap, cookieHeader);
        assertTrue((response.getStatusCode() == Response.Status.OK.getStatusCode()),
                "Wrong status code ,Expected 200 OK ,But Received " + response.getStatusCode());
        assertTrue(getSearchHistory(null).contains(serviceName),
                "search history does not contain Rest service name " + serviceName);
    }


    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Test for Search History Result Count")
    public void testSearchHistoryResultsCount() throws JSONException, IOException {
        queryParamMap.clear();
        for (int i = 0; i < 10; i++) {
            JSONObject dataObject = new JSONObject();
            dataObject.put("query", "\"version" + "\":" + "\"info" + i + "\"");
            ClientResponse response = genericRestClient.geneticRestRequestPost
                    (storeUrl + "/search-history",
                            MediaType.APPLICATION_JSON,
                            MediaType.APPLICATION_JSON, dataObject.toString(), queryParamMap, headerMap, cookieHeader);
            assertTrue((response.getStatusCode() == Response.Status.OK.getStatusCode()),
                    "Wrong status code ,Expected 200 OK ,But Received " + response.getStatusCode());
        }
        JSONArray resultArray = new JSONArray(getSearchHistory(null));
        assertEquals(resultArray.length(), 5);
    }

    @DataProvider
    private static TestUserMode[][] userModeProvider() {
        return new TestUserMode[][]{
                new TestUserMode[]{TestUserMode.SUPER_TENANT_ADMIN}
        };
    }

}
