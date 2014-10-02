var updateSortUI = function() {
	$('#ul-sort-assets').find('a[data-sort="' + store.asset.paging.sort + '"]').addClass('selected-type');
	store.asset.count = 0;
};

$(function() {
	updateSortUI();

	$('#breadcrumb > div').live('click', function(e) {
		$(this).toggleClass('breadcrumb-sel').find('.breadcrumb-body').toggle();
		$('#breadcrumb > div').not(this).removeClass('breadcrumb-sel').find('.breadcrumb-body').hide();
		e.stopPropagation();
	});
	
	$(document).click(function(){
		$('.breadcrumb-body').hide();
	});

}); 