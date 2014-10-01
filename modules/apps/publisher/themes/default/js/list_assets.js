/*
* *
* * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
* *
* * Licensed under the Apache License, Version 2.0 (the "License");
* * you may not use this file except in compliance with the License.
* * You may obtain a copy of the License at
* *
* * http://www.apache.org/licenses/LICENSE-2.0
* *
* * Unless required by applicable law or agreed to in writing, software
* * distributed under the License is distributed on an "AS IS" BASIS,
* * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* * See the License for the specific language governing permissions and
* * limitations under the License.
*
*/

/**
 * Description: Pagination
 *              Function 'scroll' bind to the UI event sroll.
 *              Requests next set of assets by call ling API endpoint /publisher/apis/assets?type=<type>&sort=<sort-by-attribute>&start=<number-of-alredy-rendered-assets>&count=<number-of-assets-per-page>
 *              Renders retrived set of assets by calling caramal client
 *              if no-no-more assets to be retrived, unbind 'scroll'
 */


var currentPage = 1, infiniteScroll = true;

/**
 * To render the next set of assets by appending to the available container
 * @param {[type]} partial  : to which partial should be added
 * @param {[type]} data     : data for the partial
 * @param {[type]} container: container to be appended
 * @param {[type]} cb       : callback function if any
 */

function renderView(partial, data, container, cb) {		
		var obj = {};
		obj[partial] = '/themes/default/partials/' + partial + '.hbs';
		delete data.content;

		caramel.partials(obj, function () {
			var template = Handlebars.partials[partial](data);		
				$(container).append(template);

				if (cb) {
					cb();
				}
		});
}

/**
 * To convert asset.attributes.overview_createdtime to UTC string
 * @param {[type]} assets  : asset object list
 */
function convertTimeToUTC(assets){		
		 for (var index in assets) {
                var asset = assets[index];
                if(asset.attributes.overview_createdtime) {
                    var value = asset.attributes.overview_createdtime; 
                    var date = new Date();
                    date.setTime(value);
                    asset.attributes.overview_createdtime = date.toUTCString();
                }
            }
		return assets;
}

/**
 * Return next set of assets for the next page by calling assets API endpoint
 * @param {[type]} param  : string of parameters for the api call
 */
function getNextPage(param){
		var assetType = store.publisher.type;//load typr from store global object
		var url = '/publisher/apis/assets?type='+ assetType + param; // build url for the endpoint call
		
		// call endpoint 
		$.ajax({
          url: url,
          type: 'GET',
          success: function(response) {//on success
            var assets = convertTimeToUTC(response.data);     
            if(assets){
		    	renderView('list_assets_table_body',assets,'#list-asset-table-body');

		    	if(assets.length < store.publisher.itemsPerPage){// if no more assets for the next page
		           	infiniteScroll = false;
		           	$('.loading-inf-scroll').hide();

	            }else{
	            	infiniteScroll = true;
	            }
			}
			else{//if no assets retrived for this page
	           	infiniteScroll = false;
            }
           	
          },
          error: function(response) {//on error
             $('.loading-inf-scroll').hide();
             $(window).unbind('scroll', scroll);
             infiniteScroll = false;
          }
        });        
}

/**
 * Build sorting parameters based on page path
 * @param {[type]} path  : string
 */
var setSortingParams = function(path){
		var obj = path.split('?');		
		var sorting='';
		if(obj[1]){
			var temp = obj[1].split('&');
			var sortby = temp[0].split('=')[1];
			var sort=temp[1].split('=')[1];
		}else{
	    	sort = 'DESC';
	    	sortby = 'overview_createdtime';
	    }	
		if(sort=='DESC'){
			 sorting = '&&sort=-'+sortby;
		}else{
			sorting ='&&sort=+'+sortby;
		}

		return sorting;
}

/**
 * scroll method bind to be scroll window function
 * 
 */
var scroll = function() {
		var startInitItems = store.publisher.itemsPerPage;// items-per-page by global store object
		if(infiniteScroll) { //if scrolll enebled
			if($(window).scrollTop() + $(window).height() >= $(document).height()*.5) {		
				var start = startInitItems*(currentPage++);				
				var path = window.location.href;//current page path				
				var param = '&&start='+start+'&&count='+startInitItems+setSortingParams(path);				
				
				getNextPage(param);// get next set of assets
				$('.loading-inf-scroll').hide();
				$(window).unbind('scroll', scroll);
				infiniteScroll = false;
				setTimeout(function() {	
					if(infiniteScroll){				
						$(window).bind('scroll', scroll);					
						$('.loading-inf-scroll').show();
					}
				}, 500);
			}
		} else {// if infinite scroll is not enabled
			$('.loading-inf-scroll').hide();
		}
		
}
// bind to window function
$(window).bind('scroll', scroll);

$( window ).load(function() {
  		scroll();
});