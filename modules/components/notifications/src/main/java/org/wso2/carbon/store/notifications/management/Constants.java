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

/**
 * Constants holder
 */
public class Constants {

    public static final String LC_STATE_CHANGE = "custom:Store LC State Change";
    public static final String ASSET_UPDATE = "custom:Store Asset Update";
    public static final String VERSION_CREATED = "custom:Store Version Create";
    public static final String MESSAGE_SENT = "custom:Store Message Sent";

    public static final String LC_STATE_CHANGE_EVENT = "lc.state.change";
    public static final String ASSET_UPDATE_EVENT = "asset.update";
    public static final String VERSION_CREATED_EVENT = "version.creation";
    public static final String MESSAGE_SENT_EVENT = "message.sent";

    public static final String TOPIC_FILTER = "http://wso2.org/registry/eventing/dialect/topicFilter";
    public static final String MAILTO_TAG = "mailto:";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String TEXT_HTML = "text/html";
}
