## AppRTC - Kurento Example

This is a simple example project in nodejs and j2ee to demonstrate the compatibility of the [AppRTCDemo](https://github.com/njovy/AppRTCDemo) Android App with the [Kurento Media Server](http://www.kurento.org/).

See for implementations in the named platform folder.
This version doesn't have a servlet for the Android version and works together with a modified, pure websocket version of AppRTC for Android see: 

pure Websocket AppRTC for Android: https://github.com/inspiraluna/AppRTCDemo 

###Documentation:
-----------------
- Kurento Java Tutorial http://doc-kurento.readthedocs.io/en/stable/tutorials/java/tutorial-one2one-adv.html


###Todo:
- when websocket closes user is not deleted from registry
- fix logging slf4j for maven
- login user (websocket session id + username) and 

- screensharing
- widget for browser http://shootitlive.com/2012/07/developing-an-embeddable-javascript-widget/
 
- Merge recorded videos of call participants into a split screen view
  - ffmpeg -i input1.mp4 -i input2.mp4 -filter_complex '[0:v]pad=iw*2:ih[int];[int][1:v]overlay=W/2:0[vid]' -map [vid] -c:v libx264 -crf 23 -preset veryfast output.mp4
  - Build new Docker image based upon fiware/stream-oriented-kurento including ffmpeg
  - Share folder of recorded videos with host (necessary in production?)


###Done
- 2016-08-02 - recording
- 2016-07-09 - get list of logged in users for android, ios and browser
- 2016-06-28 - implement simple call response within browser use kurento one2one-call example
- 2016-06-28 - android version works on websocket only / port android project
