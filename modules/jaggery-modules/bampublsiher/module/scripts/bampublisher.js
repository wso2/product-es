/**
 * Following module act as a client to publish events to BAM
 * @type {{}}
 */

var bamclient = {};

(function (bamclient) {

    var bamPublsiher = Packages.org.wso2.stroe.bamclient,
        carbon = require('carbon'),
        log = new Log();

    var eventStreamDefinition = '{"name":"asseteventsStream","version":"1.0.0","nickName":"asseteventsStream","description":"assets events stream","metaData":["name":"clientType","type":"STRING"],"payloadData":[{"name":"event","type":"STRING"},{"name":"assetId","type":"STRING"},{"name":"assetType","type":"STRING"},{"name":"assetName","type":"STRING"},{"name":"description","type":"STRING"}]}';

     bamclient.publishEvents = function (eventName, assetId, assetType,assetName,decription) {
        var streamdefObj = parse(eventStreamDefinition);
        bamPublsiher.getInstance().publishEvents(streamdefObj.name,streamdefObj.version, eventStreamDefinition, streamdefObj.metaData,{eventName,assetId,assetType, assetName, decription});
    };

}(bamclient));