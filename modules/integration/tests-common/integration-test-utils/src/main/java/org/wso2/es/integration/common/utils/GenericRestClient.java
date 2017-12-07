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
package org.wso2.es.integration.common.utils;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wink.client.ClientConfig;
import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class GenericRestClient {
    protected Log log = LogFactory.getLog(GenericRestClient.class);
    RestClient client;
    ClientResponse response;

    public GenericRestClient(boolean folllowRedirects) {
        this.setKeysForRestClient();
        ClientConfig config = new ClientConfig();
        config.followRedirects(folllowRedirects);
        client = new RestClient(config);
    }

    public GenericRestClient(ClientConfig config) {
        this.setKeysForRestClient();
        client = new RestClient(config);
    }

    public GenericRestClient() {
        this.setKeysForRestClient();
        client = new RestClient();
    }

    public static void main(String[] args) throws JSONException {
        GenericRestClient genericRestClient = new GenericRestClient();
        Map<String, String> queryParamMap = new HashMap<>();
        Map<String, String> headerMap = new HashMap<>();
        genericRestClient.setKeysForRestClient();
        JSONObject obj = new JSONObject(genericRestClient.
                geneticRestRequestPost("https://localhost:9443/publisher/apis/authenticate/",
                                       MediaType.APPLICATION_FORM_URLENCODED,
                                       MediaType.APPLICATION_JSON, "username=admin&password=admin",
                                       queryParamMap, headerMap, null).getEntity(String.class));
        obj.getJSONObject("data").getString("sessionId");
    }

    private void setKeysForRestClient() {
        String trustStore = System.getProperty("carbon.home") + File.separator + "repository" + File.separator +
                            "resources" + File.separator + "security" + File.separator + "wso2carbon.jks";
        System.setProperty("javax.net.ssl.trustStore", trustStore);
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");
    }

    /**
     * Generic rest client for basic rest POST calls over REST based on Apache Wink
     *
     * @param resourceUrl     Resource endpoint Url
     * @param contentType     ContentType of request
     * @param acceptMediaType ContentType for response
     * @param postBody        Body
     * @param queryParamMap   Map of Query parameters
     * @param headerMap       Map of headers
     * @param cookie          jSessionID in form of JSESSIONID=<ID>
     * @return
     */
    public ClientResponse geneticRestRequestPost(String resourceUrl, String contentType,
                                                 String acceptMediaType, Object postBody,
                                                 Map<String, String> queryParamMap,
                                                 Map<String, String> headerMap,
                                                 String cookie) {

        Resource resource = client.resource(resourceUrl);


        if (!(queryParamMap.size() <= 0)) {
            for (Map.Entry<String, String> queryParamEntry : queryParamMap.entrySet()) {
                resource.queryParam(queryParamEntry.getKey(), queryParamEntry.getValue());
            }
        }
        if (!(headerMap.size() <= 0)) {
            for (Map.Entry<String, String> headerEntry : headerMap.entrySet()) {
                resource.header(headerEntry.getKey(), headerEntry.getValue());
            }
        }
        if (cookie != null) {
            resource.cookie(cookie);
        }
        response = resource.contentType(contentType).
                accept(acceptMediaType).post(postBody);
        return response;
    }

    public ClientResponse geneticRestRequestDelete(String resourceUrl, String contentType,
                                                   String acceptMediaType,
                                                   Map<String, String> queryParamMap,
                                                   Map<String, String> headerMap,
                                                   String cookie) {
        Resource resource = client.resource(resourceUrl);
        if (!(queryParamMap.size() <= 0)) {
            for (Map.Entry<String, String> queryParamEntry : queryParamMap.entrySet()) {
                resource.queryParam(queryParamEntry.getKey(), queryParamEntry.getValue());
            }
        }
        if (!(headerMap.size() <= 0)) {
            for (Map.Entry<String, String> headerEntry : headerMap.entrySet()) {
                resource.header(headerEntry.getKey(), headerEntry.getValue());
            }
        }
        if (cookie != null) {
            resource.cookie(cookie);
        }

        response = resource.contentType(contentType).
                accept(acceptMediaType).delete();
        return response;
    }

    public ClientResponse geneticRestRequestDelete(String resourceUrl, String contentType,
                                                   String acceptMediaType, String data,
                                                   Map<String, String> queryParamMap,
                                                   Map<String, String> headerMap,
                                                   String cookie) {
        Resource resource = client.resource(resourceUrl);
        if (!(queryParamMap.size() <= 0)) {
            for (Map.Entry<String, String> queryParamEntry : queryParamMap.entrySet()) {
                resource.queryParam(queryParamEntry.getKey(), queryParamEntry.getValue());
            }
        }
        if (!(headerMap.size() <= 0)) {
            for (Map.Entry<String, String> headerEntry : headerMap.entrySet()) {
                resource.header(headerEntry.getKey(), headerEntry.getValue());
            }
        }
        if (cookie != null) {
            resource.cookie(cookie);
        }

        response = resource.contentType(contentType).
                accept(acceptMediaType).delete();
        return response;
    }


    public ClientResponse geneticRestRequestGet(String resourceUrl,
                                                Map<String, String> queryParamMap,
                                                Map<String, String> headerMap,
                                                String cookie) {
        Resource resource = client.resource(resourceUrl);
        MultivaluedMap<String, String> queryParamInMap = new MultivaluedHashMap<>();
        if (!(queryParamMap.size() <= 0)) {
            for (Map.Entry<String, String> queryParamEntry : queryParamMap.entrySet()) {
                queryParamInMap.add(queryParamEntry.getKey(), queryParamEntry.getValue());
            }
            resource.queryParams(queryParamInMap);
        }
        if (!(headerMap.size() <= 0)) {
            for (Map.Entry<String, String> headerEntry : headerMap.entrySet()) {
                resource.header(headerEntry.getKey(), headerEntry.getValue());
            }
        }
        if (cookie != null) {
            resource.cookie(cookie);
        }
        response = resource.get();
        return response;
    }



    public ClientResponse geneticRestRequestGet(String resourceUrl,
                                                Map<String, String> queryParamMap,
                                                String acceptMediaType,
                                                Map<String, String> headerMap,
                                                String cookie) {
        Resource resource = client.resource(resourceUrl);
        MultivaluedMap<String, String> queryParamInMap = new MultivaluedHashMap<>();
        if (!(queryParamMap.size() <= 0)) {
            for (Map.Entry<String, String> queryParamEntry : queryParamMap.entrySet()) {
                queryParamInMap.add(queryParamEntry.getKey(), queryParamEntry.getValue());
            }
            resource.queryParams(queryParamInMap);
        }
        if (!(headerMap.size() <= 0)) {
            for (Map.Entry<String, String> headerEntry : headerMap.entrySet()) {
                resource.header(headerEntry.getKey(), headerEntry.getValue());
            }
        }
        if (cookie != null) {
            resource.cookie(cookie);
        }
        response = resource.accept(acceptMediaType).get();
        return response;
    }

    /**
     * Generic rest client for basic rest PUT calls over REST based on Apache Wink
     *
     * @param resourceUrl     Resource endpoint Url
     * @param contentType     ContentType of request
     * @param acceptMediaType ContentType for response
     * @param postBody        Body
     * @param queryParamMap   Map of Query parameters
     * @param headerMap       Map of headers
     * @param cookie          jSessionID in form of JSESSIONID=<ID>
     * @return                Returns the response from the REST client
     */
    public ClientResponse genericRestRequestPut(String resourceUrl, String contentType, String acceptMediaType,
            Object postBody, Map<String, String> queryParamMap, Map<String, String> headerMap, String cookie) {

        Resource resource = client.resource(resourceUrl);

        if (!(queryParamMap.size() <= 0)) {
            for (Map.Entry<String, String> queryParamEntry : queryParamMap.entrySet()) {
                resource.queryParam(queryParamEntry.getKey(), queryParamEntry.getValue());
            }
        }
        if (!(headerMap.size() <= 0)) {
            for (Map.Entry<String, String> headerEntry : headerMap.entrySet()) {
                resource.header(headerEntry.getKey(), headerEntry.getValue());
            }
        }
        if (cookie != null) {
            resource.cookie(cookie);
        }
        response = resource.contentType(contentType).
                accept(acceptMediaType).put(postBody);
        return response;
    }

}