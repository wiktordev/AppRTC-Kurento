## AppRTC - Kurento Example

This is a simple example project in nodejs and j2ee to demonstrate the compatibility of the [AppRTCDemo](https://github.com/njovy/AppRTCDemo) Android App with the [Kurento Media Server](http://www.kurento.org/).

See for implementations in the named platform folder.
This version doesn't have a servlet for the Android version and works together with a modified, pure websocket version of AppRTC for Android see: 

pure Websocket AppRTC for Android: https://github.com/inspiraluna/AppRTCDemo 

###Documentation:
-----------------
- Kurento Java Tutorial http://doc-kurento.readthedocs.io/en/stable/tutorials/java/tutorial-one2one-adv.html


###Todo:
- config parameter in Ressource Bundle or property files 
	http://stackoverflow.com/questions/531593/how-to-use-a-property-file-with-glassfish
	
- fix logging slf4j for maven
- recording 
- screensharing  
		http://doc-kurento.readthedocs.io/en/stable/mastering/kurento_utils_js.html
- widget for browser 
		http://shootitlive.com/2012/07/developing-an-embeddable-javascript-widget/


###Done
- 2016-08-01 - login user (websocket session id + username) and 
- 2016-08-01 - when websocket closes user is not deleted from registry
- 2016-07-09 - get list of logged in users for android, ios and browser 
- 2016-06-28 - implement simple call response within browser use kurento one2one-call example
- 2016-06-28 - android version works on websocket only / port android project 
