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

/**
 * Handles notifications and subscriptions for store
 * @nameSpace NotificationManager
 */
var notificationManager = {};
(function () {

    var log = new Log('notificationManager');
    //noinspection JSUnresolvedVariable
    var StoreNotificationService = Packages.org.wso2.carbon.store.notifications.service.StoreNotificationService;
    var storeNotifier = new StoreNotificationService();

    /**
     * Notify triggered event
     * @param eventName event that is triggered
     * @param assetType type of the asset event triggered on
     * @param assetName name of the asset event triggered on
     * @param message message to be sent in the notification
     * @param path path of the resource the event occured
     * @param user logged in user
     */
    notificationManager.notifyEvent = function (eventName, assetType, assetName, message, path, user) {
        var isSuccessful;
        var emailContent = generateEmail(assetType, assetName, message, eventName);
        try {
            storeNotifier.notifyEvent(eventName, emailContent, path, user);
            isSuccessful = true;
        } catch (e) {
            log.error('Notifying the event ' + eventName + 'failed for ' + eventName, e);
            isSuccessful = false;
        }
        return isSuccessful;
    };

    /**
     * Subscribe for an event
     * @param tenantId logged in user
     * @param resourcePath path of the resource subscribing to
     * @param endpoint method of notification (user, role or email)
     * @param eventName event subscribing for
     */
    notificationManager.subscribeToEvent = function (tenantId, resourcePath, endpoint, eventName) {
        var isSuccessful;
        try {
            storeNotifier.subscribeToEvent(tenantId, resourcePath, endpoint, eventName);
            isSuccessful = true;
        } catch (e) {
            log.error('Subscribing to asset on ' + resourcePath + ' failed for ' + eventName, e);
            isSuccessful = false;
        }
        return isSuccessful;
    };

    /**
     * Remove subscription
     * @param resourcePath path of the resource subscribed to
     * @param eventName event subscribed for
     * @param endpoint method of notification
     */
    notificationManager.unsubscribe = function (resourcePath, eventName, endpoint) {
        storeNotifier.unsubscribe(resourcePath, eventName, endpoint);
    };

    /**
     * Get all the event types
     * @returns event types
     */
    notificationManager.getEventTypes = function () {
        return storeNotifier.getEventTypes();
    };

    /**
     * Get all subscriptions
     * @returns list of subscriptions
     */
    notificationManager.getAllSubscriptions = function () {
        try {
            return storeNotifier.getAllSubscriptions();
        } catch (e) {
            log.error("Retrieving subscription list failed", e);
            return null;
        }
    };

    /**
     * Generates an email message containing details of an event
     * @param assetType type of the asset event triggered on
     * @param assetName name of the asset event triggered on
     * @param msg message to send in the email
     * @param eventName name of the triggered event
     * @return content of the email
     */
    var generateEmail = function (assetType, assetName, msg, eventName) {

        var stringAssetName = stringify(assetName);
        var stringAssetType = stringify(assetType);
        var stringMsg = stringify(msg);

        var email_temp;
        if (eventName == storeConstants.LC_STATE_CHANGE_EVENT) {
            email_temp = storeConstants.EMAIL_TEMPLATE_LC;
        } else if (eventName == storeConstants.ASSET_UPDATE_EVENT) {
            email_temp = storeConstants.EMAIL_TEMPLATE_UPDATE;
        } else if (eventName == storeConstants.VERSION_CREATED_EVENT) {
            email_temp = storeConstants.EMAIL_TEMPLATE_VERSION;
        } else {
            email_temp = storeConstants.EMAIL_TEMPLATE_DEFAULT;
        }

        message = new JaggeryParser().parse(email_temp).toString();

        //evaluating the template in Jaggery //TODO improve template evaluation to support custom templates
        var templateScript = '(function() { var result = ""; var assetName=' + stringAssetName + '; var msg =' + stringMsg + '; var assetType =' + stringAssetType +
            '; print = function(text) { if(typeof text === "object") {result += stringify(text);} else {result += text;} };' + message + ' return result;}())';

        return eval(templateScript);
    }

}());