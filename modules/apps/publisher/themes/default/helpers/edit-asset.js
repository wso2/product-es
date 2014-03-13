var resources = function(page, meta) {
	var log = new Log('edit-asset');
	log.debug('resource called');
	return {
		js : ['common/form-manager.js','common/form-plugins/common-plugins.js','common/form-plugins/options-text-plugin.js','common/form-plugins/tag-plugin.js','common/form-plugins/unbound-table-plugin.js','logic/asset/edit-asset.js','logic/asset.tag.edit.js'], //['edit.asset.js', '/logic/asset.tag.edit.js', 'bootstrap-select.min.js','options.text.js'],
		css : ['bootstrap-select.min.css']
	};

};

var selectCategory = function(data) {
	var selected, 
		arr=[],
		currentCategory = data.artifact.attributes['overview_category'],
		categories = selectCategories(data.data.fields);

	for (var i in categories) {
		
		selected = (currentCategory == categories[i])?true:false;
		arr.push({
			cat:categories[i],
			sel:selected
			});
	}
	data.categorySelect = arr;
	return data;
}
var selectCategories = function(fields) {
	for (var i in fields) {	
		if(fields[i].name == "overview_category"){
			return fields[i].valueList;
		}
	}
}
