Authenticating
--------------
curl --insecure --request  POST "https://localhost:9443/publisher/apis/authenticate" --form 'username=admin' --form 'password=admin'


Obtaining All Gadgets
---------------------
curl --insecure --request  GET "https://localhost:9443/publisher/apis/assets?type=gadget" \ -v --cookie 'JSESSIONID=626DD32A927328E651F4C5BA623E6A85'


Obtaining All Gadgets with field expansion
---------------------
curl --insecure --request  GET "https://localhost:9443/publisher/apis/assets?type=gadget&fields=id,name,overview_url" \ -v --cookie 'JSESSIONID=626DD32A927328E651F4C5BA623E6A85'


Search Gadgets with 
---------------------
curl --insecure --request  GET "http://localhost:9763/publisher/apis/assets?type=gadget&q=%22overview_name%22:%22Pie%20Chart%22" \ -v --cookie 'JSESSIONID=626DD32A927328E651F4C5BA623E6A85'


Obtaining All Gadgets with pagination
---------------------
curl --insecure --request  GET "https://localhost:9443/publisher/apis/assets?type=gadget&start=5&count=3" \ -v --cookie 'JSESSIONID=626DD32A927328E651F4C5BA623E6A85'


Getting details of a single gadget
----------------------------------
curl --insecure --request  GET "https://localhost:9443/publisher/apis/assets/3909fc66-26ce-405d-b943-3d57d040cab1?type=gadget" \ -v --cookie 'JSESSIONID=626DD32A927328E651F4C5BA623E6A85'


Adding an asset
---------------
curl --insecure --request  POST "https://localhost:9443/publisher/apis/assets?type=gadget" --form 'overview_name=rest-admin-gadget-3' --form 'overview_provider=admin' --form 'overview_version=1.0.0' \ -v --cookie 'JSESSIONID=626DD32A927328E651F4C5BA623E6A85'

This will return the asset that is created


Updating an asset
---------------
curl --insecure --request  POST "https://localhost:9443/publisher/apis/assets/86d9d352-a293-41bc-9a92-df3c6414ffaf?type=gadget" --form 'overview_description=test gadget' --form 'overview_url=www.example.com' --form 'overview_category=Template' \ -v --cookie 'JSESSIONID=626DD32A927328E651F4C5BA623E6A85'

This will return the asset that is updated


Promoting an asset using SampleLifecycle2
-----------------------------------------
curl --insecure --request  POST "https://localhost:9443/publisher/apis/assets/86d9d352-a293-41bc-9a92-df3c6414ffaf/state?type=gadget" --form 'nextState=In-Review' --form 'comment=ok' \ -v --cookie 'JSESSIONID=626DD32A927328E651F4C5BA623E6A85'

Get the current state of an asset 
-----------------------------------------
curl --insecure --request  GET "https://localhost:9443/publisher/apis/assets/86d9d352-a293-41bc-9a92-df3c6414ffaf/state?type=gadget" \ -v --cookie 'JSESSIONID=626DD32A927328E651F4C5BA623E6A85'

Delete an asset
-----------------
curl --insecure --request  DELETE "https://localhost:9443/publisher/apis/assets/86d9d352-a293-41bc-9a92-df3c6414ffaf?type=gadget" \ -v --cookie 'JSESSIONID=626DD32A927328E651F4C5BA623E6A85'

Lifecycle List
-----------------------------------
curl --insecure --request  GET "https://localhost:9443/publisher/apis/lifecycles" \ -v --cookie 'JSESSIONID=626DD32A927328E651F4C5BA623E6A85'

Lifecycle definition by name
-----------------------------------
curl --insecure --request  GET "https://localhost:9443/publisher/apis/lifecycles/MobileAppLifeCycle" \ -v --cookie 'JSESSIONID=626DD32A927328E651F4C5BA623E6A85'

Lifecycle state details
-----------------------------------
curl --insecure --request  GET "https://localhost:9443/publisher/apis/lifecycles/MobileAppLifeCycle/Created" \ -v --cookie 'JSESSIONID=626DD32A927328E651F4C5BA623E6A85'


logout
--------------------
curl --insecure --request  POST "https://localhost:9443/publisher/apis/logout" \ -v --cookie 'JSESSIONID=626DD32A927328E651F4C5BA623E6A85'

