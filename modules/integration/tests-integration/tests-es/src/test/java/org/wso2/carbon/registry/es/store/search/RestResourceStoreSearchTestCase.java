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
package org.wso2.carbon.registry.es.store.search;

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
import org.wso2.carbon.registry.core.exceptions.RegistryException;
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

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * This class can be used to test search functionality & advance search functionality
 */

@SetEnvironment(executionEnvironments = {ExecutionEnvironment.ALL})
public class RestResourceStoreSearchTestCase extends ESTestBaseTest {

    protected Log log = LogFactory.getLog(RestResourceStoreSearchTestCase.class);

    private TestUserMode userMode;
    private String publisherUrl;
    private String storeUrl;
    private String resourcePath;
    private String assetId;
    private String type = "restservice";
    private String restServiceName;
    private String version;

    private GenericRestClient genericRestClient;
    private Map<String, String> queryParamMap;
    private Map<String, String> headerMap;


    private String publisherCookieHeader;
    private String storeCookieHeader;

    private ResourceAdminServiceClient resourceAdminServiceClient;

    @Factory(dataProvider = "userModeProvider")
    public RestResourceStoreSearchTestCase(TestUserMode userMode) {
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
        storeUrl = storeContext.getContextUrls().getSecureServiceUrl().replace("services", "store/apis");
        resourceAdminServiceClient = new ResourceAdminServiceClient(automationContext.getContextUrls().getBackEndUrl(),
                sessionCookie);

        deleteResource("/_system/governance/trunk/restservices");
    }

    private void deleteResource(String path){
        try {
            resourceAdminServiceClient.deleteResource(path);
        }catch (RemoteException e) {
            log.error("Failed to Remove Resource :" + e);
        } catch (ResourceAdminServiceExceptionException e) {
            log.error("Failed to Remove Resource :" + e);
        }
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() throws RegistryException {
        deleteAsset(assetId, publisherUrl, publisherCookieHeader, type, genericRestClient);
        resourceAdminServiceClient = null;
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
        publisherCookieHeader = "JSESSIONID=" + jSessionId;
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
                        , queryParamMap, headerMap, publisherCookieHeader);
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        assertTrue((response.getStatusCode() == Response.Status.CREATED.getStatusCode()),
                "Wrong status code ,Expected 201 Created ,Received " +
                        response.getStatusCode()
        );
        assetId = obj.get("id").toString();
        restServiceName = obj.get("name").toString();
        version = obj.get("version").toString();


        assertNotNull(assetId, "Empty asset resource id available" +
                response.getEntity(String.class));
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Authenticate Store",
            dependsOnMethods = "createTestRestServices")
    public void authenticateStore() throws JSONException, XPathExpressionException {

        ClientResponse response =
                genericRestClient.geneticRestRequestPost(storeUrl + "/authenticate/",
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
        storeCookieHeader = "JSESSIONID=" + jSessionId;
        assertNotNull(jSessionId, "Invalid JSessionID received");
    }


    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Search Added Rest Service",
            dependsOnMethods = {"authenticateStore"})
    public void searchByName() throws JSONException, IOException, InterruptedException {


        queryParamMap.clear();
        // https://localhost:9443/store/assets/restservice/list?q=%22name%22:%22Tip%22
        // https://localhost:9443/store/assets/restservice/list?q=%22version%22:%221.2.2%22
        queryParamMap.put("q", "\"name" + "\":" + "\"" + restServiceName + "\"");

        Thread.sleep(15000);

        ClientResponse response;
        int x = 0;
        do{
            response = genericRestClient.geneticRestRequestGet
                    (storeUrl.split("/apis")[0] + "/assets/restservice/list", queryParamMap, headerMap, storeCookieHeader);
            x++;
        }while(x < 10);

        assertTrue((response.getStatusCode() == Response.Status.OK.getStatusCode()),
                "Wrong status code ,Expected 200 OK ,But Received " + response.getStatusCode());

        assertTrue(response.getEntity(String.class).contains(restServiceName),
                "Response does not contain Rest service name " + restServiceName);

    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Search Non  Exist Rest Service",
            dependsOnMethods = {"searchByName"})
    public void searchByVersion() throws JSONException, IOException {

        queryParamMap.clear();

        queryParamMap.put("q", "\"version" + "\":" + "\"" + version + "\"");

        ClientResponse response;
        int x = 0;
        do{
            response = genericRestClient.geneticRestRequestGet(storeUrl.split("/apis")[0] + "/assets/restservice/list", queryParamMap, headerMap, storeCookieHeader);
            x++;
        }while(x < 10);

        log.info("Store search result : "+ response.getEntity(String.class));

        assertTrue(response.getEntity(String.class).contains(restServiceName),
                "Response does not contain Rest service name " + restServiceName);

        assertTrue(response.getEntity(String.class).contains(version),
                "Response does not contain Rest service version " + version);

    }


    @DataProvider
    private static TestUserMode[][] userModeProvider() {
        return new TestUserMode[][]{
                new TestUserMode[]{TestUserMode.SUPER_TENANT_ADMIN}
//                new TestUserMode[]{TestUserMode.TENANT_USER},
        };
    }

}
