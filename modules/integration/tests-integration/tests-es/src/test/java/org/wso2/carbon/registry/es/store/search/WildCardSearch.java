package org.wso2.carbon.registry.es.store.search;

import org.apache.wink.client.ClientResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.es.utils.ESTestBaseTest;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.es.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.es.integration.common.utils.GenericRestClient;

import javax.activation.DataHandler;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;


@SetEnvironment(executionEnvironments = { ExecutionEnvironment.ALL})
public class WildCardSearch extends ESTestBaseTest {

    private TestUserMode userMode;
    private String publisherUrl;
    private String storeUrl;
    private String resourcePath;
    private String assetId;
    private String wildcard = "xwildx";
    private String version;
    private ResourceAdminServiceClient resourceAdminServiceClient;

    private GenericRestClient genericRestClient;
    private Map<String, String> queryParamMap;
    private Map<String, String> headerMap;

    private String publisherCookieHeader;
    private String storeCookieHeader;

    @Factory(dataProvider = "userModeProvider")
    public WildCardSearch(TestUserMode userMode) {
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

        deteleExistingData();

    }

    private void deteleExistingData() {
        deleteResource("/_system/governance/trunk/restservices");
        deleteResource("/_system/governance/apimgt/applicationdata/api-docs");
        deleteResource("/_system/governance/trunk/schemas");
        deleteResource("/_system/governance/trunk/wadls");
        deleteResource("/_system/governance/trunk/endpoints");
    }

    @AfterClass(alwaysRun = true)
    private void cleanUp() throws RegistryException {
        deteleExistingData();
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

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Authenticate Publisher test")
    public void authenticatePublisher() throws JSONException, XPathExpressionException {

        ClientResponse response = genericRestClient
                .geneticRestRequestPost(publisherUrl + "/authenticate/", MediaType.APPLICATION_FORM_URLENCODED,
                        MediaType.APPLICATION_JSON,
                        "username=" + automationContext.getContextTenant().getContextUser().getUserName() +
                                "&password=" + automationContext.getContextTenant().getContextUser().getPassword(),
                        queryParamMap, headerMap, null);
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        assertTrue((response.getStatusCode() == Response.Status.OK.getStatusCode()),
                "Wrong status code ,Expected 200 OK ,Received " + response.getStatusCode());
        String jSessionId = obj.getJSONObject("data").getString("sessionId");
        publisherCookieHeader = "JSESSIONID=" + jSessionId;
        assertNotNull(jSessionId, "Invalid JSessionID received");
    }

    /* add a schema Gar from file system */
    @Test(groups = "wso2.greg", description = "Add Gars from file system",
            dependsOnMethods = {"authenticatePublisher"})
    public void testAddGarsFromFileSystem()
            throws IOException, RegistryException, ResourceAdminServiceExceptionException,
            LoginAuthenticationExceptionException, InterruptedException {
        String schemasResourceName = "schemas.gar";
        String swaggerResourceName = "swagger.gar";
        String wadlResourceName = "wadl.gar";

        String schemaGarPath =
                FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator + "GREG" + File.separator
                        + "gar" + File.separator + schemasResourceName;
        String swaggerGarPath =
                FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator + "GREG" + File.separator
                        + "gar" + File.separator + swaggerResourceName;
        String wadlGarPath =
                FrameworkPathUtil.getSystemResourceLocation() + "artifacts" + File.separator + "GREG" + File.separator
                        + "gar" + File.separator + wadlResourceName;

        resourceAdminServiceClient
                .addResource(schemaGarPath, "application/vnd.wso2.governance-archive", "adding schema gar file",
                        new DataHandler(new URL("file:///" + schemaGarPath)));
        Thread.sleep(10000);
        resourceAdminServiceClient
                .addResource(schemaGarPath, "application/vnd.wso2.governance-archive", "adding swagger gar file",
                        new DataHandler(new URL("file:///" + swaggerGarPath)));
        Thread.sleep(10000);
        resourceAdminServiceClient
                .addResource(schemaGarPath, "application/vnd.wso2.governance-archive", "adding wadl gar file",
                        new DataHandler(new URL("file:///" + wadlGarPath)));
        Thread.sleep(10000);
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Search Added assets",
            dependsOnMethods = {"testAddGarsFromFileSystem"})
    public void searchAddedAssetInPublisher() throws JSONException, IOException, InterruptedException {

        queryParamMap.clear();

        // https://localhost:9443/publisher/pages/search-results?q=%22name%22:%22xwildx%22
        queryParamMap.put("q", "\"name" + "\":" + "\"" + wildcard + "\"");

        Thread.sleep(17000);

        ClientResponse response = genericRestClient
                .geneticRestRequestGet(publisherUrl.split("/apis")[0] + "/pages/search-results", queryParamMap,
                        headerMap, publisherCookieHeader);

        assertTrue(response.getEntity(String.class).contains(wildcard),
                "Response does not contain Rest service name " + wildcard);

        String str = response.getEntity(String.class);
        int lastIndex = 0;
        int count = 0;

        while (lastIndex != -1) {

            lastIndex = str.indexOf(wildcard, lastIndex);

            if (lastIndex != -1) {
                count++;
                lastIndex += wildcard.length();
            }
        }

        // If this test fails please increase the time(10000) first
        assertEquals(count, 26, "Page should be full of assets. ");
    }

    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Authenticate Store",
            dependsOnMethods = "searchAddedAssetInPublisher")
    public void authenticateStore() throws JSONException, XPathExpressionException {

        ClientResponse response = genericRestClient
                .geneticRestRequestPost(storeUrl + "/authenticate/", MediaType.APPLICATION_FORM_URLENCODED,
                        MediaType.APPLICATION_JSON,
                        "username=" + automationContext.getContextTenant().getContextUser().getUserName() +
                                "&password=" + automationContext.getContextTenant().getContextUser().getPassword(),
                        queryParamMap, headerMap, null);
        JSONObject obj = new JSONObject(response.getEntity(String.class));
        assertTrue((response.getStatusCode() == Response.Status.OK.getStatusCode()),
                "Wrong status code ,Expected 200 OK ,Received " + response.getStatusCode());
        String jSessionId = obj.getJSONObject("data").getString("sessionId");
        storeCookieHeader = "JSESSIONID=" + jSessionId;
        assertNotNull(jSessionId, "Invalid JSessionID received");
    }


    @Test(groups = {"wso2.greg", "wso2.greg.es"}, description = "Search Added Rest Service",
            dependsOnMethods = {"authenticateStore"})
    public void searchByName() throws JSONException, IOException, InterruptedException {

        String wildcardToBeFoundInHTML = ">" + wildcard;
        queryParamMap.clear();

        // https://localhost:9443/store/pages/top-assets?q=%22name%22:%22xwildx%22
        queryParamMap.put("q", "\"name" + "\":" + "\"" + wildcard + "\"");

        Thread.sleep(25000);

        ClientResponse response = genericRestClient
                .geneticRestRequestGet(storeUrl.split("/apis")[0] + "/pages/top-assets", queryParamMap, headerMap,
                        storeCookieHeader);

        assertTrue((response.getStatusCode() == Response.Status.OK.getStatusCode()),
                "Wrong status code ,Expected 200 OK ,But Received " + response.getStatusCode());

        assertTrue(response.getEntity(String.class).contains(wildcard),
                "Response does not contain Rest service name " + wildcard);

        String str = response.getEntity(String.class);
        int lastIndex = 0;
        int count = 0;

        while (lastIndex != -1) {

            lastIndex = str.indexOf(wildcardToBeFoundInHTML, lastIndex);

            if (lastIndex != -1) {
                count++;
                lastIndex += wildcardToBeFoundInHTML.length();
            }
        }

        // If this test fails please increase the time(20000) first
        assertEquals(count, 24, "There should be 7-XSDs, 7-WADLs, 7-Rest services and 3-Swaggers. ");
    }

    @DataProvider
    private static TestUserMode[][] userModeProvider() {
        return new TestUserMode[][] { new TestUserMode[] { TestUserMode.SUPER_TENANT_ADMIN }
                //                new TestUserMode[]{TestUserMode.TENANT_USER},
        };
    }

}
