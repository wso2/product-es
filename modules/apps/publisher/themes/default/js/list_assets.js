var currentPage = 1, infiniteScroll = true, itemsForPage = 10;
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


	function getNextPage(param){
		var assetType = store.publisher.type;//$('#meta-asset-type').val();
		var url = '/publisher/apis/assets?type='+ assetType + param;
		
		$.ajax({
          url: url,
          type: 'GET',
          success: function(response) {
            var assets = convertTimeToUTC(response.data);        
               
            if(response.data){
		    	renderView('list_assets_table_body',assets,'#list-asset-table-body');
			}

           	if(response.data.length < $('#initial-num-of-items').val()){
	           	$('.loading-inf-scroll').hide();
	           	$(window).unbind('scroll', scroll);
	           	infiniteScroll = false;
            }else{
            	infiniteScroll = true;
            }
          },
          error: function(response) {
             $('.loading-inf-scroll').hide();
             $(window).unbind('scroll', scroll);
             infiniteScroll = false;
          }
        });        
	}

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

	var scroll = function() {
		var startInitItems = store.publisher.itemsPerPage;
		if(infiniteScroll) {
			if($(window).scrollTop() + $(window).height() >= $(document).height()*.5) {			
				var start = startInitItems*(currentPage++);				
				var path = window.location.href;				
				var param = '&&start='+start+'&&count='+startInitItems+setSortingParams(path);				
				
				getNextPage(param);
				
				$(window).unbind('scroll', scroll);
				infiniteScroll = false;
				setTimeout(function() {
					$(window).bind('scroll', scroll);
					
					$('.loading-inf-scroll').show();
				}, 1000);
			}
		} else {
			$('.loading-inf-scroll').hide();
		}
		
	}

	$(window).bind('scroll', scroll);