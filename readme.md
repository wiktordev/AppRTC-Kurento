## AppRTC - Kurento Example

This is a simple example project in nodejs and j2ee to demonstrate the compatibility of the [AppRTCDemo](https://github.com/njovy/AppRTCDemo) Android App with the [Kurento Media Server](http://www.kurento.org/).

See for implementations in the named platform folder.
This version doesn't have a servlet for the Android Version and works together with a modified, pure websocket version of AppRTC for Android see: 

pure Websocket AppRTC for Android: https://github.com/inspiraluna/AppRTCDemo 

###Documentation:
-----------------
- Kurennto Java Tutorial http://doc-kurento.readthedocs.io/en/stable/tutorials/java/tutorial-one2one-adv.html


###Todo:
- login user (websocket session id + username) and 
- get list of logged in users for android, ios and browser 
- recording 
- screensharing
- widget for browser


###Done
- 2016-06-28 - implement simple call response within browser use kurento one2one-call example
- 2016-06-28 - android version works on websocket only / port android project 
