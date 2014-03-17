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
6. caramel-view-engine
7. fiber

#Structure of the app
The Publisher app structure is broken down into several folders;

1.apis
2.assets
3.components
4.configs
5.controllers
6.entities
7.extensions
8.i18n
9.modules
10.plugins
11.tenants
12.themes
13.package.json


Each of the above folders is considered a package.A package is simply some piece of logic that needs to be activated when an app is initialized.

#Start up flow
The app goes through the following steps when it is initialized;

1. The fiber script will look for a package.json file in the root directory to determine which packages need to be deployed (if a package.json file is not found a warning will be given and the app will not load)
2. The fiber script will scan all packages defined in the package.json file and create a map of all available packages and sub packages
3. It will then step through each package and deploy the files defined in the require property


