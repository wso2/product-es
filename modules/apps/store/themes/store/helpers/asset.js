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
var resources = function (page, meta) {
    return {
        js: ['jquery.MetaData.js', 'jquery.rating.pack.js', 'async.min.js', 'asset-core.js', 'asset.js', 'moment.min.js', 'porthole.min.js'],
        css: ['jquery.rating.css', 'asset.css'],
        code: ['store.asset.hbs']
    };
};

var format = function (context) {
    //adding enriched context for paginating template
    var log = new Log();
    var avg = context.asset.rating.average;
   
    if(context.type === 'gadget') {
        context.asset_css = "cog";

    }else if (context.type === 'site'){
        context.asset_css = "globe";

    }else if(context.type === 'ebook'){
        context.asset_css = "book";

    }else{
        context.asset_css = "link";
    }
    
    context.asset.rating.ratingPx = ratingToPixels(avg);
    
    return context;
};

var formatRatings = function(context){
	var avg;
	for(var i in context){
		avg = context[i].rating;
		context[i].ratingPx = ratingToPixels(avg);
	}

	return context;
}

var formatAssetFromProviderRatings=function(context){
    var avg;
    var assets=context.assets;
    for(var i in assets){
        avg = assets[i].rating.average;
        assets[i].ratingPx = ratingToPixels(avg);
    }

    return context;
};

var ratingToPixels = function(avg) {
	var STAR_WIDTH = 84;
	var MAX_RATING = 5;

	var ratingPx = (avg / MAX_RATING) * STAR_WIDTH;
	return ratingPx;
}

