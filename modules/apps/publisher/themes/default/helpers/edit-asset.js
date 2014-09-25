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
