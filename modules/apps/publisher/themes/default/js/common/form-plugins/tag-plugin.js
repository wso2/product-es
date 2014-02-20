$(function(){

    var DEFAULT_TAG_THEME='facebook';
    var ALLOW_FREE_TAG=false;


    function TagPlugin(){
       this.tagContainer='';
    }

    TagPlugin.prototype.init=function(element){
       var tagUrl=element.meta.tagApi;
       this.tagContainer='#'+element.meta.tagContainer||element.id;
       if(!tagUrl){
           console.log('Unable to locate tag api url');
           return;
       }
       fetchInitTags(tagUrl,tagContainer);
    };

    TagPlugin.prototype.getData=function(element){
        var data={};
        var tags;
        var selectedTags;

        selectedTags=$(this.tagContainer).tokenInput('get');

        for(var index in selectedTags){
            tags.push(selectedTags[index].name);
        }

        data[element.id]=JSON.stringify(tags);

        return data;
    }

    var fetchInitTags=function(tagUrl,tagContainer){

        //Obtain all of the tags for the given asset type
        $.ajax({
            url : tagUrl,
            type : 'GET',
            success : function(response) {
                var tags = JSON.parse(response);
                $(tagContainer).tokenInput(tags, {
                    theme : DEFAULT_TAG_THEME,
                    allowFreeTagging :ALLOW_FREE_TAG
                });

            },
            error : function() {
                console.log('unable to fetch tag cloud for ' + type);
            }
        });
    }

    FormManager.register('TagPlugin',TagPlugin);
});