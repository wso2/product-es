/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.store.bamclient;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.log4j.Logger;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.agent.thrift.lb.DataPublisherHolder;
import org.wso2.carbon.databridge.agent.thrift.lb.LoadBalancingDataPublisher;
import org.wso2.carbon.databridge.agent.thrift.lb.ReceiverGroup;
import org.wso2.carbon.databridge.agent.thrift.util.DataPublisherUtil;
import org.wso2.store.bamclient.usage.ESBamPublisherUsageConstants;
import org.wso2.store.util.Configuration;
import org.wso2.store.util.ConfigurationConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

public class EventPublisher {

	private static final Logger log = Logger.getLogger(EventPublisher.class);
	static Gson gson = null;
	private static EventPublisher instance = null;

	private static String assetStatisticsDefaultStream =
			"{\"name\":" + ESBamPublisherUsageConstants.ES_STATISTICS_STREAM_NAME + "," +
			"\"version\":" + ESBamPublisherUsageConstants.ES_STATISTICS_STREAM_VERSION +
			",\"nickName\":\"asseteventsStream\",\"description\":\"assets events stream\"" +
			",\"metaData\":[{\"name\":\"clientType\",\"type\":\"STRING\"}]," +
			"\"payloadData\":[{\"name\":\"userstore\",\"type\":\"STRING\"},{\"name\":\"tenant\"," +
			"\"type\":\"STRING\"},{\"name\":\"user\",\"type\":\"STRING\"},{\"name\":\"event\"," +
			"\"type\":\"STRING\"},{\"name\":\"assetId\",\"type\":\"STRING\"},{\"name\":\"assetType\"," +
			"\"type\":\"STRING\"},{\"name\":\"description\",\"type\":\"STRING\"}]}";

	private LoadBalancingDataPublisher loadBalancingDataPublisher = null;

	private EventPublisher() throws Exception {

		try {

			InputStream inputStream =
					new FileInputStream(new File(
							System.getProperty("carbon.home") + File.separator
							+ "repository" + File.separator + "conf" + File.separator + "bam" +
							File.separator + "es-bam.xml"));

			Configuration configuration = new Configuration(inputStream);

			String trustStoreFilePath = System.getProperty("carbon.home") + File.separator
			                            + "repository" + File.separator + "resources" +
			                            File.separator + "security" +
			                            File.separator + "client-truststore.jks";

			String trustStorePwd =
					configuration.getFirstProperty(ConfigurationConstants.BAM_TRUST_STORE_PWD);
			String receiverUrls = configuration.getFirstProperty(ConfigurationConstants.BAM_HOST);
			Boolean failOver = Boolean.parseBoolean(
					configuration.getFirstProperty(ConfigurationConstants.BAM_FAILOBER));

			String userName = configuration.getFirstProperty(ConfigurationConstants.BAM_USERNAME);
			String password = configuration.getFirstProperty(ConfigurationConstants.BAM_PWD);

			if (log.isDebugEnabled()) {
				log.debug("trustStoreFilePath:" + trustStoreFilePath);
			}

			System.setProperty("javax.net.ssl.trustStore", trustStoreFilePath);
			System.setProperty("javax.net.ssl.trustStorePassword", trustStorePwd);

			if (log.isDebugEnabled()) {
				log.debug("receiverUrls" + receiverUrls);
			}

			ArrayList<ReceiverGroup> allReceiverGroups = new ArrayList<ReceiverGroup>();
			ArrayList<String> receiverGroupUrls = DataPublisherUtil.getReceiverGroups(receiverUrls);

			for (String aReceiverGroupURL : receiverGroupUrls) {
				ArrayList<DataPublisherHolder> dataPublisherHolders =
						new ArrayList<DataPublisherHolder>();
				String[] urls = aReceiverGroupURL.split(",");

				for (String aUrl : urls) {
					if (log.isDebugEnabled()) {
						log.debug("aUrl:" + aUrl);
					}
					DataPublisherHolder aNode =
							new DataPublisherHolder(null, aUrl.trim(), userName, password);
					dataPublisherHolders.add(aNode);
				}
				ReceiverGroup group = new ReceiverGroup(dataPublisherHolders, failOver);
				allReceiverGroups.add(group);
			}

			loadBalancingDataPublisher = new LoadBalancingDataPublisher(allReceiverGroups);

		} catch (FileNotFoundException fileNotFoundEx) {
			log.error("bam conf file not found", fileNotFoundEx);
			throw fileNotFoundEx;
		}

	}

	public static EventPublisher getInstance() throws Exception {
		if (instance == null) {
			synchronized (EventPublisher.class) {
				instance = new EventPublisher();
			}
		}
		return instance;
	}

	public static void main(String args[]) {
		JsonObject streamDefinition =
				(JsonObject) new JsonParser().parse(assetStatisticsDefaultStream);
	}

	public void publishEvents(String streamName, String streamVersion, String streamDefinition,
	                          String metaData, String data) throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("streamName>>" + streamName);
			log.debug("streamVersion>>" + streamVersion);
			log.debug("streamDefinition>>" + streamDefinition);
			log.debug(metaData);
			log.debug(data);
		}

		if (!loadBalancingDataPublisher.isStreamDefinitionAdded(streamName, streamVersion)) {
			loadBalancingDataPublisher
					.addStreamDefinition(streamDefinition, streamName, streamVersion);
			if (log.isDebugEnabled()) {
				log.debug("stream created:");
			}
		}

		try {
			loadBalancingDataPublisher
					.publish(streamName, streamVersion, System.currentTimeMillis(),
					         null, new Object[] { "es" }, data.split(","));
		} catch (AgentException e) {
			log.error("data publish error", e);
			throw e;
		}
	}

	public void publishAssetStatistics(String eventName, String tenantId, String userStore,
	                                   String username,
	                                   String assetUDID, String assetType, String description)
			throws Exception {

		JsonObject streamDefinition =
				(JsonObject) new JsonParser().parse(assetStatisticsDefaultStream);

		String strData =
				userStore + "," + tenantId + "," + username + "," + eventName + "," + assetUDID +
				"," + assetType + "," + description;

		publishEvents(ESBamPublisherUsageConstants.ES_STATISTICS_STREAM_NAME,
		              ESBamPublisherUsageConstants.ES_STATISTICS_STREAM_VERSION,
		              assetStatisticsDefaultStream, streamDefinition.get("metaData").toString(),
		              strData);
	}

	public void shutDownPublisher() throws RuntimeException {
		loadBalancingDataPublisher.stop();
	}

}
