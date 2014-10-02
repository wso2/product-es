/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.store.notifications.management;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.common.eventing.NotificationService;
import org.wso2.carbon.store.notifications.events.StoreMessageSentEvent;
import org.wso2.carbon.store.notifications.events.StoreVersionCreateEvent;
import org.wso2.carbon.store.notifications.events.StoreAssetUpdateEvent;
import org.wso2.carbon.store.notifications.events.StoreLCStateChangeEvent;

public class StoreNotificationManager {

    private String resourcePath;
    private int tenantId;
    private NotificationService registryNotificationService;
    private static Log log = LogFactory.getLog(StoreNotificationManager.class);

    public void notifyEvent(String eventName, String message, String path, int user){

        tenantId=user;
        resourcePath=path;
        try {
            if(eventName.equalsIgnoreCase(Constants.LC_STATE_CHANGE_VAR)){
                notifyLCStateChange(message);
            }else if(eventName.equalsIgnoreCase(Constants.ASSET_UPDATE_VAR)){
                notifyAssetUpdate(message);
            }else if(eventName.equalsIgnoreCase(Constants.VERSION_CREATED_VAR)){
                notifyVersionCreation(message);
            }else if(eventName.equalsIgnoreCase(Constants.MESSAGE_SENT_VAR)){
                notifyMessage(message);
            }else{
                log.error("Requested event does not exist");
            }
        }catch (Exception e){
            e.printStackTrace(); //TODO
        }
    }

    private void notifyLCStateChange(String message) throws Exception {

        StoreLCStateChangeEvent<String> lcStateChangeEvent=new StoreLCStateChangeEvent<String>(message);
        lcStateChangeEvent.setResourcePath(resourcePath);
        lcStateChangeEvent.setTenantId(tenantId);

        registryNotificationService=Utils.getRegistryEventingService();
        if(registryNotificationService!=null){
            registryNotificationService.notify(lcStateChangeEvent);
        }

    }

    private void notifyAssetUpdate(String message) throws Exception {

        StoreAssetUpdateEvent<String> assetUpdateEvent=new StoreAssetUpdateEvent<String>(message);
        assetUpdateEvent.setResourcePath(resourcePath);
        assetUpdateEvent.setTenantId(tenantId);

        registryNotificationService=Utils.getRegistryEventingService();
        if(registryNotificationService!=null){
            registryNotificationService.notify(assetUpdateEvent);
        }

    }

    private void notifyVersionCreation(String message) throws Exception {

        StoreVersionCreateEvent<String> versionCreationEvent=new StoreVersionCreateEvent<String>(message);
        versionCreationEvent.setResourcePath(resourcePath);
        versionCreationEvent.setTenantId(tenantId);

        registryNotificationService=Utils.getRegistryEventingService();
        if(registryNotificationService!=null){
            registryNotificationService.notify(versionCreationEvent);
        }

    }

    private void notifyMessage(String message) throws Exception {

        StoreMessageSentEvent<String> messageSentEvent=new StoreMessageSentEvent<String>(message);
        messageSentEvent.setResourcePath(resourcePath);
        messageSentEvent.setTenantId(tenantId);

        registryNotificationService=Utils.getRegistryEventingService();
        if(registryNotificationService!=null){
            registryNotificationService.notify(messageSentEvent);
        }

    }


}

