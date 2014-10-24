/*

	* Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
	* WSO2 Inc. licenses this file to you under the Apache License,
	* Version 2.0 (the "License"); you may not use this file except
	* in compliance with the License.
	* You may obtain a copy of the License at
	* http://www.apache.org/licenses/LICENSE-2.0
	* Unless required by applicable law or agreed to in writing,
	* software distributed under the License is distributed on an
	* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
	* KIND, either express or implied. See the License for the
	* specific language governing permissions and limitations
	* under the License.
	*/

package org.wso2.store.client;

import com.google.code.commons.cli.annotations.CliParser;
import com.google.code.commons.cli.annotations.ParserException;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.apache.commons.cli.BasicParser;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ArtifactPublisher {

	protected static final Logger log = Logger.getLogger(ArtifactPublisher.class);

	static CloseableHttpClient httpClient = null;
	static HttpPost httppost = null;
	static HttpGet httpGet = null;
	static CloseableHttpResponse response = null;
	static SSLConnectionSocketFactory sslConnectionSocketFactory = null;
	static HttpContext httpContext = null;

	static Gson gson = null;

	static HashMap<String, List<String>> rxtFileAttributesMap = null;

	static String hostName = "";
	static String port = "";
	static String userName = "";
	static String pwd = "";
	static String sessionId = "";
	static String context = "";
	static String location;

	public static void main(String args[]) throws Exception {

		CliParser parser = new CliParser(new BasicParser());
		CliOptions cliOptions = null;

		try {
			cliOptions = parser.parse(CliOptions.class, args);

		} catch (ParserException parseException) {
			log.error("command line arguments parsing error:", parseException);
			return;
		}

		hostName = cliOptions.getHostName();
		port = cliOptions.getPort();
		userName = cliOptions.getUserName();
		pwd = cliOptions.getPwd();
		context = cliOptions.getContext();
		location = cliOptions.getLocation();

		File samplesDirectory = new File(location);

		try {
			SSLContextBuilder builder = new SSLContextBuilder();
			builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
			sslConnectionSocketFactory = new SSLConnectionSocketFactory(builder.build());

			httpContext = new BasicHttpContext();
			sessionId =
					getSession(sslConnectionSocketFactory, httpContext, hostName, port, userName,
					           pwd);

		} catch (Exception ex) {
			log.error("logging session fetching error");
			return;
		}

		String[] rxtArr = null;
		try {
			rxtArr = getRxtTypes();

		} catch (Exception ex) {
			log.error("Error in get rxt types");
			return;

		}

		rxtFileAttributesMap = new HashMap<String, List<String>>();
		for (String rxtType : rxtArr) {
			rxtFileAttributesMap.put(rxtType, getAttributesForType(rxtType, "file"));
		}
		readAssets(samplesDirectory);
	}

	private static String getSession(SSLConnectionSocketFactory sslsf, HttpContext httpContext,
	                                 String hostName, String port, String userName, String pwd)
			throws Exception {

		gson = new Gson();
		String authUrl = "https://" + hostName + ":" + port + "/" + context +
		                 Constants.PUBLISHER_AUTHORIZATION_URL + "?username=" + userName +
		                 "&password=" + pwd;

		httppost = new HttpPost(authUrl);
		httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

		String sessionId = "";
		response = null;

		try {

			response = httpClient.execute(httppost, httpContext);

		} catch (ClientProtocolException clientProtocolException) {
			log.error("client protocol exception in logging" + authUrl, clientProtocolException);
			throw new Exception(clientProtocolException.getMessage(), clientProtocolException);

		} catch (IOException ioException) {
			log.error("io error in logging" + authUrl, ioException);
			throw new Exception(ioException.getMessage(), ioException);

		}

		try {
			String responseJson = EntityUtils.toString(response.getEntity());
			Authorize authorize = gson.fromJson(responseJson, Authorize.class);

			if (authorize.getData() != null) {
				sessionId = authorize.getData().getSessionId();
				log.info("Logged:" + sessionId);

			} else {
				log.info("login failure!!!" + responseJson);

			}

		} catch (IOException ioException) {
			log.error(ioException);
			throw new Exception(ioException.getMessage(), ioException);

		} finally {
			httpClient.close();
		}

		return sessionId;
	}

	private static String[] getRxtTypes() throws Exception {

		String apiUrl = "https://" + hostName + ":" + port + "/" + context + Constants.RXT_URL;

		httpGet = new HttpGet(apiUrl);
		httpClient = HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory).build();
		response = null;

		try {
			response = httpClient.execute(httpGet, httpContext);

		} catch (ClientProtocolException clientProtocolException) {
			log.error("client protocol exception connecting to RXT API:" + apiUrl,
			          clientProtocolException);
			throw new Exception(clientProtocolException);

		} catch (IOException ioException) {
			log.error("error connecting RXT API:" + apiUrl, ioException);
			throw new Exception(ioException);
		}

		String responseJson = null;
		String[] arrRxt = null;

		try {
			responseJson = EntityUtils.toString(response.getEntity());
			arrRxt = gson.fromJson(responseJson, String[].class);

		} catch (Exception ioException) {
			ioException.printStackTrace();

		} finally {
			try {
				httpClient.close();
			} catch (IOException e) {

			}
		}
		return arrRxt;
	}

	private static List<String> getAttributesForType(String rxtType, String type) throws Exception {

		String apiUrl = "https://" + hostName + ":" + port + "/" + context +
		                Constants.RXT_ATTRIBUTES_FOR_GIVEN_TYPE + "/" + rxtType + "/" + type;
		httpGet = new HttpGet(apiUrl);

		CloseableHttpClient httpclient =
				HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory).build();

		response = null;
		gson = new Gson();

		try {

			response = httpclient.execute(httpGet, httpContext);

		} catch (ClientProtocolException clientProtocolException) {
			log.error("client protocol error in get RXT attributes for rxt type" + apiUrl,
			          clientProtocolException);
			throw new Exception(clientProtocolException);

		} catch (IOException ioException) {
			log.error("io error in get RXT attributes for rxt type" + apiUrl, ioException);
			throw new Exception(ioException);

		}

		String responseJson = null;
		String[] attrArr = null;

		try {

			responseJson = EntityUtils.toString(response.getEntity());
			attrArr = gson.fromJson(responseJson, String[].class);

		} catch (Exception ioException) {
			log.error("io error in get RXT attributes for rxt type" + apiUrl, ioException);
			throw new Exception(ioException);

		} finally {
			try {
				httpclient.close();
			} catch (IOException e) {
			}
		}
		return Arrays.asList(attrArr);

	}

	private static void readAssets(File dir) {

		Asset[] assetArr = null;
		BufferedReader br = null;

		for (final File file : dir.listFiles()) {

			if (file.isFile() &&
			    file.getName().substring(file.getName().lastIndexOf(".") + 1).equals("json")) {
				try {
					br = new BufferedReader(new FileReader(file));
					JsonParser parser = new JsonParser();
					JsonArray jarray = (JsonArray) parser.parse(br).getAsJsonObject().get("assets");

					assetArr = gson.fromJson(jarray, Asset[].class);
					uploadAssets(assetArr, dir);

				} catch (Exception ex) {
					log.error("file not completely uploaded" + file.getName());
				}
			}

			if (file.list() != null && file.list().length > 0) {
				readAssets(file);
			}
		}

	}

	private static void uploadAssets(Asset[] assetArr, File dir) {

		HashMap<String, String> attrMap = null;
		MultipartEntityBuilder multiPartBuilder = null;
		List<String> fileAttributes = null;

		File imageFile = null;
		String responseJson = null;
		StringBuffer publisherUrlBuff = null;

		for (Asset asset : assetArr) {
			publisherUrlBuff = new StringBuffer();
			if (asset.getId() != null) {
				publisherUrlBuff.append("https://" + hostName + ":" + port + "/" + context +
				                        Constants.PUBLISHER_URL + "/" + asset.getId() + "?type=" +
				                        asset.getType());
			} else {
				publisherUrlBuff.append("https://" + hostName + ":" + port + "/" + context +
				                        Constants.PUBLISHER_URL + "?type=" + asset.getType());
			}
			multiPartBuilder = MultipartEntityBuilder.create();
			multiPartBuilder.addTextBody("sessionId", sessionId);
			multiPartBuilder.addTextBody("asset", gson.toJson(asset));

			attrMap = asset.getAttributes();

			for (String attrKey : attrMap.keySet()) {
				httpClient = HttpClients.custom().setSSLSocketFactory(sslConnectionSocketFactory)
				                        .build();
				httppost = new HttpPost(publisherUrlBuff.toString());
				fileAttributes = rxtFileAttributesMap.get(asset.getType());

				if (fileAttributes != null && fileAttributes.contains(attrKey)) {
					imageFile = new File(
							dir + File.separator + Constants.RESOURCE_DIR_NAME + File.separator +
							attrMap.get(attrKey));
					multiPartBuilder.addBinaryBody(attrKey, imageFile);
					httppost.setEntity(multiPartBuilder.build());
				}
			}
			try {
				response = httpClient.execute(httppost, httpContext);
				responseJson = EntityUtils.toString(response.getEntity());
				log.info(responseJson.toString());
			} catch (Exception ex) {
				log.error(asset);
				log.error("error in asset Upload", ex);
			} finally {
				try {
					httpClient.close();
				} catch (IOException e) {
				}
			}

		}
	}

}
