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

import org.apache.log4j.Logger;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.agent.thrift.lb.DataPublisherHolder;
import org.wso2.carbon.databridge.agent.thrift.lb.LoadBalancingDataPublisher;
import org.wso2.carbon.databridge.agent.thrift.lb.ReceiverGroup;
import org.wso2.carbon.databridge.agent.thrift.util.DataPublisherUtil;
import org.wso2.store.util.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

public class EventPublisher {

	private static final Logger logger = Logger.getLogger(EventPublisher.class);
	private static EventPublisher instance = null;
	private LoadBalancingDataPublisher loadBalancingDataPublisher = null;

	private EventPublisher() throws Exception {

		try {

			InputStream inputStream =
					new FileInputStream(new File(System.getProperty("carbon.home") + File.separator
					                             + "repository" + File.separator + "bam" +
					                             File.separator + "bam.xml"));

			Configuration configuration = new Configuration(inputStream);


			String trustStoreFilePath = configuration.getFirstProperty("clientStorePath");
			String trustStorePwd = configuration.getFirstProperty("trustStorePassword");
			String bamHost = configuration.getFirstProperty("bamhost");

			String userName = configuration.getFirstProperty("userName");
			String password = configuration.getFirstProperty("password");

			logger.debug("trustStoreFilePath:" + trustStoreFilePath);
			logger.debug("trustStorePwd:" + trustStorePwd);
			logger.debug("trustStorePwd:" + bamHost);

			String receiverUrls = "";

			System.setProperty("javax.net.ssl.trustStore", trustStoreFilePath);
			System.setProperty("javax.net.ssl.trustStorePassword", trustStorePwd);

			for (String hostWithPort : bamHost.split(",")) {
				if (receiverUrls.length() == 0) {
					receiverUrls += "{" + "tcp://" + hostWithPort + "}";
				} else {
					receiverUrls += "," + "{" + "tcp://" + hostWithPort + "}";
				}
			}

			logger.debug("receiverUrls" + receiverUrls);

			ArrayList<ReceiverGroup> allReceiverGroups = new ArrayList<ReceiverGroup>();
			ArrayList<String> receiverGroupUrls = DataPublisherUtil.getReceiverGroups(receiverUrls);

			for (String aReceiverGroupURL : receiverGroupUrls) {
				ArrayList<DataPublisherHolder> dataPublisherHolders =
						new ArrayList<DataPublisherHolder>();
				String[] urls = aReceiverGroupURL.split(",");

				for (String aUrl : urls) {
					logger.debug("aUrl:" + aUrl);
					DataPublisherHolder aNode =
							new DataPublisherHolder(null, aUrl.trim(), userName, password);
					dataPublisherHolders.add(aNode);
				}
				ReceiverGroup group = new ReceiverGroup(dataPublisherHolders);
				allReceiverGroups.add(group);
			}

			loadBalancingDataPublisher = new LoadBalancingDataPublisher(allReceiverGroups);

		} catch (FileNotFoundException fileNotFoundEx) {
			logger.error("bam conf file not found", fileNotFoundEx);
			throw new Exception(fileNotFoundEx);

		} catch (Exception e) {
			logger.error("bam client configuration error", e);
			throw new Exception(e);
		}
	}

	public static EventPublisher getInstance() throws Exception {
		if (instance == null) {
			instance = new EventPublisher();
		}
		return instance;
	}

	public void publishEvents(String streamName, String streamVersion, String streamDefinition,
	                          String metaData, String data) throws Exception {

		logger.debug("streamName>>" + streamName);
		logger.debug("streamVersion>>" + streamVersion);
		logger.debug("streamDefinition>>" + streamDefinition);
		logger.debug(metaData);
		logger.debug(data);

		if (!loadBalancingDataPublisher.isStreamDefinitionAdded(streamName, streamVersion)) {
			loadBalancingDataPublisher
					.addStreamDefinition(streamDefinition, streamName, streamVersion);
			logger.debug("stream created:");
		}

		try {
			loadBalancingDataPublisher
					.publish(streamName, streamVersion, System.currentTimeMillis(),
					         metaData.split(","), new Object[] { "es" }, data.split(","));
		} catch (AgentException e) {
			logger.error("data publish error", e);
			throw new Exception(e);
		}
	}

}
