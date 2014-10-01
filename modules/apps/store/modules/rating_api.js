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
    var log = new Log('rating-api');
    api.rate = function(session, options) {
        var success = false; //Assume the rating will fail
        if (!options.type) {
            log.error('Unable to rate assets without a type');
            return success;
        }
        if (!options.id) {
            log.error('Unable to rate assets without an id.');
            return success;
        }
        if (!options.value) {
            log.error('Cannot rate the asset when there is no rating value provided.');
            return success;
        }
        var am = getAssetManager(session, options.type);
        try {
            success = am.rate(options.id,options.value);
        } catch (e) {
            log.error('Could not rate the asset type: ' + options.type + ' id: ' + options.id + ' with rating: ' + options.value+'.Exception: ' + e);
        }
        return success;
    };
    var getAssetManager = function(session, type) {
        var asset = require('rxt').asset;
        var am = asset.createUserAssetManager(session, type);
        return am;
    }
}(api));