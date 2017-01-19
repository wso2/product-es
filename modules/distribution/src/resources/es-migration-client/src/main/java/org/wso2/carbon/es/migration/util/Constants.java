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
package org.wso2.carbon.es.migration.util;

import java.io.File;

public class Constants {

    public static final String VERSION_210 = "2.1.0";
    //Constants for email username migration client
    public static final String METADATA_NAMESPACE = "http://www.wso2.org/governance/metadata";
    public static final String OLD_EMAIL_AT_SIGN = ":";
    public static final String NEW_EMAIL_AT_SIGN = "-at-";
    public static final String CARBON_HOME = System.getProperty("carbon.home");
    public static final String REGISTRY_XML_PATH = Constants.CARBON_HOME + File.separator + "repository" + File.separator
                                                   + "conf" + File.separator + "registry.xml";
    public static final String UM_MIGRATION_SCRIPT = "/migration-scripts/um_migration.sql";
    public static final String REGISTRY_MIGRATION_SCRIPT = "/migration-scripts/reg_migration.sql";
    public static final String GOV_PATH = "/_system/governance";
    public static final String RESOURCETYPES_RXT_PATH =
            GOV_PATH + "/repository/components/org.wso2.carbon.governance/types";
    public static final String ARTIFACT_TYPE = "artifactType";
    public static final String FILE_EXTENSION = "fileExtension";
    public static final String TYPE = "type";
    public static final String STORAGE_PATH = "storagePath";
    public static final String CONTENT = "content";
    public static final String TABLE = "table";
    public static final String NAME = "name";
    public static final String FIELD = "field";
    public static final String OVERVIEW = "overview";
    public static final String PROVIDER = "provider";
    public static final String OVERVIEW_PROVIDER = "@{overview_provider}";
    public static final String DB_CONFIG = "dbConfig";
    public static final String DATASOURCE = "dataSource";
    //registry path for store.json file at config registry
    public static final String STORE_CONFIG_PATH = "/store/configs/store.json";

    public static final String PUBLISHER_CONFIG_PATH = "/publisher/configs/publisher.json";
    public static final String LOGIN_PERMISSION = "\"/permission/admin/login\":";

    public static final String PERMISSION_ACTION = "[\"ui.execute\"]";
    public static final String LOGIN_SCRIPT = "\"/permission/admin/login\":[\"ui.execute\"]";
    public static final String USER_PERMISSION_FIX_SCRIPT = "/migration-scripts/user_permission_fix.sql";


}
