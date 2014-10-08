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
package org.wso2.carbon.store.notifications.events;

/**
 * Custom event type for Store - LCStateChangeEvent
 * Triggered when the state of an asset changes
 *
 * @param <T>
 */
public class StoreLCStateChangeEvent<T> extends AbstractStoreEvent<T> {

    public static final String EVENT_NAME = "StoreLifecycleStateChange";

    /**
     * Construct the Registry Event by using the message
     *
     * @param message message used in the notification when event is triggered
     */
    public StoreLCStateChangeEvent(T message) {
        super(message);
        this.eventName = EVENT_NAME;
    }
}
