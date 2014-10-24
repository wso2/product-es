/*
 * Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
var conf = require("conf.json");
var authenticate, sessionId;
var assetId;// = '7e0f7270-30b6-4993-86a2-8ea4cb59f5d3';

var username = conf.authConfiguration.username;
var password = conf.authConfiguration.password;
var server_url = conf.StoreConfigurations.url;
var count = 0;

describe('Assets POST - Publisher API', function () {

    /*
     * Endpoint: /publisher/apis/assets?type=<type>
     * Method: POST
     * Response: created asset
     * test: check for a return-id
     */
    it('Test add asset', function () {
        var url = server_url + '/assets?type=gadget';
        var asset = {'overview_name': 'WSO2 Test Gadget',
            'overview_version': '1.2.3',
            'overview_provider': 'admin',
            'overview_description': 'initial description',
            'overview_category': 'Google'};
        var header = obtainAuthorizedHeaderForAPICall();
        var response;
        try {
            response = post(url, asset, header, 'json');
        } catch (e) {
            log.error(e);
        } finally {
            deleteAssetWithID(response.data.data.id);
            logoutAuthorizedUser(header);
            expect(response.data.data).not.toBe(undefined);
            expect(response.data.data.name).toEqual(asset.overview_name);
            expect(response.data.data.attributes.overview_description).toEqual(asset.overview_description);
        }
    });

    /*
     * Endpoint: /publisher/apis/assets/<asset-id>/state?type=gadget
     * Method: POST
     * Response: success message
     * test: data content 'Changed the state to In-Review'
     */
    it('Test invoke lifecycle action by id', function () {
        assetID = getAssetID();
        var url = server_url + '/assets/' + assetId + '/state?type=gadget';
        url = encodeURI(url);
        var header = obtainAuthorizedHeaderForAPICall();
        try {
            var response = post(url, {"nextState": "In-Review", 'comment': 'testcomment'}, header, 'json');
        } catch (e) {
            log.error(e);
        } finally {
            deleteAssetWithID(assetId);
            logoutAuthorizedUser(header);
            expect(response.data.data).toEqual(' State changed successfully to In-Review!');
        }
    });

    /*
     * Endpoint: /publisher/apis/assets/<asset-id>/state?type=gadget
     * Method: POST
     * Response: error message
     * test: error content
     */
    it('Test invoke lifecycle action by id without comment', function () {
        assetID = getAssetID();
        var url = server_url + '/assets/' + assetId + '/state?type=gadget';
        url = encodeURI(url);
        var header = obtainAuthorizedHeaderForAPICall();
        try {
            var response = post(url, {"nextState": "In-Review"}, header, 'json');
        } catch (e) {
            log.error(e);
        } finally {
            deleteAssetWithID(assetId);
            logoutAuthorizedUser(header);
            expect(response.data.error).toEqual(' Please provide a comment for this state transition!');
        }
    });

    /*
     * Endpoint: /publisher/apis/assets/<asset-id>/state?type=gadget
     * Method: POST
     * Response: error message
     * test: error content
     */
    it('Test invoke lifecycle action by non-exsiting id', function () {
        var Id = 'none-exixting-asset'
        var url = server_url + '/assets/' + Id + '/state?type=gadget';
        url = encodeURI(url);
        var header = obtainAuthorizedHeaderForAPICall();
        try {
            var response = post(url, {"nextState": "In-Review", 'comment': 'testcomment'}, header, 'json');
        } catch (e) {
            log.error(e);
        } finally {
            logoutAuthorizedUser(header);
            expect(response.data.error).toEqual('Unable to locate the asset with id: ' + Id);
        }
    });

    /*
     * Endpoint: /publisher/apis/assets/<asset-id>/state?type=gadget
     * Method: POST
     * Response: error message
     * test: error content
     */
    it('Test invoke lifecycle action without nextState', function () {
        assetID = getAssetID();
        var url = server_url + '/assets/' + assetId + '/state?type=gadget';
        url = encodeURI(url);
        var header = obtainAuthorizedHeaderForAPICall();
        try {
            var response = post(url, {'comment': 'testcomment'}, header, 'json');
        } catch (e) {
            log.error(e);
        } finally {
            deleteAssetWithID(assetId);
            logoutAuthorizedUser(header);
            expect(response.data.error).toEqual('Checklist items or next state is not provided!');
        }
    });

    /*
     * Endpoint: /publisher/apis/assets/<id>?type=<type>
     * Method: POST
     * Response: updated asset
     * test: check for a return-asset
     */
    it('Test update assets by id', function () {
        assetID = getAssetID();
        var url = server_url + '/assets/' + assetId + '?type=gadget';
        url = encodeURI(url);
        var header = obtainAuthorizedHeaderForAPICall();
        var asset = {   'overview_description': 'Test rest api testing update',
            'overview_category': 'Template'};
        try {
            var response = post(url, asset, header, 'json');
        } catch (e) {
            log.error(e);
        } finally {
            deleteAssetWithID(assetId);
            logoutAuthorizedUser(header);
            expect(response.data.data).not.toBe(undefined);
            expect(response.data.data.attributes.overview_description).toBe('Test rest api testing update');
            expect(response.data.data.attributes.overview_category).toBe('Template');
        }
    });

    it('Test add comments', function () {
        //TODO
    });

    it('Test add asset version by id', function () {
        //TODO
    });

    it('Test post lifecycle comments', function () {
        //TODO
    });

    it('Test post tags', function () {
        //TODO
    });

});

describe('Assets GET - Publisher API', function () {

    /*
     * Endpoint: /publisher/apis/assets?type=gadget
     * Method: GET
     * Response: asset
     * test: asset name
     */
    it('Test get asset by id', function () {
        var assetID = getAssetID();
        var url = server_url + '/assets/' + assetId + '?type=gadget';
        url = encodeURI(url);
        var header = obtainAuthorizedHeaderForAPICall();
        try {
            var response = get(url, {}, header, 'json');
        } catch (e) {
            log.debug(e);
        } finally {
            deleteAssetWithID(assetId);
            logoutAuthorizedUser(header);
            expect(response.data.data).not.toBe(undefined);
            expect(response.data.data.attributes.overview_name).toEqual('WSO2 Test Gadget');
        }
    });

    /*
     * Endpoint: /publisher/apis/assets?type=gadget
     * Method: GET
     * Response: assets list
     * test: check for default pagination size
     */
    it('Test get assets by type, without pagination', function () {
        var url = server_url + '/assets?type=gadget';
        var header = obtainAuthorizedHeaderForAPICall();
        url = encodeURI(url);
        try {
            var response = get(url, {}, header, 'json');
        } catch (e) {
            log.debug(e);
        } finally {
            logoutAuthorizedUser(header);
            expect(response.data.data).not.toBe(undefined);
            expect(response.data.data.length).toEqual(12);
        }
    });

    /*
     * Endpoint: /publisher/apis/assets?type=gadget
     * Method: GET
     * Response: assets list
     * test: check for pagination, 5 items
     */
    it('Test get assets by type, with pagination', function () {
        var url = server_url + '/assets?type=gadget&start=0&count=5';
        var header = obtainAuthorizedHeaderForAPICall();
        url = encodeURI(url);
        try {
            var response = get(url, {}, header, 'json');
        } catch (e) {
            log.debug(e);
        } finally {
            logoutAuthorizedUser(header);
            expect(response.data.data).not.toBe(undefined);
            expect(response.data.data.length).toEqual(5);
        }
    });

    /*
     * Endpoint: /publisher/apis/assets?type=gadget
     * Method: GET
     * Response: assets list
     * test: number of fields
     */
    it('Test get assets by type, with field expansion for attributes', function () {
        var id = getAssetID();
        var url = server_url + '/assets?type=gadget&fields=overview_name,overview_version,overview_provider';
        url = encodeURI(url);
        var header = obtainAuthorizedHeaderForAPICall();
        try {
            var response = get(url, {}, header, 'json');
        } catch (e) {
            log.debug(e);
        } finally {
            deleteAssetWithID(id)
            logoutAuthorizedUser(header);
            expect(response.data.data).not.toBe(undefined);
            var count = Object.keys(response.data.data[0].attributes).length;
            expect(count).toEqual(3);
        }
    });

    /*
     * Endpoint: /publisher/apis/assets?type=gadget
     * Method: GET
     * Response: assets list
     * test: number of fields
     */
    it('Test get assets by type, with field expansion for outer fields', function () {
        var id = getAssetID();
        var url = server_url + '/assets?type=gadget&fields=name,id,type';
        var header = obtainAuthorizedHeaderForAPICall();
        try {
            var response = get(url, {}, header, 'json');
        } catch (e) {
            log.debug(e);
        } finally {
            deleteAssetWithID(id)
            logoutAuthorizedUser(header);
            expect(response.data.data).not.toBe(undefined);
            var count = Object.keys(response.data.data[0]).length;
            expect(count).toEqual(3);
        }
    });

    /*
     * Endpoint: /publisher/apis/assets?type=gadget
     * Method: GET
     * Response: assets list
     * test: number of fields
     */
    it('Test get assets by type, with field expansion for outer fields and attributes', function () {
        var id = getAssetID();
        var url = server_url + '/assets?type=gadget&fields=name,id,type,overview_version,overview_name';
        var header = obtainAuthorizedHeaderForAPICall();
        try {
            var response = get(url, {}, header, 'json');
        } catch (e) {
            log.debug(e);
        } finally {
            deleteAssetWithID(id);
            logoutAuthorizedUser(header);
            expect(response.data.data).not.toBe(undefined);
            var count = Object.keys(response.data.data[0]).length;
            expect(count).toEqual(4);
            var attributeCount = Object.keys(response.data.data[0].attributes).length;
            expect(attributeCount).toEqual(2);
        }
    });

    /*
     * Endpoint: /publisher/apis/assets?type=gadget
     * Method: GET
     * Response: assets list
     * test: number of fields
     */
    it('Test get assets by type, with field expansion for outer fields with not available fields', function () {
        var id = getAssetID();
        var url = server_url + '/assets?type=gadget&fields=name,id,type,unavailable1,unavailable2';
        var header = obtainAuthorizedHeaderForAPICall();
        try {
            var response = get(url, {}, header, 'json');
        } catch (e) {
            log.debug(e);
        } finally {
            deleteAssetWithID(id);
            logoutAuthorizedUser(header);
            expect(response.data.data).not.toBe(undefined);
            var count = Object.keys(response.data.data[0]).length;
            expect(count).toEqual(3);
        }
    });
    /*
     * Endpoint: /publisher/apis/assets?type=gadget
     * Method: GET
     * Response: assets list
     * test: number of fields
     */
    it('Test get assets by type, with field expansion for outer fields and attributes not available', function () {
        var id = getAssetID();
        var url = server_url + '/assets?type=gadget&fields=name,id,type,overview_version,overview_unavailable';
        var header = obtainAuthorizedHeaderForAPICall();
        try {
            var response = get(url, {}, header, 'json');
        } catch (e) {
            log.debug(e);
        } finally {
            deleteAssetWithID(id);
            logoutAuthorizedUser(header);
            expect(response.data.data).not.toBe(undefined);
            var count = Object.keys(response.data.data[0]).length;
            expect(count).toEqual(4);
            var attributeCount = Object.keys(response.data.data[0].attributes).length;
            expect(attributeCount).toEqual(1);
        }
    });

    /*
     * Endpoint: /publisher/apis/assets?type=gadget
     * Method: GET
     * Response: assets list
     * test: sort assending
     */
    it('Test get assets by type, sort +overview_name', function () {
        var url = server_url + '/assets?type=gadget&sort=+overview_name';
        var header = obtainAuthorizedHeaderForAPICall();
        try {
            var response = get(url, {}, header, 'json');
        } catch (e) {
            log.debug(e);
        } finally {
            logoutAuthorizedUser(header);
            for (var index = 0; index < response.data.data.length; index++) {
                var nextAsset = response.data.data[index + 1];
                if (nextAsset) {
                    var isLessthan = compareStrings(response.data.data[index].name, nextAsset.name, 'isLessThan');
                    expect(isLessthan).toBe(true);
                }
            }
        }
    });

    /*
     * Endpoint: /publisher/apis/assets?type=gadget
     * Method: GET
     * Response: assets list
     * test: sort descending
     */
    it('Test get assets by type, sort -overview_name', function () {
        var url = server_url + '/assets?type=gadget&sort=-overview_name';
        var header = obtainAuthorizedHeaderForAPICall();

        try {
            var response = get(url, {}, header, 'json');
        } catch (e) {
            log.debug(e);
        } finally {
            logoutAuthorizedUser(header);
            expect(response.data.data).not.toBe(undefined);

            for (var index = 0; index < response.data.data.length; index++) {
                var nextAsset = response.data.data[index + 1];
                if (nextAsset) {
                    var isGreaterThan = compareStrings(response.data.data[index].name, nextAsset.name, 'isGreaterThan');
                    expect(isGreaterThan).toBe(true);
                }
            }
        }

    });

    /*
     * Endpoint: /publisher/apis/assets?type=gadget&q="overview_version":"1.0.0"
     * Method: GET
     * Response: assets list
     * test: check version
     */
    it('Test get assets by type, search by overview_version', function () {
        var url = server_url + '/assets?type=gadget&q="overview_version":"1.0.0"';
        url = encodeURI(url);
        var header = obtainAuthorizedHeaderForAPICall();
        try {
            var response = get(url, {}, header, 'json');

        } catch (e) {
            log.debug(e);
        } finally {
            logoutAuthorizedUser(header);
            for (var i in response.data.data) {
                expect(response.data.data[i].attributes.overview_version).toEqual('1.0.0');
            }
        }
    });

    /**
     * This function compares the sort order of two strings according to the given comparator
     * @param str1 Fist String to compare
     * @param str2 Second string to compare
     * @param param Parameter which indicate comparator
     * @return {boolean}
     */
    var compareStrings = function (str1, str2, param) {
        switch (param) {
            case 'isLessThan':
                if (str1.localeCompare(str2) < 0) {
                    return true;
                } else {
                    return false;
                }
                break;
            case 'isGreaterThan':
                if (str1.localeCompare(str2) > 0) {
                    return true;
                } else {
                    return false;
                }
                break;
            case 'isEqual':
                if (str1.localeCompare(str2) == 0) {
                    return true;
                } else {
                    return false;
                }
                break;
            default:
                return false;
        }
    };

});

/**
 * Groups TEST cases for Asset-DELETE endpoints
 */
describe('Assets DELETE - Publisher API', function () {

    /*
     * Endpoint: /publisher/apis/assets?type=gadget
     * Method: GET
     * Response: asset
     * test: asset name
     */
    it('Test delete asset by id', function () {
        assetId = getAssetID();
        var url = server_url + '/assets/' + assetId + '?type=gadget';
        var header = obtainAuthorizedHeaderForAPICall();
        try {
            var response = del(url, {}, header, 'json');
        } catch (e) {
            log.debug(e);
        } finally {
            logoutAuthorizedUser(header);
            expect(response.data.data).toEqual('Asset Deleted Successfully');
        }
    });

});

/**
 * To add a asset and return the retrieved id of newly added asset
 * @return uuid
 */
var getAssetID = function () {
    var url = server_url + '/assets?type=gadget';
    var asset = {'overview_name': 'WSO2 Test Gadget',
        'overview_version': '1.2.3',
        'overview_provider': 'admin',
        'overview_description': 'initial description',
        'overview_category': 'Google'};
    var header = obtainAuthorizedHeaderForAPICall();
    var response;
    try {
        response = post(url, asset, header, 'json');
    } catch (e) {
        log.error(e);
    } finally {
        assetId = response.data.data.id;
        logoutAuthorizedUser(header);
        expect(response.data.data).not.toBe(undefined);
    }
    return response.data.data.id;
};

/**
 *
 * @return {{Cookie: string}}
 */
var obtainAuthorizedHeaderForAPICall = function () {
    var authenticate = post(server_url + '/authenticate', {"password": password, "username": username }, {}, 'json');
    var header = {'Cookie': "JSESSIONID=" + authenticate.data.data.sessionId + ";"};
    return header
};

/**
 * The function to send logout request to publisher API
 * @param header
 */
var logoutAuthorizedUser = function (header) {
    post(server_url + '/logout', {}, header, 'json');
};

/**
 * This function will send delete request for given asset id
 * @param id The uuid of asset to be deleted
 */
var deleteAssetWithID = function (id) {
    var url = server_url + '/assets/' + id + '?type=gadget';
    var header = obtainAuthorizedHeaderForAPICall();
    var response;
    try {
        response = del(url, {}, header, 'json');
    } catch (e) {
        log.debug(e);
    } finally {
        logoutAuthorizedUser(header);
        expect(response.data.data).toEqual('Asset Deleted Successfully');
    }
};
        
