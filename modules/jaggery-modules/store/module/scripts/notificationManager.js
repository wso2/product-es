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
var notificationManager = {};
(function () {

        var StoreNotificationService = Packages.org.wso2.carbon.store.notifications.service.StoreNotificationService;
        var storeNotifier = new StoreNotificationService();

        notificationManager.notifyEvent = function(eventName, message, path, user) {
              storeNotifier.notifyEvent(eventName, message, path, user);
        };

        notificationManager.subscribeToEvent = function(tenantId, resourcePath, endpoint, eventName) {
              var log = new Log('notificationManager');
              storeNotifier.subscribeToEvent(tenantId, resourcePath, endpoint, eventName);
        };

	    notificationManager.unsubscribe = function(resourcePath, eventName, endpoint) {
              storeNotifier.unsubscribe(resourcePath, eventName, endpoint);
        };

	    notificationManager.getEventTypes = function(resourcePath, eventName, endpoint) {
              return storeNotifier.getEventTypes(resourcePath, eventName, endpoint);
        };

	    notificationManager.getAllSubscriptions = function(resourcePath, eventName, endpoint) {
              return storeNotifier.getAllSubscriptions(resourcePath, eventName, endpoint);
        };
	

}());