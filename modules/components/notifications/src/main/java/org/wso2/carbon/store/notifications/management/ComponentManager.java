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
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.eventing.services.EventingService;
import org.wso2.carbon.store.notifications.events.StoreAssetUpdateEvent;
import org.wso2.carbon.store.notifications.events.StoreLCStateChangeEvent;
import org.wso2.carbon.store.notifications.events.StoreMessageSentEvent;
import org.wso2.carbon.store.notifications.events.StoreVersionCreateEvent;

/**
 * Utility Class to register event types and services
 */
public class ComponentManager {

    private static EventingService registryEventingService;
    private static RegistryService registryService;
    private static Log log = LogFactory.getLog(ComponentManager.class);

    /**
     * Set Registry Service
     *
     * @param service Registry service
     */
    public static synchronized void setRegistryService(RegistryService service) {
        registryService = service;
    }

    /**
     * Get Registry Service
     *
     * @return Registry Service
     */
    public static synchronized RegistryService getRegistryService() {
        return registryService;
    }

    /**
     * Get Registry Eventing Service
     *
     * @return Registry Eventing Service
     */
    public static EventingService getRegistryEventingService() {
        return registryEventingService;
    }

    /**
     * Set Registry Eventing Service
     *
     * @param registryEventingService Registry Eventing Service
     */
    public static void setRegistryEventingService(EventingService registryEventingService) {
        ComponentManager.registryEventingService = registryEventingService;
    }

    /**
     * Register custom Event Types for ES
     */
    public static void registerEvents() {
        if (registryEventingService != null && registryService != null) {
            registryEventingService.registerEventType(Constants.LC_STATE_CHANGE, StoreLCStateChangeEvent.EVENT_NAME, null);
            registryEventingService.registerEventType(Constants.ASSET_UPDATE, StoreAssetUpdateEvent.EVENT_NAME, null);
            registryEventingService.registerEventType(Constants.VERSION_CREATED, StoreVersionCreateEvent.EVENT_NAME, null);
            registryEventingService.registerEventType(Constants.MESSAGE_SENT, StoreMessageSentEvent.EVENT_NAME, null);
            log.info("Store event types successfully registered");
        }else {
            log.error("Store event type registration failed");
        }
    }

}
