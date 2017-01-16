/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.es.migration.client;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.es.migration.EsMigrationException;
import org.wso2.carbon.es.migration.MigrationDatabaseCreator;
import org.wso2.carbon.es.migration.client.internal.ServiceHolder;
import org.wso2.carbon.es.migration.util.Constants;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceArtifactConfiguration;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.user.core.util.DatabaseUtil;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * This class migrates registry artifacts which uses overview_provider in the storage path and/or uses it in resource
 * metadata content when email login is used.
 */
public class EmailUserNameMigrationClient implements MigrationClient{

    private static final Log log = LogFactory.getLog(EmailUserNameMigrationClient.class);
    private DataSource dataSource, umDataSource;

    /**
     * This method replaces artifacts containing email username with ':' to '-at-'.
     * This will replace the storage path and resource content which contains overview_provider attribute with emailusername.
     *
     * @throws org.wso2.carbon.es.migration.EsMigrationException
     */
    private void migrateResourcesWithEmailUserName() throws EsMigrationException {
        long startTimeMillis = System.currentTimeMillis();
        log.info("Resource migration for email username started.");
        try {
            //databaseMigration();
            List<Tenant> tenantsArray = getTenantsArray();
            for (Tenant tenant : tenantsArray) {
                migrate(tenant);
            }
            log.info("Migration for email username Completed Successfully in " +
                     (System.currentTimeMillis() - startTimeMillis)
                     + "ms");
        } catch (UserStoreException e) {
            String msg = "Error occurred while searching for tenant admin. ";
            throw new EsMigrationException(msg, e);
        } catch (RegistryException e) {
            String msg = "Error occurred while performing registry operation. ";
            throw new EsMigrationException(msg, e);
        } catch (XMLStreamException e) {
            String msg = "Error while processing string to xml(registry.xml). ";
            throw new EsMigrationException(msg, e);
        } catch (Exception e){
            String msg = "Error while migrating. ";
            throw new EsMigrationException(msg, e);
        }

    }

    @Override
    public void cleanOldResources() throws EsMigrationException {
        log.info("Not implemeted for Email user migration client");
    }

    /**
     * This method calls the methods related to getting datasources and executing migration scripts on those datasources.
     * @throws EsMigrationException
     */
    public void databaseMigration() throws EsMigrationException {
        try {
            initDataSource();
            initUMDataSource();
        } catch (CarbonException | XMLStreamException e) {//TODO:Java7 exception handling
            String msg = "Error while processing inputstream of registry.xml. ";
            throw new EsMigrationException(msg, e);
        }catch (NamingException e) {
            String msg = "Error when looking up the Data Source. ";
            throw new EsMigrationException(msg, e);
        } catch (SQLException e) {
            String msg = "Failed to execute the migration script. ";
            throw new EsMigrationException(msg, e);
        } catch (IOException e) {
            String msg = e.getMessage();
            throw new EsMigrationException(msg, e);
        } catch (Exception e) {
            String msg = "Error while migrating database. ";
            throw new EsMigrationException(msg, e);
        }
    }

    @Override
    public void registryResourceMigration() throws EsMigrationException {
        migrateResourcesWithEmailUserName();
    }

    /**
     * This method extracts the datsources configured in registry.xml and iterate over them and execute sql scripts on
     * each datasource.
     */
    private void initDataSource() throws CarbonException, XMLStreamException, NamingException,
                                         SQLException, IOException {
        InputStream xmlInputStream = null;
        String regConfigPath = Constants.REGISTRY_XML_PATH;
        File file = new File(regConfigPath);
        try {
            xmlInputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new IOException("Error while processing registry.xml file. ", e);
        }
        StAXOMBuilder builder = new StAXOMBuilder(
                CarbonUtils.replaceSystemVariablesInXml(xmlInputStream));
        OMElement configElement = builder.getDocumentElement();
        Iterator dbConfigs = configElement.getChildrenWithName(new QName(Constants.DB_CONFIG));
        while (dbConfigs.hasNext()) {
            OMElement dbConfig = (OMElement) dbConfigs.next();
            OMElement dataSourceNameElem = dbConfig.getFirstChildWithName(new QName(Constants.DATASOURCE));
            if (dataSourceNameElem != null) {
                String dataSourceName = dataSourceNameElem.getText();
                Context ctx = new InitialContext();
                dataSource = (DataSource) ctx.lookup(dataSourceName);
                MigrationDatabaseCreator migrationDatabaseCreator = new MigrationDatabaseCreator(dataSource);
                try {
                    migrationDatabaseCreator.executeRegistryMigrationScript();
                } catch (IOException e) {
                    throw new IOException("Error while reading registry migration script. ", e);
                }
            }
        }
    }

    /**
     * This method initializes and execute the um migration scripts.
     * @throws Exception
     */
    private void initUMDataSource() throws SQLException, IOException {
        umDataSource = DatabaseUtil.getRealmDataSource(ServiceHolder.getRealmService().getBootstrapRealmConfiguration());
        MigrationDatabaseCreator migrationDatabaseCreator = new MigrationDatabaseCreator(dataSource, umDataSource);
        try {
            migrationDatabaseCreator.executeUmMigrationScript();
        } catch (IOException e) {
            throw new IOException("Error while reading um migration script. ",e);
        }
    }

    /**
     * This method replaces artifacts containing email username with ':' to '-at-'.
     * This will replace the storage path and resource content which contains overview_provider attribute with emailusername.
     *
     * @param artifacts artifacts of a particular rxt type. with overview-provider attribute in the storage path.
     * @param registry registry instance
     * @throws RegistryException
     * @throws javax.xml.stream.XMLStreamException
     */
    private static void migrateArtifactsWithEmailUserName(GenericArtifact[] artifacts, Registry registry)
            throws RegistryException, XMLStreamException {
        for (GenericArtifact artifact : artifacts) {
            boolean isProviderMetadataUpdated = false;
            String relativePath = artifact.getPath();
            if (registry.resourceExists(relativePath)) {
                Resource resource = registry.get(relativePath);
                String metadataString = RegistryUtils.decodeBytes((byte[]) resource.getContent());
                OMElement metadataOM = AXIOMUtil.stringToOM(metadataString);
                OMElement overview = metadataOM.getFirstChildWithName(new QName(Constants.METADATA_NAMESPACE,
                                                                                Constants.OVERVIEW));
                OMElement providerElement = overview.getFirstChildWithName(new QName(Constants.METADATA_NAMESPACE,
                                                                                     Constants.PROVIDER));
                if (providerElement != null && providerElement.getText().contains(Constants.OLD_EMAIL_AT_SIGN)) {
                    String oldProviderName = providerElement.getText();
                    String newProviderName = oldProviderName.replace(Constants.OLD_EMAIL_AT_SIGN,
                                                                     Constants.NEW_EMAIL_AT_SIGN);
                    providerElement.setText(newProviderName);
                    resource.setContent(metadataOM.toStringWithConsume());
                    isProviderMetadataUpdated = true;
                }
                String newPath = null;
                if (relativePath.contains(Constants.OLD_EMAIL_AT_SIGN)) {
                    newPath = relativePath.replace(Constants.OLD_EMAIL_AT_SIGN,
                                                   Constants.NEW_EMAIL_AT_SIGN);//TODO:replaceALL
                    registry.move(relativePath, newPath);
                    registry.put(newPath, resource);
                } else if (relativePath.contains(Constants.NEW_EMAIL_AT_SIGN)) {
                    newPath = relativePath;
                    if(isProviderMetadataUpdated==true) {
                        registry.put(newPath, resource);
                    }
                }
            }
        }
    }

    /**
     * This method returns the list of tenants.
     * @return list of tenants
     * @throws UserStoreException
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

    /**
     * This method extracts the artifact types which contains '@{overview_provider}' in the storage path, and call the
     * migration method.
     * @param tenant The tenant object
     * @throws UserStoreException
     * @throws RegistryException
     * @throws XMLStreamException
     */
    private void migrate(Tenant tenant)
            throws UserStoreException, RegistryException, XMLStreamException{

        int tenantId = tenant.getId();
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenant.getDomain(), true);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
            String adminName = ServiceHolder.getRealmService().getTenantUserRealm(tenantId).getRealmConfiguration()
                    .getAdminUserName();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(adminName);
            ServiceHolder.getTenantRegLoader().loadTenantRegistry(tenantId);
            Registry registry = ServiceHolder.getRegistryService().getGovernanceUserRegistry(adminName, tenantId);
            GovernanceUtils.loadGovernanceArtifacts((UserRegistry) registry);
            List<GovernanceArtifactConfiguration> configurations = GovernanceUtils.
                    findGovernanceArtifactConfigurations(registry);
            for (GovernanceArtifactConfiguration governanceArtifactConfiguration : configurations) {
                String pathExpression = governanceArtifactConfiguration.getPathExpression();
                if (pathExpression.contains(Constants.OVERVIEW_PROVIDER) ||
                    hasOverviewProviderElement(governanceArtifactConfiguration)) {
                    String shortName = governanceArtifactConfiguration.getKey();
                    GenericArtifactManager artifactManager = new GenericArtifactManager(registry, shortName);
                    GenericArtifact[] artifacts = artifactManager.getAllGenericArtifacts();
                    migrateArtifactsWithEmailUserName(artifacts, registry);
                }
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }

    }

    /**
     * This method checks whether there is a overview_provider in the overview table.
     * @param governanceArtifactConfiguration
     * @return
     * @throws RegistryException
     * @throws XMLStreamException
     */
    private boolean hasOverviewProviderElement(GovernanceArtifactConfiguration governanceArtifactConfiguration)
            throws RegistryException, XMLStreamException {

        OMElement rxtContentOM = governanceArtifactConfiguration.getContentDefinition();
        Iterator tableNodes = rxtContentOM.getChildrenWithLocalName(Constants.TABLE);
        while (tableNodes.hasNext()) {
            OMElement tableOMElement = (OMElement) tableNodes.next();
            if (Constants.OVERVIEW.equalsIgnoreCase(tableOMElement.getAttributeValue(new QName(Constants.NAME)))) {
                Iterator fieldNodes = tableOMElement.getChildrenWithLocalName(Constants.FIELD);
                while (fieldNodes.hasNext()) {
                    OMElement fieldElement = (OMElement) fieldNodes.next();
                    OMElement nameElement = fieldElement.getFirstChildWithName(new QName(Constants.NAME));
                    if (nameElement != null) {
                        if (Constants.PROVIDER.equalsIgnoreCase(nameElement.getText())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

}
