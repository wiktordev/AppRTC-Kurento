## AppRTC - Kurento Example

This is a simple example project in nodejs and j2ee to demonstrate the compatibility of the [AppRTCDemo](https://github.com/njovy/AppRTCDemo) Android App with the [Kurento Media Server](http://www.kurento.org/).

See for implementations in the named platform folder.
This version doesn't have a servlet for the Android version and works together with a modified, pure websocket version of AppRTC for Android see: 

pure Websocket AppRTC for Android: https://github.com/inspiraluna/AppRTCDemo 

###Documentation:
-----------------
- Kurennto Java Tutorial http://doc-kurento.readthedocs.io/en/stable/tutorials/java/tutorial-one2one-adv.html

###Installation:
- run this jWebRTC Project in Netbeans (we use Glassfish at the moment)
- Have a look at Config.java and adjust your ip-addresses for your application server (this server) and kurento media server, also adjust your turn server
- start kurento server
- connect your mobile phone with the same network as your application server
- install AppRTC for Android from https://github.com/inspiraluna/AppRTCDemo 
- open browser at http://localhost:8080 and register user 'johann' on browser
- register second user e.g. 'christina' under settings in Android App
- call 'johann' from android (you should see your video and the video of the browser in both devices) 

###Todo:

- get list of logged in users for android, ios and browser (call 'onlineUsers')
- if new user registered (call 'register') send and update websocket message ('registerResponse') to all connect users and update  userlist on client
- recording 
- screensharing
- widget for browser


###Done
- 2016-06-28 - login user (websocket session id + username) and 
- 2016-06-28 - implement simple call response within browser use kurento one2one-call example
- 2016-06-28 - android version works on websocket only / port android project 
