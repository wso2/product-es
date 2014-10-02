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
var api = {};
(function(api) {
    api.subscribe = function(session, options) {
        var type = options.type;
        var id = options.id;
        var success = false;
        if (!type) {
            log.error('Unable to locate type information to build an asset manager to subscribe.');
            return success;
        }
        var am = getAssetManager(session, type);
        try {
            success = am.subscribe(id, session);
        } catch (e) {
            log.warn('Unable to process the asset as the id: ' + id + ' could not be obained.Exception: ' + e);
        }
        return success;
    };
    api.unsubscribe = function(session, options) {
        var type = options.type;
        var success = false;
        var id = options.id;
        if (!type) {
            log.error('Unable to locate type information to build an asset manager to subscribe.');
            return success;
        }
        var am = getAssetManager(session, type);
        try {
            success = am.unsubscribe(id, session);
        } catch (e) {
            log.warn('Unable to process the asset as the id: ' + id + ' could not be obained.Exception: ' + e);
        }
        return success;
    };
    api.addSubscriptionDetails = function(assets, am, session) {
        var utils = require('utils').reflection;
        if (!utils.isArray(assets)) {
            assets = [assets];
        }
        for (var index = 0; index < assets.length; index++) {
            assets[index].isSubscribed = am.isSubscribed(assets[index].id, session);
        }
    };
    var getAssetManager = function(session, type) {
        var asset = require('rxt').asset;
        var am = asset.createUserAssetManager(session, type);
        return am;
    }
}(api))