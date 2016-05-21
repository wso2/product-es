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
package org.wso2.carbon.registry.es.publisher.search;

import org.apache.wink.client.ClientResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.testng.annotations.*;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.es.utils.ESTestCommonUtils;
import org.wso2.carbon.registry.es.utils.ESTestBaseTest;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.es.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.es.integration.common.utils.GenericRestClient;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * This class can be used to test search functionality & advance search functionality
 */

@SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
public class RestResourceSearchAndAdvanceSearchTestCase extends ESTestBaseTest {

    private TestUserMode userMode;
    private String publisherUrl;
    private String resourcePath;
    private String assetId;
    private String type = "restservice";
    private String restServiceName;
    private String version;
    private String lcState;
    private GenericRestClient genericRestClient;
    private Map<String, String> queryParamMap;
    private Map<String, String> headerMap;

    ESTestCommonUtils crudTestCommonUtils;
    String cookieHeader;
    private ResourceAdminServiceClient resourceAdminServiceClient;

    @Factory(dataProvider = "userModeProvider")
    public RestResourceSearchAndAdvanceSearchTestCase(TestUserMode userMode) {
        this.userMode = userMode;
    }

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init(userMode);
        genericRestClient = new GenericRestClient();
        queryParamMap = new HashMap<>();
        headerMap = new HashMap<>();
        resourcePath =
                FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator + "GREG" + File.separator;
        publisherUrl = publisherContext.getContextUrls().getSecureServiceUrl().replace("services", "publisher/apis");
        crudTestCommonUtils = new ESTestCommonUtils(genericRestClient, publisherUrl, headerMap);
        resourceAdminServiceClient = new ResourceAdminServiceClient(automationContext.getContextUrls().getBackEndUrl(),
                sessionCookie);

        deteleExistingData();
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws RegistryException {
        deleteAsset(assetId, publisherUrl, cookieHeader, type, genericRestClient);
        resourceAdminServiceClient = null;

    }

    private void deteleExistingData() {
        deteleExistingResource("/_system/governance/trunk/restservices");
        deteleExistingResource("/_system/governance/apimgt/applicationdata/api-docs");
        deteleExistingResource("/_system/governance/trunk/schemas");
        deteleExistingResource("/_system/governance/trunk/wadls");
        deteleExistingResource("/_system/governance/trunk/endpoints");
    }

    private void deteleExistingResource(String path){
        try {
            resourceAdminServiceClient.deleteResource(path);
        }catch (RemoteException e) {
            log.error("Failed to Remove Resource :" + e);
        } catch (ResourceAdminServiceExceptionException e) {
            log.error("Failed to Remove Resource :" + e);
        }
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Authenticate Publisher test")
    public void authenticatePublisher() throws JSONException, XPathExpressionException {

        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/authenticate/",
                        MediaType.APPLICATION_FORM_URLENCODED,
                        MediaType.APPLICATION_JSON,
                        "username=" + automationContext.getContextTenant().getContextUser().getUserName() +
                                "&password=" + automationContext.getContextTenant().getContextUser().getPassword()
                        , queryParamMap, headerMap, null
                );
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        assertTrue((response.getStatusCode() == Response.Status.OK.getStatusCode()),
                "Wrong status code ,Expected 200 OK ,Received " +
                        response.getStatusCode()
        );
        String jSessionId = obj.getJSONObject("data").getString("sessionId");
        cookieHeader = "JSESSIONID=" + jSessionId;
        assertNotNull(jSessionId, "Invalid JSessionID received");
    }


    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Create Test Rest Service",
            dependsOnMethods = {"authenticatePublisher"})
    public void createTestRestServices() throws JSONException, IOException {

        queryParamMap.put("type", type);
        String dataBody = readFile(resourcePath + "json" + File.separator + "publisherPublishRestResource.json");
        ClientResponse response =
                genericRestClient.geneticRestRequestPost(publisherUrl + "/assets",
                        MediaType.APPLICATION_JSON,
                        MediaType.APPLICATION_JSON, dataBody
                        , queryParamMap, headerMap, cookieHeader);
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        assertTrue((response.getStatusCode() == Response.Status.CREATED.getStatusCode()),
                "Wrong status code ,Expected 201 Created ,Received " +
                        response.getStatusCode()
        );
        assetId = obj.get("id").toString();
        restServiceName = obj.get("name").toString();
        version = obj.get("version").toString();
        lcState = obj.get("lifecycleState").toString();

        assertNotNull(assetId, "Empty asset resource id available" +
                response.getEntity(String.class));
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Search Added Rest Service",
            dependsOnMethods = {"createTestRestServices"})
    public void searchAddedRestService() throws JSONException, IOException, InterruptedException {


        queryParamMap.clear();

        queryParamMap.put("q", "\"name" + "\":" + "\"" + restServiceName + "\"");

        Thread.sleep(2000);

        ClientResponse response;
        int x = 0;
        do{
            response = genericRestClient.geneticRestRequestGet
                    (publisherUrl.split("/apis")[0] + "/pages/search-results", queryParamMap, headerMap, cookieHeader);
            x++;
        }while(x < 10);

        assertTrue(response.getEntity(String.class).contains(restServiceName),
                "Response does not contain Rest service name " + restServiceName);

    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Search Non  Exist Rest Service",
            dependsOnMethods = {"searchAddedRestService"})
    public void searchNonExistRestService() throws JSONException, IOException {

        queryParamMap.clear();

        queryParamMap.put("q", "\"name" + "\":" + "\"" + "dummy" + "\"");

        ClientResponse response;
        int x = 0;
        do{
            response = genericRestClient.geneticRestRequestGet
                    (publisherUrl.split("/apis")[0] + "/pages/search-results", queryParamMap, headerMap, cookieHeader);
            x++;
        }while(x < 10);

        assertFalse(response.getEntity(String.class).contains(restServiceName),
                "Response does not contain Rest service name " + restServiceName);

    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Advance search Positive Scenario",
            dependsOnMethods = {"searchNonExistRestService"})
    public void advancedSearchAddedRestService() throws JSONException, IOException, XPathExpressionException {

        queryParamMap.clear();

        queryParamMap.put("q", "\"name" + "\":" + "\"" + restServiceName + "\"" + "," +
                "\"version" + "\":" + "\"" + version + "\"" + "," +
                "\"lcState" + "\":" + "\"" + lcState + "\"");
        try {
            Thread.sleep(5000);
            //Wait till indexing completed
        } catch (InterruptedException e) {

        }

        int x = 0;
        ClientResponse response;
        do{
            response = genericRestClient.geneticRestRequestGet
                    (publisherUrl + "/assets", queryParamMap, headerMap, cookieHeader);
            x++;
        }while(x < 10);

        assertTrue(response.getEntity(String.class).contains(restServiceName),
                "Response does not contain Rest service name " + restServiceName);
        assertTrue(response.getEntity(String.class).contains(version),
                "Response does not contain correct version " + version);
        assertTrue(response.getEntity(String.class).contains(lcState),
                "Response does not contain correct LC State " + lcState);

    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Advance search With Version Incorrect Request",
            dependsOnMethods = {"advancedSearchAddedRestService"})
    public void advancedSearchIncorrectRequest() throws JSONException, IOException, XPathExpressionException, ParseException {

        queryParamMap.clear();

        queryParamMap.put("q", "\"name" + "\":" + "\"" + restServiceName + "\"" + "," +
                "\"version" + "\":" + "\"" + "1.5.0" + "\"" + "," +
                "\"lcState" + "\":" + "\"" + lcState + "\"");

        ClientResponse response = genericRestClient.geneticRestRequestGet
                (publisherUrl + "/assets", queryParamMap, headerMap, cookieHeader);

        org.json.simple.JSONObject jsonObject = (org.json.simple.JSONObject)
                new JSONParser().parse(response.getEntity(String.class));

        assertTrue(jsonObject.get("count").toString().equals("0.0"));
        assertTrue(jsonObject.get("list").toString().equals("[]"));

    }

    @DataProvider
    private static TestUserMode[][] userModeProvider() {
        return new TestUserMode[][]{
                new TestUserMode[]{TestUserMode.SUPER_TENANT_ADMIN}
//                new TestUserMode[]{TestUserMode.TENANT_USER},
        };
    }

}
