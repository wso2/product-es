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

public class Constants {

    public static final String LC_STATE_CHANGE = "custom:Store LC State Change";
    public static final String ASSET_UPDATE = "custom:Store Asset Update";
    public static final String VERSION_CREATED = "custom:Store Version Create";
    public static final String MESSAGE_SENT = "custom:Store Message Sent";

    public static final String LC_STATE_CHANGE_VAR = "lc.state.change";
    public static final String ASSET_UPDATE_VAR = "asset.update";
    public static final String VERSION_CREATED_VAR = "version.creation";
    public static final String MESSAGE_SENT_VAR = "message.sent";

    public static final String TOPIC_FILTER="http://wso2.org/registry/eventing/dialect/topicFilter";
}
