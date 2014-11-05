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

package org.wso2.store.util;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class Configuration {

	private static Logger logger = Logger.getLogger(Configuration.class);
	private Map<String, List<String>> configuration = new HashMap<String, List<String>>();

	public Configuration(InputStream xmlInputStream) throws Exception {

		try {
			DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = dBuilder.parse(xmlInputStream);

			Stack<String> nameStack = new Stack<String>();

			if (doc.hasChildNodes()) {
				NodeList nodeList = doc.getChildNodes();
				for (int count = 0; count < nodeList.getLength(); count++) {
					Node tempNode = nodeList.item(count);
					// make sure it's element node.
					if (tempNode.getNodeType() == Node.ELEMENT_NODE) {
						NodeList chiledNodeList = tempNode.getChildNodes();
						readChildElements(chiledNodeList, nameStack);
					}
				}
			}

		} catch (ParserConfigurationException e) {
			logger.fatal("Problem in parsing the configuration file ", e);
			throw new Exception(e);
		} catch (SAXException e) {
			logger.fatal("Problem in parsing the configuration file ", e);
			throw new Exception(e);
		} catch (IOException e) {
			logger.fatal("Problem in parsing the configuration file ", e);
			throw new Exception(e);
		}

	}

	private void readChildElements(NodeList nodeList, Stack<String> nameStack) {

		for (int count = 0; count < nodeList.getLength(); count++) {
			Node tempNode = nodeList.item(count);
			if (tempNode.getNodeType() == Node.ELEMENT_NODE) {
				nameStack.push(tempNode.getNodeName());
				if (elementHasText(tempNode)) {
					String key = getKey(nameStack);
					String value = tempNode.getFirstChild().getNodeValue();
					addToConfiguration(key, value);
				}
				readChildElements(tempNode.getChildNodes(), nameStack);
				nameStack.pop();
			}
		}
	}

	private boolean elementHasText(Node element) {
		String text = element.getFirstChild().getNodeValue();
		return text != null && text.trim().length() != 0;
	}

	private String getKey(Stack<String> nameStack) {
		StringBuffer key = new StringBuffer();
		for (int i = 0; i < nameStack.size(); i++) {
			String name = nameStack.elementAt(i);
			key.append(name).append(".");
		}
		key.deleteCharAt(key.lastIndexOf("."));

		return key.toString();
	}

	private void addToConfiguration(String key, String value) {
		List<String> list = configuration.get(key);
		if (list == null) {
			list = new ArrayList<String>();
			list.add(value);
			configuration.put(key, list);
		} else {
			if (!list.contains(value)) {
				list.add(value);
			}
		}
	}

	public String getFirstProperty(String key) {
		List<String> value = configuration.get(key);
		if (value == null) {
			return null;
		}
		return value.get(0);
	}

}
