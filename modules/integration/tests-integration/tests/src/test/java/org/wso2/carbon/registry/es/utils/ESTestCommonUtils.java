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
package org.wso2.carbon.registry.es.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wink.client.ClientResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.wso2.es.integration.common.utils.GenericRestClient;

import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

@Deprecated
public class ESTestCommonUtils {
    private static final Log log = LogFactory.getLog(ESTestCommonUtils.class);
    private String cookieHeader;
    private GenericRestClient genericRestClient;
    private Map<String, String> headerMap;
    private String publisherUrl;

    public ESTestCommonUtils(GenericRestClient genericRestClient, String publisherUrl, Map<String, String> headerMap) {
        this.genericRestClient = genericRestClient;
        this.publisherUrl = publisherUrl;
        this.headerMap = headerMap;
    }

    public ClientResponse searchAssetByQuery(Map<String, String> queryParamMap) throws JSONException {
        ClientResponse clientResponse;
        JSONObject obj;
        double time1 = System.currentTimeMillis();
        int count = 0;
        do {
            clientResponse = genericRestClient.geneticRestRequestGet(publisherUrl + "/assets", queryParamMap,
                    headerMap, cookieHeader);
            Assert.assertNotNull(clientResponse, "Client Response for search rest service cannot be null");
            Assert.assertTrue((clientResponse.getStatusCode() == 200), "Wrong status code ,Expected 200 OK " +
                    clientResponse.getStatusCode());
            String response = clientResponse.getEntity(String.class);
            obj = new JSONObject(response);
            double time2 = System.currentTimeMillis();
            if ((time2 - time1) > 240000) {
                log.error("Timeout while searching for assets | time waited: " + (time2 - time1));
                break;
            }
            count = count + 1;
        } while ((Double) obj.get("count") <= 0);
        double time3 = System.currentTimeMillis();
        System.out.println("Time for query the results: " + (time3 - time1));
        System.out.println("search for the rest service...." + count);
        return clientResponse;
    }

    public ClientResponse getAssetById(String id, Map<String, String> queryParamMap) {
        return genericRestClient.geneticRestRequestGet(publisherUrl + "/assets/" + id, queryParamMap,
                headerMap, cookieHeader);
    }

    public boolean deleteAssetById(String id, Map<String, String> queryParamMap) {
        ClientResponse clientResponse = this.getAssetById(id, queryParamMap);
        if (clientResponse.getStatusCode() != 404) {
            genericRestClient.geneticRestRequestDelete(publisherUrl + "/assets/" + id,
                    MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, queryParamMap, headerMap, cookieHeader);
        }
        return true;
    }

    public ClientResponse getAssociationsById(String id, Map<String, String> queryParamMap) {
        return genericRestClient.geneticRestRequestGet(publisherUrl + "/association/" + queryParamMap.get("type") +
                        "/depends/" + id,
                queryParamMap, headerMap, cookieHeader);
    }

    public boolean deleteAllAssociationsById(String id, Map<String, String> queryParamMap) throws JSONException {
        boolean result = false;
        if (id != null) {
            ClientResponse clientResponse = this.getAssociationsById(id, queryParamMap);
            String payload = clientResponse.getEntity(String.class);
            payload = payload.substring(payload.indexOf('{'));
            JSONObject jsonObject = new JSONObject(payload);
            JSONArray assocArray = jsonObject.getJSONArray("results");
            for (int i = 0; i < assocArray.length(); i++) {
                String assocId = (String) assocArray.getJSONObject(i).get("uuid");
                String assocShortName = (String) assocArray.getJSONObject(i).get("shortName");
                Map<String, String> assocQueryMap = new HashMap<>();
                assocQueryMap.put("type", assocShortName);
                this.deleteAssetById(assocId, assocQueryMap);
                this.deleteAllAssociationsById(assocId, assocQueryMap);
            }
            result = true;
        }
        return result;
    }

    public Map<String, String> getAssociationsFromPages(String id, Map<String, String> queryParamMap) {
        String requestUrl = publisherUrl.replace("apis", "pages") + "/associations/" + queryParamMap.get("type") + "/" + id;
        System.out.println("get Association by ID: request url : " + requestUrl);
        ClientResponse clientResponse = genericRestClient.geneticRestRequestGet(requestUrl,
                queryParamMap, "text/html", headerMap, cookieHeader);
        String response = clientResponse.getEntity(String.class);
        String[] dataArray = response.split("data-uuid=");
        Map<String, String> assocMap = new HashMap<String, String>();
        for (int i = 1; i < dataArray.length; i++) {
            String mediaType = null;
            if (dataArray[i].contains("application")) {
                int startIndex = dataArray[i].indexOf("application");
                mediaType = dataArray[i].substring(startIndex, dataArray[i].indexOf("<", startIndex));
            }
            String uuid = dataArray[i].substring(1, dataArray[1].indexOf('\"', 1));
            if (mediaType != null) {
                assocMap.put(uuid, mediaType);
            }
        }
        return assocMap;
    }

    public void setCookieHeader(String cookieHeader) {
        this.cookieHeader = cookieHeader;
    }

    public String getType(String mediaType) {
        String type;
        switch (mediaType) {
            case "application/x-xsd+xml":
                type = "schema";
                break;
            case "application/vnd.wso2-service+xml":
                type = "service";
                break;
            case "application/vnd.wso2-soap-service+xml":
                type = "soapservice";
                break;
            case "application/vnd.wso2-restservice+xml":
                type = "restservice";
                break;
            case "application/policy+xml":
                type = "policy";
                break;
            case "application/vnd.wso2-endpoint+xml":
                type = "endpoint";
                break;
            case "application/vnd.wso2-notes+xml":
                type = "note";
                break;
            case "application/vnd.wso2-server+xml":
                type = "server";
                break;
            case "application/swagger+json":
                type = "swagger";
                break;
            case "application/wadl+xml":
                type = "wadl";
                break;
            case "application/wsdl+xml":
                type = "wsdl";
                break;
            default:
                type = null;
                break;
        }
        return type;
    }
}
