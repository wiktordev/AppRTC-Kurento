## AppRTC - Kurento Example

This is a simple example project in nodejs to demonstrate the compatibility of the [AppRTCDemo](https://github.com/njovy/AppRTCDemo) Android App with the [Kurento Media Server](http://www.kurento.org/).

This server copies the API of an apprtc server so a mobile device running the AppRTCDemo App can communicate with a Webclient powered by kurento.

This is a proof of concept and not meant to be stable or complete.

It is heavily based on the [Kurento Example Apps](https://github.com/Kurento/kurento-tutorial-node.git).

Tested with AppRTCDemo Version [b73a922be382193ec703c42284c448ff38107f11](https://github.com/njovy/AppRTCDemo/commit/b73a922be382193ec703c42284c448ff38107f11) on a Sony Xperia


## Installation

1. Clone this repository
2. copy config.js.example to config.js and adjust the values
3. run `npm install`

## Execution

1. start your kurento server
2. run `npm start`
3. on the mobile device start the [AppRTCDemo](https://github.com/njovy/AppRTCDemo) app
4. in the app settings adjust the Room server URL to your previously started server
5. connect to a room
6. on a different device direct a webbrowser to your server url.
7. enter the room name and hit start
8. you should be able to see and hear eachother

## License

Licensed under [LGPL v2.1 License]
