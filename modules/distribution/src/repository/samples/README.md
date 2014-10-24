#Introduction
This document will provide a description of the test_manifest.json file

#Purpose
It is often the case that resources must be copied to a directory prior to running a set of tests.
These resources may need to be present in the pack prior to starting the ES server.The test_manifest.json
allows a set of file copy rules to be specified by test developers.It is guaranteed that these resources
will be available in the designated destination locations prior to starting the server.

#Instructions

##Copying a resource
A resource can be copied by specifying a resource rule as follows:

{
	src:"some resource location"
	destination:"some destination location"
}

##Organzing copying rules
For the sake of promoting readability the resource copying rules should be organized into packages, with
each package copying a set of related resources.This is shown in the following code snippet:

"servicex_sample": {
            "resources": [{
                "src": "samples/extension_samples/servicex_sample/publisher/assets/servicex",
                "dest": "repository/deployment/server/jaggeryapps/publisher/extensions/assets/servicex"
            }, {
                "src": "samples/extensions_samples/servicex_sample/store/assets/servicex",
                "dest": "repository/deployment/server/jaggeryapps/store/extensions/assets/servicex"
            }, {
                "src": "samples/extensions_samples/servicex_sample/publisher/config/publisher-tenant.json",
                "dest": "repository/deployment/server/jaggeryapps/publisher/config/publisher-tenant.json"
            }]
}



