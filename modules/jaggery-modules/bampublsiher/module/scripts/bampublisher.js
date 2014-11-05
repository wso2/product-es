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

var bamclient = {};

(function (bamclient) {

    var Publisher = Packages.org.wso2.store.bamclient.EventPublisher,
        carbon = require('carbon'),
        log = new Log();
    var server = require('store').server;
    var userutil = Packages.org.wso2.carbon.user.core.util.UserCoreUtil;


    bamclient.publishAssetEvents = function (session, eventName, assetUDID, assetType, assetName, description) {

        var user = server.current(session);
        log.info("user" + user);
        var tenantId = -1234;

        if (user != null) {
            tenantId = user.tenantId;
        }

        log.info("tenantId:" + tenantId);
        var userName = user.userName;
        var userStore = userutil.getDomainFromThreadLocal();

        try {
            Publisher.getInstance().publishAssetStatistics(eventName, tenantId, userStore, user.username, assetUDID, assetType, description);
        }catch(e){
            log.error("error bam client:",e);
        }

    };
}(bamclient));