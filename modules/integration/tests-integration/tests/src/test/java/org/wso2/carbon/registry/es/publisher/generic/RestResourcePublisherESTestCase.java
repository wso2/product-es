/*
*Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.es.publisher.generic;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wink.client.ClientResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.es.utils.ESTestBaseTest;
import org.wso2.es.integration.common.utils.GenericRestClient;

import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
public class RestResourcePublisherESTestCase extends ESTestBaseTest {
    private static final Log log = LogFactory.getLog(RestResourcePublisherESTestCase.class);
    private TestUserMode userMode;
    String jSessionId;
    String assetId;
    String cookieHeader;
    GenericRestClient genericRestClient;
    Map<String, String> queryParamMap;
    Map<String, String> headerMap;
    String publisherUrl;
    String resourcePath;

    @Factory(dataProvider = "userModeProvider")
    public RestResourcePublisherESTestCase(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(userMode);
        genericRestClient = new GenericRestClient();
        queryParamMap = new HashMap<>();
        headerMap = new HashMap<>();
        resourcePath = FrameworkPathUtil.getSystemResourceLocation()
                       + "artifacts" + File.separator + "GREG" + File.separator;
        publisherUrl = publisherContext.getContextUrls()
                .getSecureServiceUrl().replace("services", "publisher/apis");
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Authenticate Publisher test")
    public void authenticatePublisher() throws JSONException {
        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/authenticate/",
                                                         MediaType.APPLICATION_FORM_URLENCODED,
                                                         MediaType.APPLICATION_JSON,
                                                         "username=admin&password=admin"
                        , queryParamMap, headerMap, null);
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        Assert.assertTrue((response.getStatusCode() == 200),
                          "Wrong status code ,Expected 200 OK ,Received " +
                          response.getStatusCode());
        jSessionId = obj.getJSONObject("data").getString("sessionId");
        cookieHeader = "JSESSIONID=" + jSessionId;
        Assert.assertNotNull(jSessionId, "Invalid JSessionID received");
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Authenticate Publisher test",
            dependsOnMethods = {"authenticatePublisher"})
    public void createRestServiceAsset() throws JSONException, IOException {
        queryParamMap.put("type", "restservice");
        String dataBody = readFile(resourcePath + "json" + File.separator + "publisherPublishRestResource.json");
        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/assets",
                                                         MediaType.APPLICATION_JSON,
                                                         MediaType.APPLICATION_JSON, dataBody
                        , queryParamMap, headerMap, cookieHeader);
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        Assert.assertTrue((response.getStatusCode() == 201),
                          "Wrong status code ,Expected 201 Created ,Received " +
                          response.getStatusCode());
        assetId = obj.get("id").toString();
        Assert.assertNotNull(assetId, "Empty asset resource id available" +
                                      response.getEntity(String.class));
        waitForIndexing();
        Assert.assertTrue(
                this.getAssetsById(genericRestClient, publisherUrl, assetId, cookieHeader, "restservice")
                        .getEntity(String.class).contains(assetId));

    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Authenticate Publisher test",
            dependsOnMethods = {"authenticatePublisher", "createRestServiceAsset"})
    public void updateRestServiceAsset() throws JSONException, IOException {
        queryParamMap.put("type", "restservice");
        String dataBody = readFile(resourcePath + "json" + File.separator + "PublisherRestResourceUpdate.json");
        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/assets/" + assetId,
                                                         MediaType.APPLICATION_JSON,
                                                         MediaType.APPLICATION_JSON, dataBody
                        , queryParamMap, headerMap, cookieHeader);
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        Assert.assertTrue((response.getStatusCode() == 202),
                          "Wrong status code ,Expected 202 Created ,Received " +
                          response.getStatusCode());
        Assert.assertTrue(obj.getJSONObject("attributes").get("overview_context")
                                  .equals("/changed/Context"));
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Delete Publisher test",
            dependsOnMethods = {"authenticatePublisher", "createRestServiceAsset",
                                "updateRestServiceAsset"}, alwaysRun = true)
    public void deleteRestServiceAsset() throws JSONException {
        queryParamMap.put("type", "restservice");
        genericRestClient.geneticRestRequestDelete(publisherUrl + "/assets/" + assetId,
                                                   MediaType.APPLICATION_JSON,
                                                   MediaType.APPLICATION_JSON
                , queryParamMap, headerMap, cookieHeader);

        Assert.assertTrue(this.getAssetsById(genericRestClient, publisherUrl,
                                             assetId, cookieHeader, "restservice")
                                  .getStatusCode() == 404);
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws RegistryException {

        if (this.getAssetsById(genericRestClient, publisherUrl,
                               assetId, cookieHeader, "restservice")
                    .getStatusCode() != 404) {
            queryParamMap.put("type", "restservice");
            genericRestClient.geneticRestRequestDelete(publisherUrl + "/assets/" + assetId,
                                                       MediaType.APPLICATION_JSON,
                                                       MediaType.APPLICATION_JSON
                    , queryParamMap, headerMap, cookieHeader);
        }
    }

    @DataProvider
    private static TestUserMode[][] userModeProvider() {
        return new TestUserMode[][]{
                new TestUserMode[]{TestUserMode.SUPER_TENANT_ADMIN}
//                new TestUserMode[]{TestUserMode.TENANT_USER},
        };
    }

    private void waitForIndexing(){
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            //Ignore exception due to indexing wait
        }
    }
}
