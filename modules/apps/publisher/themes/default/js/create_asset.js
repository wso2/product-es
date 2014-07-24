$(function(){
	var obtainFormMeta=function(formId){
		return $(formId).data();
	};
	$(document).ready(function(){

		$('#form-asset-create').ajaxForm({
			success:function(){
				var options=obtainFormMeta('#form-asset-create');
				//alert('Aww snap! '+JSON.stringify(options));
				window.location=options.redirectUrl;
			},
			error:function(){
				alert('Unable to add the asset');
			}
		});
	});

});