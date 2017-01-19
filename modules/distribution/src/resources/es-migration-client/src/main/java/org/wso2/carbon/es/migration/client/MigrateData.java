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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.es.migration.EsMigrationException;
import org.wso2.carbon.es.migration.MigrationDatabaseCreator;
import org.wso2.carbon.es.migration.client.internal.ServiceHolder;
import org.wso2.carbon.es.migration.util.Constants;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.user.core.util.DatabaseUtil;

import javax.sql.DataSource;
import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class remove the old store and publisher tenant configuration (store.json).
 */
public class MigrateData implements MigrationClient {
    private static final Log log = LogFactory.getLog(MigrateData.class);
    private DataSource dataSource, umDataSource;

    @Override
    public void databaseMigration() throws EsMigrationException {
        try {
            umDataSource = DatabaseUtil.getRealmDataSource(ServiceHolder.getRealmService().getBootstrapRealmConfiguration());
            MigrationDatabaseCreator migrationDatabaseCreator = new MigrationDatabaseCreator(dataSource, umDataSource);
            migrationDatabaseCreator.executeUserPermissionFixScript();
            log.info("Database migration completed successfully.");
        } catch (Exception e) {
            String msg = "Error occurred while executing the database migration.";
            log.error(msg, e);
            throw new EsMigrationException(msg, e);
        }
    }

    @Override
    public void registryResourceMigration() throws EsMigrationException {
        log.info("Not implemented in 2.0.0 to 2.1.0 migration");
    }

    @Override
    public void cleanOldResources() throws EsMigrationException {
        List<Tenant> tenantsArray = null;
        try {
            long startTimeMillis = System.currentTimeMillis();
            log.info("Store configuration migration started.");
            tenantsArray = getTenantsArray();
            for (Tenant tenant : tenantsArray) {
                clean(tenant);
            }
            log.info("Migration for store config Completed Successfully in " +
                    (System.currentTimeMillis() - startTimeMillis) + "ms");
        } catch (UserStoreException e) {
            String msg = "Error occurred while searching for tenant admin. ";
            log.error(msg, e);
            throw new EsMigrationException(msg, e);
        } catch (RegistryException e) {
            String msg = "Error occurred while performing registry operation. ";
            log.error(msg, e);
            throw new EsMigrationException(msg, e);
        } catch (XMLStreamException e) {
            String msg = "Error while processing string to xml";
            log.error(msg, e);
            throw new EsMigrationException(msg, e);
        }
    }

    /**
     * This method returns the list of tenants.
     *
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
     * This method removes the store.json file at config registry which will fix issue REGISTRY-3528
     *
     * @param tenant tenant
     * @throws UserStoreException
     * @throws RegistryException
     * @throws XMLStreamException
     */
    private void clean(Tenant tenant) throws UserStoreException, RegistryException, XMLStreamException {
        int tenantId = tenant.getId();
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenant.getDomain(), true);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
            String adminName = ServiceHolder.getRealmService().getTenantUserRealm(tenantId).getRealmConfiguration()
                    .getAdminUserName();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(adminName);
            ServiceHolder.getTenantRegLoader().loadTenantRegistry(tenantId);
            Registry registry = ServiceHolder.getRegistryService().getConfigUserRegistry(adminName, tenantId);

            updateResource(registry, Constants.STORE_CONFIG_PATH);
            updateResource(registry, Constants.PUBLISHER_CONFIG_PATH);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }

    }

    private void updateResource(Registry registry, String path) throws RegistryException {
        if (registry.resourceExists(path)) {
            Resource resource = registry.get(path);
            String content = new String((byte[]) resource.getContent());
            if (content.contains(Constants.LOGIN_SCRIPT)) {
                content = content.replace(Constants.LOGIN_PERMISSION, "");
                content = content.replace(Constants.PERMISSION_ACTION, "");
                resource.setContent(content);
                registry.put(path, resource);
            }

        }
    }

}
