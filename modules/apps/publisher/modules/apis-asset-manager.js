/*
 Descripiton:The apis-asset-manager is used to retriew assets for api calls
 Filename: apis-asset-manager.js
 Created Date: 7/24/2014
 */

var createAsset = function(options, req, res, session) {
        var asset = require('rxt').asset;
        var am = asset.createUserAssetManager(session, options.type);
        var assetReq = req.getAllParameters('UTF-8');
        var asset = am.importAssetFromHttpRequest(assetReq);
        log.info('adding asset');
        try{
            am.create(asset);
        }
        catch(e){
            log.error('Asset of type: '+options.type+' was not created due to '+e);
            res.sendError(500,'Failed to create asset of type: '+options.type);
            return;
        }

        var isLcAttached = am.attachLifecycle(asset);
        //Check if the lifecycle was attached
        if (isLcAttached) {
            var synched = am.synchAsset(asset);
            if (synched) {
                am.invokeDefaultLcAction(asset);
                log.info('Finished invoking default action');
            } else {
                log.warn('Failed to invoke default action as the asset could not be synched.')
            }
        }
    };

    var updateAsset = function(options, req, res, session) {
        log.info('updating asset');
        var asset = require('rxt').asset;
        var am = asset.createUserAssetManager(session, options.type);
        var assetReq = req.getAllParameters('UTF-8');
        var asset = am.importAssetFromHttpRequest(assetReq);
        asset.id = options.id;
        am.update(asset);
    };

    var fieldExpansion = function(options, req, res, session) {
        var fields = options.fields;  
         
        var artifacts = options.assets;
        var extendedAssetTemplate ={};

        for(var val in fields){
            var key = fields[val];
            var value = '';
            extendedAssetTemplate[key]=value;          

        }    
        var newArtifactTemplateString = stringify(extendedAssetTemplate);       
        var modifiedArtifacts = [];
       
        for (var j in artifacts) {            
            var artifactObject = parse(newArtifactTemplateString);
            for(var i in extendedAssetTemplate){      
                artifactObject[i] = artifacts[j].attributes[i];                                      
            }                    
            modifiedArtifacts.push(artifactObject);                      
        }
        print(modifiedArtifacts);
        return;
    };


    var listAssets = function(options, req, res, session) {
        var assetManager = asset.createUserAssetManager(session, options.type); 
        var sort = (request.getParameter("sort")||'');
            
        var sortOrder =DEFAULT_PAGIN.sortOrder;

        if(sort){
            var order = sort.charAt(0);
            if(order == '+'|| order == ' '){
                sortOrder = 'asc';
                sort = sort.slice( 1 );

            }else if(order == '-'){
                sortOrder = 'desc';
                sort = sort.slice( 1 );

            }else{
                sortOrder = DEFAULT_PAGIN.sortOrder;
            }          
        }
        
        var sortBy = (sort||DEFAULT_PAGIN.sortBy);
        
        var count = (request.getParameter("count")||DEFAULT_PAGIN.count);
        var start = (request.getParameter("start")||DEFAULT_PAGIN.start);
        var paginationLimit =(request.getParameter("paginationLimit")|| DEFAULT_PAGIN.paginationLimit);
        var paging ={'start': start,
                    'count': count,
                    'sortOrder': sortOrder,
                    'sortBy': sortBy,
                    'paginationLimit': paginationLimit};

        var q = (request.getParameter("q")||'');
        
        try {
            if(q){                   
                var qString ='{'+q+'}';                    
                var query=parse(qString);
                //print(query);
                var assets = assetManager.search(query, paging); //doesnt work properly
                           
            }else{
                //print(paging);
                var assets = assetManager.list(paging);
            }                                
            var expansionFields = (request.getParameter('fields')||'');           
            if(expansionFields){

                options.fields = expansionFields.split(',');
                options.assets = assets;       
                fieldExpansion(options, req, res, session);
                //return;                    
            }else{
                print(assets);
                return assets;
            }                  

        } catch (e) {
            res.sendError(400, "Your request is malformed");
            log.info(e);
        }
    };

    var getAsset = function(options, req, res, session) {
        var assetManager = asset.createUserAssetManager(session, options.type); 
        try{            
            log.info('--------------------------------------_____________');              
            var retrievedAsset = assetManager.get(options.id);

            if (!retrievedAsset) {
                print({ error: "no matching asset found" });
                res.sendError(400, "no matching asset found");
                return;
            }
            else {
                var expansionFields = (request.getParameter('fields')||'');
                if(expansionFields){
                    options.fields = expansionFields.split(',');
                    var assets = [];
                    assets.push(retrievedAsset);
                    options.assets = assets;      
                    fieldExpansion(options, req, res, session);
                                        
                }
                else{
                    print(retrievedAsset);
                    return retrievedAsset;
                }
                
            }
        }catch(e){
            res.sendError(400, "No matching asset found");
            log.info(e);
        }

    
    };