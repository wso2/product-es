#Introduction
The Publisher app is the asset management arm of the Enterprise Store.It allows an asset publisher and administrators to;
1. To create instances of new assets
2. To update instances of existing assets
2. Manage the lifecycle states of the assets

This document highlights the basic structure of the app along with some of the basic customization options that can be carried out.



#Contents
This document will be broken down into the following sections;
1.Structure of the app

#Dependencies
The app depends on the following Jaggery modules;

1. pipe
2. pipe-commons
3. entity
4. router
5. utils

#Structure of the app
The app directory is broken down into several sub directories;

1. configs
2. themes
3. modules
4. packages
5. i18n

We will be taking a look at the purpose of each sub directory in more detail in the next few sections.

#Packages
Packages are bits of self contained logic and resources that provided or override some functionality of the application.There are two distinct types of packages;

1.Global
2.Tenant

The Global packages define any features that are 

###Structure of a Package
Any folder can become a package provided it has the following json file;

```json
  {
    "name":"package_name",
    "version":"1.0.0",
    "description":"Purpose_of_your_app"
  }
```

The above two properties are the only properties that you need to define a package.

You can also define a package to consume another package;

```json
  {
    "name":"child_package_name",
    "version":"1.0.0",
    "description":"This_is_a_child_package",
    "consumes":["package_name"]
  }
```

The package script initializes logic of this package after the parent package.This allows tenant packages to override global routes.

If you need more granualrity as to how your package is started up, then you can also define;

```json
   "main":"main.js"
```

The file defined by the main property will be executed when your package is read for the first time.

####Organizing your package
Although you are free to add any directory within the package the following names are reserved;

1. routes
2. config
3. themes
4. modules
5. widgets

####How does a package work?
When a package is read the package management script will read any sub folders defined in the directory and trigger predefined logic based on the folder name.You can define your own logic to execute when a package is read by ;

```javascript
  var myPackageReader=function(fiber,options){
  
    fiber.events.on('routes','init',function(context){
    });
    
  };
```

#Adding your own asset
The Publisher can be configured to support any asset type defined by an RXT file. All new assets must be placed inside the packages/globals/extensions directory.

#Customizing the Publisher for a tenant
The Publisher can be configured with tenant specific customizations.All tenant specific customizations should be placed in the packages/tenants directory.

###How does it work?
When the Publisher loads for a given tenant, the app will look for a directory with the tenant id.If one is present then any packages specified in this directory are loaded.

####Adding functionality
The Publisher can be augmented with new functionality on a per tenant basis.

If you need to customize the Publisher on a per tenant basis then this logic should be placed in the /packages/tenant/{your_tenant_id} directory.

####Overriding a global functionality
A tenant customization can also change the way a global customization could work for a 
