WSO2 Enterprise Store Asset upload client
----------------------

 How to upload assets to publisher

1. Create asset meta data in a json format and store in sample folder.
2. The meta data should store in a .json file.
3. In a json file can store multiple assets.
4. Sample format of a json as follows.


   {
  "assets":
    [
        {
            "attributes": {
                "overview_provider": "wso2",
                "overview_name": "test_overview1",
                "overview_version": "1.0",
                "overview_createdtime": "",
                "overview_category": "Google",
                "overview_url": "yahoo.com",
                "overview_description": "test1",
                "images_thumbnail": "thumbnail.png",
                "images_banner": "banner.png"
            },
            "name": "asset1",
            "type": "gadget"
        },
        {
            "attributes": {
                "overview_provider": "wso2",
                "overview_name": "test_overview2",
                "overview_version": "1.0",
                "overview_createdtime": "",
                "overview_category": "Google",
                "overview_url": "yahoo.com",
                "overview_description": "test2",
                "images_thumbnail": "thumbnail.png",
                "images_banner": "banner.png"
            },
            "name": "asset2",
            "type": "gadget"
        }
    ]
} 


5. Images and physical files should store in a folder called resources in the same level which exists the json file.
6. Inside the resource folder, the physical files should be store with the same name in the json file.
   
   Ex -: image_thumbnail is thumbnail.png. Its find a physical file called thumbnail.png.

7. Possible to store in a folder structure under sample folder.

   Ex -: sample folder can have sub folders for each type. suppose gadgets, ebooks, apis etc
         Inside each folder can have meta data json file and resource folder for that meta data file.

8. Inside meta data json file should maintain same attributes names which exists in the rxt. type attribute should be rxt type.

9. To publish assets, in the command prompt go to ES_HOME/sample
   ant publish



---------------------------------------------------------------------------
(c) Copyright 2013 WSO2 Inc.
