/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.es.migration.client;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.es.migration.EsMigrationException;
import org.wso2.carbon.es.migration.client.internal.ServiceHolder;
import org.wso2.carbon.es.migration.util.Constants;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/*
 * This class perform a migration process to remove "provider" child from the resource content
 */
public class ProviderMigrationClient {
    private static final Log log = LogFactory.getLog(ProviderMigrationClient.class);

    /*
     * This method is used to handle the migration process
     */
    public void providerMigration() throws EsMigrationException {
        long startTimeMillis = System.currentTimeMillis();
        log.info("Provider migration client started");
        try {
            List<Tenant> tenantsArray = getTenantsArray();
            for (Tenant tenant : tenantsArray) {
                migrate(tenant);
            }
            log.info("Migration Completed Successfully in " + (System.currentTimeMillis() - startTimeMillis)
                     + "ms");
        } catch (IOException e) {
            String msg = "Error occurred while performing operations on input source. ";
            log.error(msg);
            throw new EsMigrationException(msg, e);
        } catch (UserStoreException e) {
            String msg = "Error occurred while searching for tenant admin. ";
            log.error(msg);
            throw new EsMigrationException(msg, e);
        } catch (RegistryException e) {
            String msg = "Error occurred while performing registry operation. ";
            log.error(msg);
            throw new EsMigrationException(msg, e);
        } catch (TransformerException e) {
            String msg = "Error occurred while converting DOM objects into xml string. ";
            log.error(msg);
            throw new EsMigrationException(msg, e);
        } catch (SAXException e) {
            String msg = "Error occurred while parsing the xml content into DOM objects. ";
            log.error(msg);
            throw new EsMigrationException(msg, e);
        } catch (ParserConfigurationException e) {
            String msg = "Error occurred while defining document builder. ";
            log.error(msg);
            throw new EsMigrationException(msg, e);
        } catch (XMLStreamException e) {
            String msg = "Error while processing string to xml";
            log.error(msg);
            throw new EsMigrationException(msg, e);
        }
    }

    /*
     * This method is used to get all tenants as a tenant array
     */
    private List<Tenant> getTenantsArray() throws UserStoreException {
        TenantManager tenantManager = ServiceHolder.getRealmService().getTenantManager();
        List<Tenant> tenantsArray = new ArrayList<Tenant>(Arrays.asList(tenantManager.getAllTenants()));
        Tenant superTenant = new Tenant();
        superTenant.setDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        superTenant.setId(MultitenantConstants.SUPER_TENANT_ID);
        tenantsArray.add(superTenant);
        return tenantsArray;
    }

    /*
     * This method is used to handle the migration process
     */
    private void migrate(Tenant tenant)
            throws UserStoreException, RegistryException, SAXException, TransformerException,
                   ParserConfigurationException, IOException, XMLStreamException {
        int tenantId = tenant.getId();
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenant.getDomain(), true);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
            String adminName = ServiceHolder.getRealmService().getTenantUserRealm(tenantId).getRealmConfiguration()
                    .getAdminUserName();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(adminName);
            ServiceHolder.getTenantRegLoader().loadTenantRegistry(tenantId);
            Registry registry = ServiceHolder.getRegistryService().getRegistry(adminName, tenantId);
            Collection resourceTypes = (Collection) registry.get(Constants.RESOURCETYPES_RXT_PATH);
            String[] resourceTypesRxtPaths = resourceTypes.getChildren();
            for (String resourceTypeRxtPath : resourceTypesRxtPaths) {
                if (!isContentArtifact(resourceTypeRxtPath, registry)) {
                    ServiceHolder.getRXTStoragePathService().addStoragePath(getMediaType(resourceTypeRxtPath, registry),
                                                                            getStoragePath(resourceTypeRxtPath, registry));
                    if (!(getStoragePath(resourceTypeRxtPath, registry).contains("@{overview_provider}")) ||
                        !hasOverviewProviderElement(resourceTypeRxtPath, registry)) {
                        String[] storagePathElements = getStoragePath(resourceTypeRxtPath, registry).split("/");
                        String storagePath = Constants.GOV_PATH;
                        for (String storagePathElement : storagePathElements) {
                            if (storagePathElement.startsWith("@")) {
                                break;
                            }
                            if (!storagePathElement.isEmpty()) {
                                storagePath += ("/" + storagePathElement);
                            }
                        }
                        if (registry.resourceExists(storagePath)) {
                            Collection storageCollection = (Collection) registry.get(storagePath);
                            migrateProvider(storageCollection, registry);
                        }
                    }


                }
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /*
     * This method is used to check whether the artifact at the given rxt path is a content artifact
     */
    private boolean isContentArtifact(String rxtPath, Registry registry)
            throws RegistryException, IOException, SAXException, ParserConfigurationException {
        Resource artifactRxt = registry.get(rxtPath);
        byte[] rxtContent = (byte[]) artifactRxt.getContent();
        String rxtContentString = RegistryUtils.decodeBytes(rxtContent);
        Document dom = stringToDocument(rxtContentString);
        Element domElement = (Element) dom.getElementsByTagName(Constants.ARTIFACT_TYPE).item(0);
        return domElement.hasAttribute(Constants.FILE_EXTENSION);
    }

    /*
     * This method is used to get the media type of the artifact at the given rxt path
     */
    private String getMediaType(String rxtPath, Registry registry)
            throws RegistryException, IOException, SAXException, ParserConfigurationException {
        Resource artifactRxt = registry.get(rxtPath);
        byte[] rxtContent = (byte[]) artifactRxt.getContent();
        String rxtContentString = RegistryUtils.decodeBytes(rxtContent);
        Document dom = stringToDocument(rxtContentString);
        Element domElement = (Element) dom.getElementsByTagName(Constants.ARTIFACT_TYPE).item(0);
        return domElement.getAttribute(Constants.TYPE);
    }

    /*
     * This method is used to get the storage path of the artifact at the given rxt path
     */
    private String getStoragePath(String rxtPath, Registry registry)
            throws RegistryException, IOException, SAXException, ParserConfigurationException {
        Resource artifactRxt = registry.get(rxtPath);
        byte[] rxtContent = (byte[]) artifactRxt.getContent();
        String rxtContentString = RegistryUtils.decodeBytes(rxtContent);
        Document dom = stringToDocument(rxtContentString);
        Node storagePath = dom.getElementsByTagName(Constants.STORAGE_PATH).item(0);
        return storagePath.getFirstChild().getNodeValue();
    }

    /**
     * This method checks whether there is a provider in the overview table.
     * @param rxtPath
     * @param registry
     * @return boolean
     * @throws RegistryException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws XMLStreamException
     */
    private boolean hasOverviewProviderElement(String rxtPath, Registry registry)
            throws RegistryException, IOException, SAXException, ParserConfigurationException, XMLStreamException {
        Resource artifactRxt = registry.get(rxtPath);
        byte[] rxtContent = (byte[]) artifactRxt.getContent();
        String rxtContentString = RegistryUtils.decodeBytes(rxtContent);
        OMElement rxtContentOM = AXIOMUtil.stringToOM(rxtContentString);
        OMElement contentElement = rxtContentOM.getFirstChildWithName(new QName(Constants.CONTENT));
        Iterator tableNodes = contentElement.getChildrenWithLocalName(Constants.TABLE);
        while (tableNodes.hasNext()) {
            OMElement tableOMElement = (OMElement) tableNodes.next();
            if ("Overview".equals(tableOMElement.getAttributeValue(new QName(Constants.NAME)))) {
                Iterator fieldNodes = tableOMElement.getChildrenWithLocalName(Constants.FIELD);
                while (fieldNodes.hasNext()) {
                    OMElement fieldElement = (OMElement) fieldNodes.next();
                    OMElement nameElement = fieldElement.getFirstChildWithName(new QName(Constants.NAME));
                    if (nameElement != null) {
                        if ("Provider".equals(nameElement.getText())) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }
    /*
     * This method is used to remove the 'provider' child of 'overview' from the xml content
     */
    private void migrateProvider(Collection root, Registry registry)
            throws RegistryException, SAXException, TransformerException, ParserConfigurationException, IOException {
        String[] childrenPaths = root.getChildren();
        for (String child : childrenPaths) {
            Resource childResource = registry.get(child);
            if (childResource instanceof Collection) {
                migrateProvider((Collection) childResource, registry);
            } else {
                String path = childResource.getPath();
                byte[] configContent = (byte[]) childResource.getContent();
                String contentString = RegistryUtils.decodeBytes(configContent);
                Document dom = stringToDocument(contentString);
                if (dom.getElementsByTagName(Constants.OVERVIEW).getLength() > 0) {
                    Node overview = dom.getElementsByTagName(Constants.OVERVIEW).item(0);
                    NodeList childrenList = overview.getChildNodes();
                    for (int j = 0; j < childrenList.getLength(); j++) {
                        Node node = childrenList.item(j);
                        if (Constants.PROVIDER.equals(node.getNodeName())) {
                            overview.removeChild(node);
                        }
                    }
                    String newContentString = documentToString(dom);
                    byte[] newContentObject = RegistryUtils.encodeString(newContentString);
                    childResource.setContent(newContentObject);
                    registry.put(path, childResource);
                }
            }
        }
    }

    /*
     * This method is used to convert given xml string to Document type
     */
    private Document stringToDocument(String strXml) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(strXml));
        return builder.parse(is);
    }

    /*
     * This method is used to convert given xml Document to String type
     */
    private String documentToString(Document doc) throws TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,  "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.getBuffer().toString();
    }
}