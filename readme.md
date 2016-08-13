## AppRTC - Kurento Example

This is a simple example project in nodejs and j2ee to demonstrate the compatibility of the [AppRTCDemo](https://github.com/njovy/AppRTCDemo) Android App with the [Kurento Media Server](http://www.kurento.org/).

See for implementations in the named platform folder.
This version doesn't have a servlet for the Android version and works together with a modified, pure websocket version of AppRTC for Android see: 

pure Websocket AppRTC for Android: https://github.com/inspiraluna/AppRTCDemo 

###Documentation:
-----------------
- Kurento Java Tutorial http://doc-kurento.readthedocs.io/en/stable/tutorials/java/tutorial-one2one-adv.html


###Todo:
- fix logging slf4j for maven
- screensharing  
		http://doc-kurento.readthedocs.io/en/stable/mastering/kurento_utils_js.html
- widget for browser 
		http://shootitlive.com/2012/07/developing-an-embeddable-javascript-widget/ 
- Merge recorded videos of call participants into a split screen view
  - ffmpeg -i input1.mp4 -i input2.mp4 -filter_complex '[0:v]pad=iw*2:ih[int];[int][1:v]overlay=W/2:0[vid]' -map [vid] -c:v libx264 -crf 23 -preset veryfast output.mp4 (http://superuser.com/a/537482)
  - Build new Docker image based upon fiware/stream-oriented-kurento including ffmpeg
  - Share folder of recorded videos with host (necessary in production?)

##Bugs
- wenn closing socket registered user should be deregistered too!
- turnConfig in WebSocketServer.java too much - needs clean up! check ios / android / web client 



###Done


- 2016-08-13  - config parameter in Ressource Bundle or property files 
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
