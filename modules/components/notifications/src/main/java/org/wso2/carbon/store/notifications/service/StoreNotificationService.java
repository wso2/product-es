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
package org.wso2.carbon.store.notifications.service;

import org.wso2.carbon.event.core.subscription.Subscription;
import org.wso2.carbon.store.notifications.management.StoreNotificationManager;
import org.wso2.carbon.store.notifications.management.StoreSubscriptionManager;

import java.util.List;
import java.util.Map;

public class StoreNotificationService {

    private StoreNotificationManager storeNotificationManager = new StoreNotificationManager();
    private StoreSubscriptionManager storeSubscriptionManager = new StoreSubscriptionManager();

    public void notifyEvent(String eventName, String message, String path, int user){
            storeNotificationManager.notifyEvent(eventName, message,path, user);
    }

    public void subscribeToEvent(String userName, String resourcePath,String endpoint, String eventName){
            storeSubscriptionManager.subscribe(userName, resourcePath, endpoint, eventName);
    }

    public void unsubscribe(String id){
        storeSubscriptionManager.unsubscribe(id);
    }

    public Map getEventTypes() {
        return storeSubscriptionManager.getEventTypes();
    }

    public List<Subscription> getAllSubscriptions(){
        return storeSubscriptionManager.getAllSubscriptions();
    }

}
