# AppRTC - Kurento 

This project is a webrtc signaling server written in Java.
It has a sample HTML-client which displays the capabilities after deployment.
All calls are beeing recorded by Kurento Media Server to the configured directory. 

There are:
- a pure websocket AppRTC for iOS: https://github.com/inspiraluna/apprtc-ios and
- a pure websocket AppRTC for Android: https://github.com/inspiraluna/AppRTCDemo 


##Documentation:
-----------------
- Kurento Java Tutorial http://doc-kurento.readthedocs.io/en/stable/tutorials/java/tutorial-one2one-adv.html

###Installation:
1. Clone this repository to your workstation
```git clone https://github.com/<<repository-url>>.git```
2. Change into the 'jWebrtc' directory  
```cd <<repository-dir>>/jWebrtc```
3. Create war file 
```mvn package```
4. Deploy war file into your servlet container (e.g. JBoss, Tomcat, Glassfish)
5. Configure environment variables of your servlet container. E.g. modify .profile in the home directory of the user who runs the servlet container. We configure the URL of the Kurento-Server (here on localhost), STUN-,TURN-Urls and TURN username and password. Use public IP since its used by the clients not by the server.
```export JAVA_OPTS="$JAVA_OPTS -Dkms.url=ws://localhost:8888/kurento -DSTUN_URL=stun:<<stun-ip>>:3478 -DTURN_USERNAME=webrtc -DTURN_PASSWORD=fondkonzept -DTURN_URL=turn:<<turn-ip>>:3478"``` 
6. configure your servlet container to use SSL if you wanto to use WebRTC outside of localhost (WebRTC only works with HTTPS!)
7. Start Servlet Container and Test WebRTC in (e.g. go to: ```http://localhost/jWebrtc```) 
8. Install and configure your Android-WebRTC App
9. Install and configure your iOS-WebRTC App


##Tests
- Browser2Browser in local-LAN and to remote LANs
- Browser2AndroidApp in local-LAN and to remote LANs
- Browser2iOSApp in local-LAN and to remote LANs
- Android2AndroidApp in local-LAN and to remote LANs
- iOS2iOSApp in local-LAN and to remote LANs
- iOS2AndroidApp in local-LAN and to remote LANs
- Support-Widget 
	- connect and login with to webrtc with browser, android or ios e.g. with user 'CustomerSupportUser'
	- install a support-widget.html on your favourite webserver e.g. 

```
		<html>
			<head><title>Example Support Widget</title>
			<script src="https://<<your-webrtc-server>>/jWebrtc/js/webrtcStatusWidget.js"></script>
			</head>
			<body>
					<h1>Example WebRTC Support Widget</h1>
				  <div id="webrtc-online-status" data-peer='CustomerSupportUser'  data-me='webuser'>
				  </div>
			</body></html>
```


###Todo:
- duplicate repository to le-space 
		- https://help.github.com/articles/duplicating-a-repository/
- widget for browser 
		- http://shootitlive.com/2012/07/developing-an-embeddable-javascript-widget/ 
		- enable videocall in widget
- screensharing  
		https://groups.google.com/forum/#!topic/kurento/jpis7IbU2Zo
		https://github.com/muaz-khan/WebRTC-Experiment/tree/master/Chrome-Extensions/desktopCapture
		https://groups.google.com/forum/#!topic/kurento/s1FrlX_9n4I
		http://stackoverflow.com/questions/36485558/getting-screencaptureerror-in-chrome-using-kurento-media-server
		http://doc-kurento.readthedocs.io/en/stable/mastering/kurento_utils_js.html
		https://www.webrtc-experiment.com/Pluginfree-Screen-Sharing/
		https://github.com/muaz-khan/Chrome-Extensions/tree/master/desktopCapture
		http://doc-kurento.readthedocs.io/en/stable/mastering/kurento_utils_js.html
		https://bloggeek.me/implement-screen-sharing-webrtc/
- error-message-improvents
	- if kurento connection cannot be established create better error message

###Nice2Haves
- Merge recorded videos of call participants into a split screen view
  - ffmpeg -i input1.mp4 -i input2.mp4 -filter_complex '[0:v]pad=iw*2:ih[int];[int][1:v]overlay=W/2:0[vid]' -map [vid] -c:v libx264 -crf 23 -preset veryfast output.mp4 (http://superuser.com/a/537482)
  
###Bugs
- Tomcat does not create nice session IDs for the websockts - use HTTP-SessionId?

###Verinbdungsprobleme
- iOS kann nicht in bestimmten Netzen keine PeerConnection aufbauen. Turn/Stun Problme
- ich habe mich (Mac) beim telefonat mit einem Linux-Rechner selbst gehört. War sehr unangenehm.
- Proleme mit Windows Chrome (kein Bild) und Mac (Chrome)
- Probleme mit Windows Mozilla und (Mac Chrome) Probleme mit Qualität (Skype besser) (verzerrt) (Mac zu Mac mit Eggenfelden war okey)
- Probleme mit Anruf zu iOS keine Videoverbindung kommt zustande (Android funktioniert)

###Done
- 2016-09-01 - fixed logging slf4j for maven
- 2016-08-29 - (administration/turnServer)
	http://stackoverflow.com/questions/28772212/stun-turn-server-connectivity-test
	https://tools.ietf.org/html/rfc7376
	http://rtcquickstart.org/guide/multi/firewall-nat-considerations.html
	https://www.nomachine.com/AR07N00894


- 2016-08-29 - kurento behind NAT
		http://builds.kurento.org/release/5.0.3/mastering/advanced_installation_guide.html?highlight=plumberendpoint
		https://groups.google.com/forum/#!topic/kurento/QkO_ct0QEGE
- 2016-08-21 - (browser) update options.iceServers after appConfig response 
- 2016-08-21 - fixed problems with stopping a call. (finish session on both sides)
- 2016-08-19 - wenn closing socket registered user should be deregistered too!
- 2016-08-19 - turnConfig in WebSocketServer.java too much - needs clean up! check ios / android / web client 
- 2016-08-17 - change turn server config in server - because it produces wrong format
- 2016-08-14 - create self signed certificate for netbeans glassfish
- 2016-08-14 - add system properties
- 2016-08-13  - config parameter in ressource bundle or property files 
				http://stackoverflow.com/questions/531593/how-to-use-a-property-file-with-glassfish
				http://stackoverflow.com/questions/1094121/application-configuration-files-for-glassfish-java-ee-5-web-services
- 2016-08-02 - recording
- 2016-08-01 - login user (websocket session id + username) and 
- 2016-08-01 - when websocket closes user is not deleted from registry
- 2016-07-09 - get list of logged in users for android, ios and browser 
- 2016-07-09 - login user (websocket session id + username) and 
- 2016-07-09 - get list of logged in users for android, ios and browser
- 2016-06-28 - implement simple call response within browser use kurento one2one-call example
- 2016-06-28 - android version works on websocket only / port android project
