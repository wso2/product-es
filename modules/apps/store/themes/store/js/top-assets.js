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

//TODO: add delay before opening more details
/*
 var timer;
 var details;
 ;
 */
var opened = false;

$(function() {
	var details;

	$(document).on('click', '.assets-container .asset-add-btn', function(event) {
		var parent = $(this).parent().parent().parent();
		asset.process(parent.data('type'), parent.data('id'), location.href);
		event.stopPropagation();
	});

	$(document).on('click', '.asset > .asset-details', function(event) {
		var link = $(this).find('.asset-name > a').attr('href');
		location.href = link;
	});

	mouseStop();
	applyTopAssetsSlideshow();

	$(".assetSlider").carouFredSel({
		items : 4,
		width : "100%",
		infinite : false,
		auto : false,
		circular : false,
		pagination : ".assetSliderPag"
	});
});

var mouseStop = function() {
	$('.asset').bind('mousestop', 300, function() {
		//console.log("In");
		bookmark = $(this).find('.store-bookmark-icon');
		bookmark.animate({
			top : -200
		}, 200);
		details = $(this).find('.asset-details');
		details.animate({
			top : 0
		}, 200);
		opened = true;
	}).mouseleave(function() {
		//console.log("out");
		bookmark = $(this).find('.store-bookmark-icon');
		bookmark.animate({
			top : -4
		}, 200);
		opened = opened && details.stop(true, true).animate({
			top : 200
		}, 200) ? false : opened;
	});
}

var applyTopAssetsSlideshow = function(){
	var visible,
		size =  $('#asset-slideshow-cont').find('.slide').size();

	if(size<=3){
		visible = 1;
		$('#asset-slideshow-cont .html_carousel').css('margin-left', 163);
	} else {
		visible = 3;
	}

	$("#asset-slideshow").carouFredSel({
		items : {
			visible : visible
		},
		height : 300,
		scroll : 1,
		auto : true,
		prev : {
			button : "#asset-slideshow-next",
			key : "left"
		},
		next : {
			button : "#asset-slideshow-prev",
			key : "right"
		}

	}).find(".slide").hover(function() {
		$(this).find(".asset-intro-box").slideDown("fast");
	}, function() {
		$(this).find(".asset-intro-box").slideUp("fast");
	});
}
