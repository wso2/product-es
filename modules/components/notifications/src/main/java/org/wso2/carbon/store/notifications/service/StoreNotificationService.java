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

package org.wso2.carbon.store.notifications.service;

import org.wso2.carbon.event.core.exception.EventBrokerException;
import org.wso2.carbon.event.core.subscription.Subscription;
import org.wso2.carbon.event.ws.internal.builders.exceptions.InvalidMessageException;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.store.notifications.events.*;
import org.wso2.carbon.store.notifications.management.Constants;
import org.wso2.carbon.store.notifications.management.StoreNotificationManager;
import org.wso2.carbon.store.notifications.management.StoreSubscriptionManager;

import java.util.List;
import java.util.Map;

/**
 * Store Notification Service
 */
public class StoreNotificationService {

    private StoreNotificationManager storeNotificationManager = new StoreNotificationManager();
    private StoreSubscriptionManager storeSubscriptionManager = new StoreSubscriptionManager();

    /**
     * Notify Store Event
     *
     * @param eventName event to be notified of
     * @param message   message to sent with notification
     * @param path      path of the resource where the event occurred
     * @param tenantId  logged in tenantId
     * @throws Exception if notifying fails
     */
    @SuppressWarnings("unused")
    public void notifyEvent(String eventName, String message, String path, int tenantId) throws Exception {
        AbstractStoreEvent<String> event;
        if (eventName.equals(Constants.LC_STATE_CHANGE_EVENT)) {
            event = new StoreLCStateChangeEvent<String>(message);
        } else if (eventName.equals(Constants.ASSET_UPDATE_EVENT)) {
            event = new StoreAssetUpdateEvent<String>(message);
        } else if (eventName.equals(Constants.VERSION_CREATED_EVENT)) {
            event = new StoreVersionCreateEvent<String>(message);
        } else if (eventName.equals(Constants.MESSAGE_SENT_EVENT)) {
            event = new StoreMessageSentEvent<String>(message);
        } else {
            throw new IllegalStateException("Unknown event type: " + eventName);
        }
        event.setResourcePath(path);
        event.setTenantId(tenantId);
        storeNotificationManager.notifyEvent(event);
    }

    /**
     * Subscribe To an Event
     *
     * @param userName     logged in user
     * @param resourcePath path of the resource subscribing to
     * @param endpoint     notification method (user, role or email)
     * @param eventName    event to be subscribed for
     * @throws InvalidMessageException is subscribing fails
     * @throws RegistryException       if subscribing fails
     */
    @SuppressWarnings("unused")
    public void subscribeToEvent(String userName, String resourcePath, String endpoint,
                                 String eventName) throws InvalidMessageException, RegistryException {
        try {
            storeSubscriptionManager.subscribe(userName, resourcePath, endpoint, eventName);
        } catch (InvalidMessageException e) {
            throw new InvalidMessageException("Subscribing to event:" + eventName + " failed for " +
                    "" + resourcePath, e);
        } catch (RegistryException e) {
            throw new RegistryException("Subscribing to event:" + eventName + " failed for " +
                    resourcePath, e);
        }
    }

    /**
     * Remove subscription
     *
     * @param id Subscription id
     */
    @SuppressWarnings("unused")
    public void unsubscribe(String id) {
        storeSubscriptionManager.unsubscribe(id);
    }

    /**
     * Get all the event types in store
     *
     * @return map of event types
     */
    @SuppressWarnings("unused")
    public Map getEventTypes() {
        return storeSubscriptionManager.getEventTypes();
    }

    /**
     * List all the subscriptions made
     *
     * @return subscription list
     * @throws EventBrokerException if retrieving all subscriptions fails
     */
    @SuppressWarnings("unused")
    public List<Subscription> getAllSubscriptions() throws EventBrokerException {
        return storeSubscriptionManager.getAllSubscriptions();
    }

}
