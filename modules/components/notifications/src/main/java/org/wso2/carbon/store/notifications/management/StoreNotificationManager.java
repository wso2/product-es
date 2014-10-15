/*
 * Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.wso2.carbon.store.notifications.management;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.common.eventing.NotificationService;
import org.wso2.carbon.store.notifications.events.*;

/**
 * Manages Notification of the Store
 */
public class StoreNotificationManager {

    private NotificationService registryNotificationService = ComponentManager.getRegistryEventingService();
    private static final Log log = LogFactory.getLog(StoreNotificationManager.class);

    /**
     * Notify triggered event
     *
     * @param storeEvent Store Event
     * @throws Exception if Notifying fails
     */
    public void notifyEvent(AbstractStoreEvent storeEvent) throws Exception {
        if (registryNotificationService == null) {
            throw new IllegalStateException("Registry Notification Service Not Found");
        }
        try {
            registryNotificationService.notify(storeEvent);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Notifying the event:" + storeEvent.getEventName() + " for " +
                        storeEvent.getResourcePath() + " failed", e);
            }
            throw new Exception("Notifying the event " + storeEvent.getEventName() + " failed");
        }
    }
}

