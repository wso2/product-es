/*
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */


package org.wso2.es.ui.integration.test.store;

import org.wso2.es.ui.integration.util.ESUtil;

/**
 * Tests the homepage of the Store when it is accessed by
 * an anonymous user with the tenant url (t/carbon.super)
 */
public class ESStoreAnonSuperTenantHomePageTestCase extends ESStoreAnonHomePageTestCase {
    @Override
    public  String resolveStoreUrl(){
       String tenantDomain = "carbon.super";//TODO: Obtain this from the automation context
       return baseUrl+STORE_URL+ ESUtil.getTenantQualifiedUrl(tenantDomain);
    }
}
