var core={};

(function(){

	var DEFAULT_MEDIA_TYPE='application/vnd.wso2.registry-ext-type+xml';
	var ASSET_PATH='/_system/governance/repository/components/org.wso2.carbon.governance/types/';
	var GovernanceUtils=Packages.org.wso2.carbon.governance.api.util.GovernanceUtils;
	var utils=require('utils');

	function RxtManager(registry){
		this.registry=registry;
		this.rxtMap={};
	}

	RxtManager.prototype.load=function(){
		var rxtPaths=GovernanceUtils.findGovernanceArtifacts(DEFAULT_MEDIA_TYPE,
			this.registry);
		var content;
		var rxtDefinition;

		for(var rxtPath in rxtPaths){
			content=this.registry.get(rxtPath);
			rxtDefinition=utils.xml.convertE4XtoJSON(createXml(content));
			rxtMap[rxtDefinition.type]=rxtDefinition;
		}
	};

	RxtManager.prototype.get=function(rxtType){
		if(this.rxtMap[rxtType]){
			return this.rxtMap[rxtType];
		}
		throw "Unable to locate rxt type: "+rxtType;
	};

	/*
	Creates an xml file from the contents of an Rxt file
	@rxtFile: An rxt file
	@return: An xml file
	*/
	function createXml(rxtFile){
		var content=rxtFile.content.toString();
		
		
		var fixedContent=content.replace('<xml version="1.0"?>',EMPTY)		
					 .replace('</xml>',EMPTY);
		return new XML(fixedContent);
	}

	core.RxtManager=RxtManager;

}());