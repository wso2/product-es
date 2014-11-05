/*
 * Copyright (c) 2014, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.es.integration.common.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.*;

public class AssetsRESTClient extends ESIntegrationTest {
    String username = "admin";
    String password = "admin";
    URLConnection urlConn;
    DataOutputStream printout;
    DataInputStream input;
    String baseURL = "https://localhost:9443";
    protected static final Logger log = Logger.getLogger(AssetsRESTClient.class);
    StringBuilder response = null;
    String endpoint;
    URL endpointUrl;
    JsonParser parser;
    JsonElement elem;

    /**
     * This methods make a call to ES-Publisher REST API and obtain a sessionID
     * @return
     */
    private String getSessionID() {
        String sessionID = null;
        try {
            endpoint = baseURL + "/publisher/apis/authenticate?"; //authenticate endpoint
            endpointUrl = new URL(endpoint);
            urlConn = endpointUrl.openConnection();
            urlConn.setDoInput(true);
            urlConn.setDoOutput(true);
            urlConn.setUseCaches(false);
            // Specify the content type.
            urlConn.setRequestProperty
                    ("Content-Type", "application/x-www-form-urlencoded");
            // Send POST output.
            printout = new DataOutputStream(urlConn.getOutputStream());
            String content =
                    "username=" + URLEncoder.encode(username) +
                            "&password=" + URLEncoder.encode(password);
            printout.writeBytes(content);
            printout.flush();
            printout.close();
            // Get response data.
            input = new DataInputStream(urlConn.getInputStream());
            String str;
            response = new StringBuilder();
            while (null != ((str = input.readLine()))) {
                response.append(str);
            }
            parser = new JsonParser();
            elem = parser.parse(response.toString());
            sessionID = elem.getAsJsonObject().getAsJsonObject("data").get("sessionId").toString();
            input.close();
        } catch (MalformedURLException e) {
            log.error(e);
        } catch (IOException e) {
            log.error(e);
        } catch (Exception e) {
            log.error(e);
        }
        return sessionID;
    }

    private JsonArray getData(String sessionId) {
        try {
            endpoint = baseURL + "/publisher/apis/assets?type=gadget";//endpoint list assets
            endpointUrl = new URL(endpoint);
            urlConn = endpointUrl.openConnection();
            urlConn.setRequestProperty("Cookie", "JSESSIONID=" + sessionId + ";");//send
            // SessionId Cookie
            urlConn.connect();
            //GET response data
            input = new DataInputStream(urlConn.getInputStream());
            response = new StringBuilder();
            String str;
            while (null != ((str = input.readLine()))) {
                response.append(str);
            }
            input.close();
            parser = new JsonParser();
            elem = parser.parse(response.toString());
            JsonArray assets = elem.getAsJsonObject().getAsJsonArray("data");
            // parse response to a JasonArray
            return assets;

        } catch (MalformedURLException e) {
            log.error(e);
        } catch (IOException e) {
            log.error(e);
        } catch (Exception e) {
            log.error(e);
        }
        return null;
    }

    public boolean isIndexCompleted() {
        String sessionId = getSessionID();
        JsonArray assets = getData(sessionId);
        if (assets.size() > 0) {
            return true;
        }
        return false;
    }
}
