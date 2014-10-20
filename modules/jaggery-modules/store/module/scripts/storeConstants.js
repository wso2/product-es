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

var storeConstants = {};
(function(storeConstants) {

    var subscriptionConstants = Packages.org.wso2.carbon.store.notifications.management.Constants;

    storeConstants.EMAIL_TEMPLATE_LC = "<html><h2>Notification for <%=assetType%>:'<%=assetName%>'</h2><h3>Event:lifecycle state changed</h3><p><b>Comment:</b><%=msg%></p></html>";
    storeConstants.EMAIL_TEMPLATE_UPDATE = "<html><h2>Notification for <%=assetType%>:'<%=assetName%>'</h2><h3>Event:<%=assetType%> updated</h3></html>";
    storeConstants.EMAIL_TEMPLATE_VERSION = "<html><h2>Notification for <%=assetType%>:'<%=assetName%>'</h2><h3>Event:new version created</h3></html>";
    storeConstants.EMAIL_TEMPLATE_DEFAULT = "<html><h2>Notification</h2><p><b>Message:</b><%=msg%></p></html>";

    storeConstants.PRIVATE_ROLE_ENDPOINT = "role://Internal/private_";
    storeConstants.ADMIN_ROLE_ENDPOINT = "role://admin";
    storeConstants.ADMIN_ROLE = "admin";

    storeConstants.LC_STATE_CHANGE = "StoreLifecycleStateChange";
    storeConstants.ASSET_UPDATE = "StoreAssetUpdate";
    storeConstants.VERSION_CREATED = "StoreVersionCreate";
    storeConstants.MESSAGE_SENT = "StoreMessageSent";

    storeConstants.LC_STATE_CHANGE_EVENT = "lc.state.change";
    storeConstants.ASSET_UPDATE_EVENT = "asset.update";
    storeConstants.VERSION_CREATED_EVENT = "version.creation";
    storeConstants.MESSAGE_SENT_EVENT = "message.sent";

}(storeConstants));

