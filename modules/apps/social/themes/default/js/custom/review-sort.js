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
var $stream = $stream || $('#stream');

var didILike = function (review, username) {
    var likes = review.likes && review.likes.items;
    if (likes) {
        for (var j = 0; j < likes.length; j++) {
            var like = likes[j];
            if (like.id == username) {
                return true;
            }
        }
    }
    return false;
};

var usingTemplate = function (callback) {
    caramel.partials({activity: 'themes/' + caramel.themer + '/partials/activity.hbs'}, function () {
        callback(Handlebars.partials['activity']);
    });
};

var redrawReviews = function (sortBy, callback) {
    $('.com-sort .selected').removeClass('selected');
    $.get('apis/object.jag', {
        target: target,
        sortBy: sortBy
    }, function (obj) {
        var reviews = obj.attachments || [];
        usingTemplate(function (template) {
            var str = "";
            for (var i = 0; i < reviews.length; i++) {
            	//Remove carbon.super tenant domain from username
                var user = reviews[i].actor.id;
                var pieces = user.split(/[\s@]+/);
                if(pieces[pieces.length-1] == 'carbon.super'){
                	reviews[i].actor.id= pieces[pieces.length-2];
                }
                var review = reviews[i];
                var iLike = didILike(review, user);
                review.iLike = iLike;
                console.log(iLike);
                str += template(review);
            }
            $stream.html(str);
            callback && callback();
            adjustHeight();
        });
    })
};

$(document).on('click', '.com-sort a', function (e) {
    var $target = $(e.target);
    if (!$target.hasClass('selected')) {
        redrawReviews($target.text().toUpperCase());
        $target.addClass('selected');
    }
});

