/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.server.mgt.test;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.om.xpath.AXIOMXPath;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.test.utils.common.FileManager;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.es.integration.common.clients.ResourceAdminServiceClient;
import org.wso2.es.integration.common.utils.ESIntegrationBaseTest;
import org.wso2.es.integration.common.utils.RegistryProviderUtil;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.List;

public class RegistryConfiguratorTestCase extends ESIntegrationBaseTest {

    private static final Log log = LogFactory.getLog(RegistryConfiguratorTestCase.class);
    private ResourceAdminServiceClient resourceAdminServiceClient;
    private String sessionCookie;

    @BeforeClass(alwaysRun = true)
    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.STANDALONE })
    public void serverRestart() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        sessionCookie = new LoginLogoutClient(automationContext).login();
        resourceAdminServiceClient =
                new ResourceAdminServiceClient(backendURL, sessionCookie);

    }

    @Test(groups = "wso2.greg")
    @SetEnvironment(executionEnvironments = { ExecutionEnvironment.STANDALONE })
    public void testSetupServerEnvironment() throws Exception {

        copyResources();
        copyJarFiles();
        editConfigurationFiles();
        updateRegistry();
        addResourceFileToRegistry();
        ServerConfigurationManager serverConfigurationManager =
                new ServerConfigurationManager(automationContext);
        serverConfigurationManager.restartGracefully();
        Thread.sleep(12000);

    }

    public void copyResources() throws Exception {

        copyMimeMappingFile();

    }

    public void copyJarFiles() {

    }

    public void updateRegistry() throws Exception {
        updateMimetypes();
    }

    public void editConfigurationFiles() throws Exception {

        increaseSearchIndexStartTimeDelay();
        changeServiceCreationElement();
        enableJmxManagement();
        enableWorkList();
        increaseConnectionTimeoutValue();
        changeSsoIdpPort();
    }

    public void updateMimetypes() throws Exception {

        final String MIME_TYPE_PATH = "/_system/config/repository/components/org.wso2.carbon.governance" +
                "/media-types/index";
        WSRegistryServiceClient wsRegistry;
        RegistryProviderUtil registryProviderUtil = new RegistryProviderUtil();
        wsRegistry = registryProviderUtil.getWSRegistry(automationContext);
        Resource resource = wsRegistry.get(MIME_TYPE_PATH);
        resource.addProperty("properties", "text/properties");
        resource.addProperty("cfg", "text/config");
        resource.addProperty("rb", "text/ruby");
        resource.addProperty("drl", "xml/drool");
        resource.addProperty("xq", "xml/xquery");
        resource.addProperty("eva", "xml/evan");
        wsRegistry.put(MIME_TYPE_PATH, resource);

    }

    private void copyMimeMappingFile() throws IOException {

        String fileName = "mime.mappings";
        String targetPath = CarbonUtils.getCarbonHome() + File.separator + "repository" +
                File.separator + "conf" + File.separator + "etc";
        String sourcePath = getTestArtifactLocation() + "artifacts" +
                File.separator + "GREG" + File.separator + "config" + File.separator +
                "mime.mappings";
        FileManager.copyResourceToFileSystem(sourcePath, targetPath, fileName);
    }

    public static OMElement getRegistryXmlOmElement()
            throws FileNotFoundException, XMLStreamException {

        String registryXmlPath = CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator
                + "conf" + File.separator + "registry.xml";
        File registryFile = new File(registryXmlPath);
        FileInputStream inputStream = new FileInputStream(registryFile);
        XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
        StAXOMBuilder builder = new StAXOMBuilder(parser);
        return builder.getDocumentElement();

    }

    private void changeServiceCreationElement() throws Exception {
        FileOutputStream fileOutputStream = null;
        XMLStreamWriter writer = null;
        OMElement documentElement = getRegistryXmlOmElement();
        try {
            AXIOMXPath xpathExpression = new AXIOMXPath("/wso2registry/handler/property");
            List<OMElement> nodes = xpathExpression.selectNodes(documentElement);
            for (OMElement node : nodes) {
                if (node.getAttributeValue(new QName("name")).equals("createSOAPService")) {
                    node.setText("false");
                }
            }
            fileOutputStream = new FileOutputStream(getRegistryXMLPath());
            writer = XMLOutputFactory.newInstance().createXMLStreamWriter(fileOutputStream);
            documentElement.serialize(writer);
            documentElement.build();
            Thread.sleep(2000);

        } catch (Exception e) {
            log.error("Failed to modify registry.xml " + e.getMessage());
            throw new Exception("Failed to modify registry.xml " + e.getMessage());
        } finally {
            assert fileOutputStream != null;
            fileOutputStream.close();
            assert writer != null;
            writer.flush();
        }

    }

    private void increaseSearchIndexStartTimeDelay() throws Exception {

        FileOutputStream fileOutputStream = null;
        XMLStreamWriter writer = null;
        OMElement documentElement = getRegistryXmlOmElement();
        try {
            AXIOMXPath xpathExpression = new AXIOMXPath("/wso2registry/indexingConfiguration/startingDelayInSeconds");
            OMElement indexConfigNode = (OMElement) xpathExpression.selectSingleNode(documentElement);
            indexConfigNode.setText("60");
            AXIOMXPath xpathExpression1 = new AXIOMXPath(
                    "/wso2registry/indexingConfiguration/indexingFrequencyInSeconds");
            OMElement indexConfigNode1 = (OMElement) xpathExpression1.selectSingleNode(documentElement);
            indexConfigNode1.setText("30");
            fileOutputStream = new FileOutputStream(getRegistryXMLPath());
            writer = XMLOutputFactory.newInstance().createXMLStreamWriter(fileOutputStream);
            documentElement.serialize(writer);
            documentElement.build();
            Thread.sleep(2000);

        } catch (Exception e) {
            log.error("registry.xml edit fails" + e.getMessage());
            throw new Exception("registry.xml edit fails" + e.getMessage());
        } finally {
            assert fileOutputStream != null;
            fileOutputStream.close();
            assert writer != null;
            writer.flush();
        }
    }

    private void enableJmxManagement() throws Exception {

        FileOutputStream fileOutputStream = null;
        XMLStreamWriter writer = null;
        OMElement documentElement = getRegistryXmlOmElement();
        try {
            AXIOMXPath xpathExpression = new AXIOMXPath("/wso2registry/jmx");
            OMElement indexConfigNode = (OMElement) xpathExpression.selectSingleNode(documentElement);
            OMAttribute omAttribute = indexConfigNode.getAttribute(new QName("enabled"));
            omAttribute.setAttributeValue("true");
            fileOutputStream = new FileOutputStream(getRegistryXMLPath());
            writer = XMLOutputFactory.newInstance().createXMLStreamWriter(fileOutputStream);
            documentElement.serialize(writer);
            documentElement.build();
            Thread.sleep(2000);

        } catch (Exception e) {
            log.error("registry.xml edit fails" + e.getMessage());
            throw new Exception("registry.xml edit fails" + e.getMessage());
        } finally {
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            if (writer != null) {
                writer.flush();
            }
        }
    }

    private void enableWorkList() throws Exception {

        FileOutputStream fileOutputStream = null;
        XMLStreamWriter writer = null;
        String workList;
        workList = "<workList serverURL=\"local://services/\" remote=\"false\">\n" +
                "        <username>" + automationContext.getContextTenant().getContextUser()
                .getUserName()
                +
                "</username>\n" +
                "        <password>" + automationContext.getContextTenant().getContextUser()
                .getPassword()
                +
                "</password>\n" +
                "    </workList>";

        try {
            OMElement registryXML = getRegistryXmlOmElement();
            registryXML.addChild(AXIOMUtil.stringToOM(workList));
            registryXML.build();
            fileOutputStream = new FileOutputStream(getRegistryXMLPath());
            writer = XMLOutputFactory.newInstance().createXMLStreamWriter(fileOutputStream);
            registryXML.serialize(writer);

        } catch (Exception e) {
            throw new Exception("registry.xml update fails");
        } finally {
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            if (writer != null) {
                writer.flush();
            }
        }

    }

    private String getRegistryXMLPath() {

        return CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator
                + "conf" + File.separator + "registry.xml";
    }

    private void addResourceFileToRegistry()
            throws MalformedURLException, ResourceAdminServiceExceptionException, RemoteException {

        String resourcePath = getTestArtifactLocation() + "artifacts" +
                File.separator + "GREG" + File.separator + "utf8" + File.separator + "test.txt";
        DataHandler dh = new DataHandler(new URL("file:///" + resourcePath));
        resourceAdminServiceClient.addResource("/_system/config/test_utf8_Resource", "text/plain", "testDesc", dh);
    }

    public static OMElement getAxis2XmlOmElement()
            throws FileNotFoundException, XMLStreamException {
        String axis2XmlPath = CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator
                + "conf" + File.separator + "axis2" + File.separator + "axis2_client.xml";

        File axis2File = new File(axis2XmlPath);

        FileInputStream inputStream = new FileInputStream(axis2File);
        XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
        StAXOMBuilder builder = new StAXOMBuilder(parser);

        return builder.getDocumentElement();
    }

    private String getAxis2XMLPath() {
        return CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator
                + "conf" + File.separator + "axis2" + File.separator + "axis2_client.xml";
    }

    private void increaseConnectionTimeoutValue() throws Exception {
        FileOutputStream fileOutputStream = null;
        XMLStreamWriter writer = null;
        OMElement documentElement = getAxis2XmlOmElement();
        try {
            AXIOMXPath xpathExpression = new AXIOMXPath("/axisconfig/transportSender/parameter");
            List<OMElement> nodes = xpathExpression.selectNodes(documentElement);
            for (OMElement node : nodes) {
                if (node.getAttributeValue(new QName("name")).equals("SO_TIMEOUT")) {
                    node.setText("240000");
                }
                if (node.getAttributeValue(new QName("name")).equals("CONNECTION_TIMEOUT")) {
                    node.setText("240000");
                }
            }
            fileOutputStream = new FileOutputStream(getAxis2XMLPath());
            writer = XMLOutputFactory.newInstance().createXMLStreamWriter(fileOutputStream);
            documentElement.serialize(writer);
            documentElement.build();
            Thread.sleep(2000);

        } catch (Exception e) {
            log.error("axis2_client.xml edit fails" + e.getMessage());
            throw new Exception("axis2_client.xml edit fails" + e.getMessage());
        } finally {
            assert fileOutputStream != null;
            fileOutputStream.close();
            assert writer != null;
            writer.flush();
        }
    }

    public OMElement getSsoIdpConfigOmElement()
            throws FileNotFoundException, XMLStreamException {
        String ssoIDPConfigXmlPath = getSsoIdpConfigXMLPath();
        File registryFile = new File(ssoIDPConfigXmlPath);
        FileInputStream inputStream = new FileInputStream(registryFile);
        XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
        StAXOMBuilder builder = new StAXOMBuilder(parser);
        return builder.getDocumentElement();
    }

    /**
     * This method is used to change sso idp port due to port offsets.
     *
     * @throws Exception
     */
    private void changeSsoIdpPort() throws Exception {

        FileOutputStream fileOutputStream = null;
        XMLStreamWriter writer = null;
        OMElement documentElement = getSsoIdpConfigOmElement();
        try {
            AXIOMXPath xpathExpression = new AXIOMXPath("/SSOIdentityProviderConfig/ServiceProviders/ServiceProvider");
            List nodes = xpathExpression.selectNodes(documentElement);

            for (Object node : nodes) {
                OMElement nodeElement = (OMElement) node;
                AXIOMXPath xPathAssertionConsumerService = new AXIOMXPath("AssertionConsumerService");
                OMElement nodeUrlElement = (OMElement) xPathAssertionConsumerService.selectSingleNode(nodeElement);
                String newURL = nodeUrlElement.getText().replace("9443", "10343");
                nodeUrlElement.setText(newURL);
            }

            fileOutputStream = new FileOutputStream(getSsoIdpConfigXMLPath());
            writer = XMLOutputFactory.newInstance().createXMLStreamWriter(fileOutputStream);
            documentElement.serialize(writer);
            documentElement.build();
            Thread.sleep(2000);

        } catch (Exception e) {
            log.error("sso-idp-config.xml edit fails" + e.getMessage());
            throw new Exception("sso-idp-config.xml edit fails" + e.getMessage());
        } finally {
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            if (writer != null) {
                writer.flush();
            }
        }
    }

    private String getSsoIdpConfigXMLPath() {
        return CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator
                + "conf" + File.separator + "identity" + File.separator + "sso-idp-config.xml";
    }
}

