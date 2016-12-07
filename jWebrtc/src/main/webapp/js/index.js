/*
 * (C) Copyright 2014 Kurento (http://kurento.org/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

var ws = new WebSocket('wss://' + location.host + '/jWebrtc/ws');
var videoInput;
var videoOutput;
var screenOutput;
var webRtcPeer;
var webRtcPeer2;
var response;
var callerMessage;

var isAudioEnabled = true;
var isWebcamEnabled = true;
var isScreenSharingEnabled;
var isScreenSharingAvailable = false;
var isVideoStreamEnabled = isWebcamEnabled || isScreenSharingEnabled;

var chkAudioEnabled;
var chkWebcamEnabled;
var chkScreenEnabled;

var from;
var myConsultant = {
    name: '',
    status: ''
};
var configuration = {
    "iceServers": [{
        "urls": "stun:webrtc.a-fk.de:3478"
    }, {
        "urls": "turn:webrtc.a-fk.de:3478",
        "username": "webrtc",
        "credential": "fondkonzept"
    }]
};

var registerName = null;
var registerState = null;
const NOT_REGISTERED = 0;
const REGISTERING = 1;
const REGISTERED = 2;


function setRegisterState(nextState) {
    switch (nextState) {
        case NOT_REGISTERED:
            enableButton('#register', 'register()');
            hideButton('#call');
            hideButton('#screenEnabled');
            hideButton('#screenOutput');
             hideButton('#peers');
            setCallState(NO_CALL);
            break;
        case REGISTERING:
            disableButton('#register');
            break;
        case REGISTERED:
            disableButton('#register');
            showButton('#terminate');
            showButton('#audioEnabled');
            showButton('#call');
            
            showButton('#peers');

            setCallState(NO_CALL);
            break;
        default:
            return;
    }
    registerState = nextState;
}

var callState = null;
const NO_CALL = 0; // client is idle
const PROCESSING_CALL = 1; // client is about to call someone (ringing the phone)
const IN_CALL = 2; // client is talking with someone
const IN_PLAY = 4; // client is replaying a record

function setCallState(nextState) {
    switch (nextState) {
        case NO_CALL:
            enableButton('#call', 'call()');
            disableButton('#terminate');
            hideButton('#terminate');
            hideButton('#audioEnabled');
            hideButton('#videoEnabled');
            hideButton('#screenEnabled');
             hideButton('#screenOutput');
            disableButton('#play');
            break;
        case PROCESSING_CALL:
            disableButton('#call');
            disableButton('#play');
            enableButton('#terminate', 'stop()');
            break;
        case IN_CALL:
            disableButton('#call');
            enableButton('#terminate', 'stop()');
            showButton('#terminate');
            showButton('#audioEnabled');
            setAudioEnabled(isAudioEnabled);
            showButton('#videoEnabled');
            showButton('#screenEnabled');
            setWebcamEnabled(isWebcamEnabled);
          //  hideButton('#screenEnabled');
            disableButton('#play');
            break;
        case IN_PLAY:
            disableButton('#call');
            enableButton('#terminate', 'stop()');
            disableButton('#play');
            break;
        default:
            return;
    }
    callState = nextState;
}

window.onload = function() {
  console = new Console();
  setRegisterState(NOT_REGISTERED);
  var drag = new Draggabilly(document.getElementById('videoSmall'));
  videoInput = document.getElementById('videoInput');
  videoOutput = document.getElementById('videoOutput');
  screenOutput = document.getElementById('screenOutput');

  document.getElementById('name').focus();
  ws.onopen = function() {
      console.log("ws connection now open");
      requestAppConfig();
  }
}

$(function() {
  // Handler for .ready() called.

  chkAudioEnabled = $("#audioEnabled");
  chkWebcamEnabled = $("#videoEnabled");
  chkScreenEnabled = $("#screenEnabled");

  chkAudioEnabled.on("click", function () {
    toggleAudio();
  });
  //setAudioEnabled(true);

  chkWebcamEnabled.on("click", function() {
    toggleWebcam();
  })
  //setWebcamEnabled(true);

  chkScreenEnabled.on("click", function() {
    toggleScreenSharing();
  })
  
  $( "#screenOutput" ).dblclick(function() {
    fullscreen() ;
  });
  
});

window.onbeforeunload = function() {
    ws.close();
}

ws.onmessage = function(message) {
    var parsedMessage = JSON.parse(message.data);
    console.info('Received message: ' + message.data);

    if (parsedMessage.params) {
        readAppConfig(parsedMessage);
    } else {
        switch (parsedMessage.id) {
            case 'registerResponse':
                registerResponse(parsedMessage);
                break;
            case 'registeredUsers':
                updateRegisteredUsers(JSON.parse(parsedMessage.response));
                break;
            case 'callResponse':
                callResponse(parsedMessage);
                break;
            case 'callScreenResponse':
                callScreenResponse(parsedMessage);
                break;         
            case 'incomingCall':
                incomingCall(parsedMessage);
            break;
            case 'incomingScreenCall':
                incomingScreenCall(parsedMessage);
            break;    
            case 'startCommunication':
                startCommunication(parsedMessage);
                break;
            case 'startScreenCommunication':
                startScreenCommunication(parsedMessage);
                break;
            case 'stopCommunication':
                console.info('Communication ended by remote peer');
                stop(true);
                break;
            case 'stopScreenCommunication':
                console.info('Screen Communication ended by remote peer');
                stopScreen(true);
            break;
            case 'iceCandidate':
                webRtcPeer.addIceCandidate(parsedMessage.candidate, function(error) {
                    if (error)
                        return console.error('Error adding candidate: ' + error);
                });
                break;
            case 'iceCandidateScreen':
                webRtcPeer2.addIceCandidate(parsedMessage.candidate, function(error) {
                    if (error)
                        return console.error('Error adding screen candidate: ' + error);
                });
                break;
            case 'responseOnlineStatus':
                setOnlineStatus(parsedMessage);
                break;
            case 'playResponse':
                playResponse(parsedMessage);
                break;
            case 'playEnd':
                playEnd();
                break;
            default:
                console.error('Unrecognized message', parsedMessage);
        }
    }
}

function requestAppConfig() {
    console.log('requesting app config');
    var message = {
        id: 'appConfig',
        type: 'browser'
    };
    sendMessage(message);
}

function setOnlineStatus(message) {
    var statusTextElement = $("#webrtc-online-status");
    if (message.message == myConsultant.name) {
        myConsultant.status = message.response;
    }
    statusTextElement.text(myConsultant.name + ' is ' + myConsultant.status);
}

function readAppConfig(message) {
    if (message.params) {
        configuration = message.params.pc_config;
    }
    if (message.result == "SUCCESS") return true;
}

function registerResponse(message) {
    if (message.response == 'accepted') {
        setRegisterState(REGISTERED);
        console.log(message.message);
    } else {
        setRegisterState(NOT_REGISTERED);
        var errorMessage = message.message ? message.message :
            'Unknown reason for register rejection.';
        console.log(errorMessage);
        alert('Error registering user. See console for further information.');
    }
}

function updateRegisteredUsers(userList) {
    console.log("User list: " + userList);
    var peers = $("#peer").find('option').remove().end();
    var name;
    for (var i = 0; i < userList.length; i++) {
        //options += '<option value="' + result[i].ImageFolderID + '">' + result[i].Name + '</option>';
        name = userList[i];
        if (name != $('#name').val()) {
            peers.append($("<option />").val(name).text(name));
        }
    }
}

// toggle audio stream
function toggleAudio() {
    setAudioEnabled(!isAudioEnabled);
}

// enable or disable the audio stream
function setAudioEnabled(enabled) {
  isAudioEnabled = enabled;
  if (webRtcPeer != undefined) {
    var localStreams = webRtcPeer.peerConnection.getLocalStreams();
    console.log(localStreams.length + " local streams");
    localStreams.forEach(function(localStream, index, array) {
      var audioTracks = localStream.getAudioTracks();

      console.log(audioTracks.length + " audio tracks");

      // if MediaStream has reference to microphone
      if (audioTracks[0]) {
          audioTracks[0].enabled = enabled;
      } else {
        console.error("No reference to microphone set!");
      }
    })

  } else {
    console.error("webRtcPeer is undefined! Cannot mute.");
  }

  $(chkAudioEnabled).toggleClass('active', isAudioEnabled);
  $(chkAudioEnabled).toggleClass('focus', false);

  console.log("Audio enabled: " + isAudioEnabled);
}

// toggle video stream
function toggleWebcam() {
  setWebcamEnabled(!isWebcamEnabled);
}

// enable or disable the video stream
function setWebcamEnabled(enabled) {
  isWebcamEnabled = enabled;

  $(chkWebcamEnabled).toggleClass('active', isWebcamEnabled);

  console.log("Video enabled: " + isWebcamEnabled);

  setVideoStreamEnabled(isWebcamEnabled || isScreenSharingEnabled);

  if(isWebcamEnabled) {
    switchToWebcam();
  }
}

// toggle screen sharing
function toggleScreenSharing() {
  if(webRtcPeer2==null){
        setScreenSharingEnabled(!isScreenSharingEnabled);
  }else{
        stopScreen();
  }
  
}

// enable or disable screen sharing
function setScreenSharingEnabled(enabled) {
  if(enabled) isExtensionInstalled();
  
  isScreenSharingEnabled = enabled; //&& isScreenSharingAvailable;

  $(chkScreenEnabled).toggleClass('btn-danger', isScreenSharingEnabled);
  console.log("Screen sharing enabled: " + isScreenSharingEnabled);

  setVideoStreamEnabled(isWebcamEnabled || isScreenSharingEnabled);

  if(isScreenSharingEnabled) {
      if (!DetectRTC.isWebRTCSupported) {
        console.log("WebRTC not supported");
        showCompatibilityWarning("#rtc-area");
      }

    switchToScreenSharing();
  }
}

function switchToScreenSharing() {
  console.log("Start screen sharing... creating another webrtc connection");
  additionalScreenCall();
}

function switchToWebcam() {
  console.log("Start video from webcam...");
}

function setVideoStreamEnabled(enabled) {
  isVideoStreamEnabled = enabled;
  if (webRtcPeer != undefined) {
    webRtcPeer.peerConnection.getLocalStreams()[0].getVideoTracks()[0].enabled = isVideoStreamEnabled;
  }
}

function playResponse(message) {
    if (message.response != 'accepted') {
        hideSpinner(videoOutput);
        document.getElementById('videoSmall').style.display = 'block';
        alert(message.error);
        document.getElementById('peer').focus();
        setCallState(NO_CALL);
    } else {
        setCallState(IN_PLAY);
        webRtcPeer.processAnswer(message.sdpAnswer, function(error) {
            if (error)
                return console.error(error);
        });
    }
}

// Start streaming on callers side, if accepted
function callResponse(message) {
    if (message.response != 'accepted') {
        console.info('Call not accepted by peer. Closing call');
        var errorMessage = message.message ? message.message :
            'Unknown reason for call rejection.';
        console.log(errorMessage);
        stop();
    } else {
      console.log("call accepted");
        setCallState(IN_CALL);
        webRtcPeer.processAnswer(message.sdpAnswer, function(error) {
            if (error)
                return console.error(error);
        });
        console.log("answer processed");
      //  setVideoStreamEnabled(isWebcamEnabled || isScreenSharingEnabled);
    }
}

// Start streaming on callers side, if accepted
function callScreenResponse(message) {
    if (message.response != 'accepted') {
        console.info('Call not accepted by peer. Closing call');
        var errorMessage = message.message ? message.message :
            'Unknown reason for call rejection.';
        console.log(errorMessage);
        stopScreen();
    } else {
      console.log("call accepted");
       // setCallState(IN_CALL);
       webRtcPeer2.processAnswer(message.sdpAnswer, function(error) {
            if (error)
                return console.error(error);
        });
        console.log("answer processed");
      //  setVideoStreamEnabled(isWebcamEnabled || isScreenSharingEnabled);
    }
}

// Start streaming on callees side
function startCommunication(message) {
  console.log("startCommunication");
    setCallState(IN_CALL);

    webRtcPeer.processAnswer(message.sdpAnswer, function(error) {
        if (error)
            return console.error(error);
    });
    console.log("answer processed");
  //  setVideoStreamEnabled(isWebcamEnabled || isScreenSharingEnabled);
} 

// Start streaming on callees side
function startScreenCommunication(message) {
    
  console.log("startScreenCommunication");
  
    webRtcPeer2.processAnswer(message.sdpAnswer, function(error) {
        showButton('#screenOutput');
        if (error)
            return console.error(error);
    });
    console.log("answer for screen processed");
  //  setVideoStreamEnabled(isWebcamEnabled || isScreenSharingEnabled);
} 

/*
Someone is calling
*/
function incomingCall(message) {
    // If busy just reject without disturbing user
    if (callState != NO_CALL) {
        var response = {
            id: 'incomingCallResponse',
            from: message.from,
            callResponse: 'reject',
            message: 'bussy'
        };
        return sendMessage(response);
    }

    setCallState(PROCESSING_CALL);
    if (confirm('User ' + message.from +
            ' is calling you. Do you accept the call?')) {

        console.log("accepting call");
        showSpinner(videoInput, videoOutput);

        from = message.from;
        var options = {
            localVideo: videoInput,
            remoteVideo: videoOutput,
            onicecandidate: onIceCandidate,
            onerror: onError
        }
        options.configuration = configuration;
        webRtcPeer = new kurentoUtils.WebRtcPeer.WebRtcPeerSendrecv(options,
            function(error) {
                if (error) {
                    return console.error(error);
                }
                webRtcPeer.generateOffer(onOfferIncomingCall);
            });

    } else {
        var response = {
            id: 'incomingCallResponse',
            from: message.from,
            callResponse: 'reject',
            message: 'user declined'
        };
        sendMessage(response);
        stop();
    }
}
/*
Someone is calling
*/
function incomingScreenCall(message) {
  
        console.log("accepting screen call");

        from = message.from;
        
        var constraints = {
            audio: false
        };
        
        var options = {
        //    localVideo: videoInput,
            remoteVideo: screenOutput,
            onicecandidate: onIceCandidateScreen,
            onerror: onError,   
            mediaconstrains: constraints
        }
        options.configuration = configuration;
        webRtcPeer2 = new kurentoUtils.WebRtcPeer.WebRtcPeerSendrecv(options,
            function(error) {
                if (error) {
                    return console.error(error);
                }
              webRtcPeer2.generateOffer(onOfferIncomingScreenCall);
              //onIncomingScreenCall(error);
              hideButton('#screenEnabled');
            });
}

function onOfferIncomingCall(error, offerSdp) {
    if (error)
        return console.error("Error generating the offer");
    var response = {
        id: 'incomingCallResponse',
        from: from,
        callResponse: 'accept',
        sdpOffer: offerSdp
    };
    sendMessage(response);
}

function onOfferIncomingScreenCall(error,offerSdp) {
    if (error)
        return console.error("Error generating the offer");
    var response = {
        id: 'incomingScreenCallResponse',
        from: from,
        callResponse: 'accept',
        sdpOffer: offerSdp
    };
    sendMessage(response);
}

function register() {
    var name = document.getElementById('name').value;
    if (name == '') {
        window.alert('You must insert your user name');
        return;
    }
    setRegisterState(REGISTERING);

    var message = {
        id: 'register',
        name: name
    };
    sendMessage(message);
    document.getElementById('peer').focus();
}

function call() {
    
    if (document.getElementById('peer').value == '') {
        window.alert('You must specify the peer name');
        return;
    }
    setCallState(PROCESSING_CALL);
    showSpinner(videoInput, videoOutput);


        var options = {
            localVideo: videoInput,
            remoteVideo: videoOutput,
            onicecandidate: onIceCandidate,
            onerror: onError,
          //mediaConstraints: constraints
        }
        options.configuration = configuration;
        webRtcPeer = new kurentoUtils.WebRtcPeer.WebRtcPeerSendrecv(options,
            function(error) {
                if (error) {
                    return console.error(error);
                }
                webRtcPeer.generateOffer(onOfferCall);
        });

}

function additionalScreenCall() {

    if (isScreenSharingEnabled) {
        // Der Weg über die mediaSource funktioniert aus unbekannten Gründen nicht,
        // daher ermittle ich den Videostream und übergebe ihn direkt an den WebRtcPeer
        // options.videoStream

            showButton('#screenOutput');
        // first get audio stream
        var audioConstraints = {
          audio: false,  //turn this true in case you want to share this in a single stream 
          video: true,
        };

        navigator.getUserMedia(audioConstraints, function(stream) {
          initiateScreenSharing(stream);
        }, function(error) {
          console.error("Could not get audio stream! " + error);
        });

    } 
}

function initiateScreenSharing(audioStream) {
  getScreenId(function(error, sourceId, screen_constraints) {
      // error    == null || 'permission-denied' || 'not-installed' || 'installed-disabled' || 'not-chrome'
      // sourceId == null || 'string' || 'firefox'

      navigator.getUserMedia = navigator.mozGetUserMedia || navigator.webkitGetUserMedia;
      navigator.getUserMedia(screen_constraints, function(stream) {

        var constraints = {
            audio: false
        };
        
        var options = {
              localVideo: screenOutput,
             // remoteVideo: videoOutput,
              videoStream: stream,
              //audioStream: audioStream,
              onicecandidate: onIceCandidateScreen,
              onError: onError,
              sendSource: 'window',
              mediaConstraints: constraints
        }
          
          options.configuration = configuration;
          webRtcPeer2 = new kurentoUtils.WebRtcPeer.WebRtcPeerSendrecv(options,
              function(error) {
                  if (error) {
                      return console.error(error);
                  }
                  webRtcPeer2.generateOffer(onOfferCallScreen);
              });

      }, function(error) {
          console.error(error);
      });
  });
}

function play() {
    var peer = document.getElementById('peer').value;
    if (peer == '') {
        window.alert('You must specify the peer name');
        document.getElementById('peer').focus;
        return;
    }

    document.getElementById('videoSmall').display = 'none';
    setCallState(IN_PLAY);
    showSpinner(videoOutput);

    var options = {
        remoteVideo: videoOutput,
        onicecandidate: onIceCandidate
    }
    
    webRtcPeer = new kurentoUtils.WebRtcPeer.WebRtcPeerSendrecv(options,
        function(error) {
            if (error) {
                return console.error(error);
            }
            this.generateOffer(onOfferPlay);
        }
    )

}

function onOfferCall(error, offerSdp) {
    if (error) {
        return console.error('Error generating the offer');
    }
    console.log('Invoking SDP offer callback function');
    var message = {
        id: 'call',
        from: document.getElementById('name').value,
        to: $('#peer').val(),
        sdpOffer: offerSdp
    };
    sendMessage(message);
}

function onOfferCallScreen(error, offerSdp) {
    if (error) {
        return console.error('Error generating the offer');
    }
    console.log('Invoking SDP offer callback function for the screensharing function');
    var message = {
        id: 'callScreen',
        from: document.getElementById('name').value,
        to: $('#peer').val()?$('#peer').val():from,  //this should be read from a local variable which is written 
        sdpOffer: offerSdp
    };
    sendMessage(message);
}

function onOfferPlay(error, offerSdp) {
    if (error) {
        return console.error('Error generating the offer');
    }
    console.log('Invoking SDP offer callback function');
    var message = {
        id: 'play',
        user: document.getElementById('peer').value,
        sdpOffer: offerSdp
    };
    sendMessage(message);
}

function playEnd() {
    setCallState(NO_CALL);
    hideSpinner(videoInput, videoOutput);
    document.getElementById('videoSmall').style.display = 'block';
}

function stop(message) {
    var stopMessageId = (callState == IN_CALL || callState == PROCESSING_CALL) ? 'stop' : 'stopPlay';
    setCallState(NO_CALL);
    if (webRtcPeer) {
        
        console.log('message is:' + message);
        hideSpinner(videoInput, videoOutput);
        document.getElementById('videoSmall').display = 'block';
        webRtcPeer.dispose();
        webRtcPeer = null;

        if (!message) {
            var message = {
                id: stopMessageId
            }
            sendMessage(message);
        }
        stopScreen(screen);
    }
}
function stopScreen(message) {  //message true means: stopScreen was called from websocket (by other party) so it the party does not need to be informed
    //var stopMessageId = (callState == IN_CALL || callState == PROCESSING_CALL) ? 'stop' : 'stopPlay';
   // setCallState(NO_CALL);
    if (webRtcPeer2) {
        isScreenSharingEnabled = false;
        $(chkScreenEnabled).toggleClass('btn-danger', false);
        hideButton('#screenOutput');
        console.log('message is:' + message);
       
        document.getElementById('screenSmall').display = 'block';
        webRtcPeer2.dispose();
        webRtcPeer2 = null;

        if (!message) {
            var message = {
                id: 'stopScreen'
            }
            sendMessage(message);
        }else{
             showButton('#screenEnabled');
        }
    }
}

function onError() {
    setCallState(NO_CALL);
}

function onIceCandidate(candidate) {

    var message = {
        id: 'onIceCandidate',
        candidate: candidate
    };
    sendMessage(message);
}

function onIceCandidateScreen(candidate) {

    var message = {
        id: 'onIceCandidateScreen',
        candidate: candidate
    };
    sendMessage(message);
}


function sendMessage(message) {
    var jsonMessage = JSON.stringify(message);
    console.log('Sending message: ' + jsonMessage);
    ws.send(jsonMessage);
}

function showSpinner() {
    for (var i = 0; i < arguments.length; i++) {
        arguments[i].poster = './img/transparent-1px.png';
        arguments[i].style.background = 'center transparent url("./img/spinner.gif") no-repeat';
    }
}

function hideSpinner() {
    for (var i = 0; i < arguments.length; i++) {
        arguments[i].src = '';
        arguments[i].poster = './img/webrtc.png';
        arguments[i].style.background = '';
    }
}
function hideButton(id){
    $(id).addClass("hidden");
}
function showButton(id){
    $(id).removeClass( "hidden" );
}

function disableButton(id) {
    $(id).attr('disabled', true);
    $(id).removeAttr('onclick');
    $(id).toggleClass("disabled", true);
}

function enableButton(id, functionName) {
    $(id).attr('disabled', false);
    $(id).attr('onclick', functionName);
    $(id).toggleClass("disabled", false);
}

function showCompatibilityWarning(id) {
  $(id).html("Please use a browser that supports WebRTC, like Firefox or Chrome or install WebRTC-Plugin https://github.com/sarandogou/webrtc-everywhere");
}

/**
 * Lightbox utility (to display media pipeline image in a modal dialog)
 */
$(document).delegate('*[data-toggle="lightbox"]', 'click', function(event) {
    event.preventDefault();
    $(this).ekkoLightbox();
});

function isExtensionInstalled() {
  if (DetectRTC.browser.isChrome) {
    // Check for chrome extension
    getChromeExtensionStatus(function(status) {
      console.info("Chrome extension: " + status);

        if(status == 'installed') {
          // chrome extension is installed.
          handleScreenSharingAvailable();
      }

      if(status == 'installed-disabled') {
          // chrome extension is installed but disabled.
          handleMissingChromeExtension();
      }

      if(status == 'not-installed') {
          // chrome extension is not installed
          handleMissingChromeExtension();
      }

      if(status == 'not-chrome') {
          // using non-chrome browser
          handleMissingChromeExtension();
      }
    });
  }

  if (DetectRTC.browser.isFirefox) {
    // Check for firefox add on
    // request addon to enable screen capturing for your domains
    window.postMessage({
        enableScreenCapturing: true,
        domains: ['localhost', '127.0.0.1','*.a-fk.de', '*.le-space.de', '*.nicokrause.com']
    }, "*");

    // watch addon's response
    // addon will return "enabledScreenCapturing=true" for success
    // else "enabledScreenCapturing=false" for failure (i.e. user rejection)
    window.addEventListener("message", function(event) {
        var addonMessage = event.data;

        if(!addonMessage || typeof addonMessage.enabledScreenCapturing === 'undefined') {
          console.warn("Firefox AddOn not available");
          handleMissingFirefoxAddon();
          return;
        }

        if(addonMessage.enabledScreenCapturing === true) {
            // addonMessage.domains === [array-of-your-domains]
            console.info("Firefox AddOn available");
            console.log(JSON.stringify(addonMessage.domains) + ' are enabled for screen capturing.');
            $("#warningScreenSharingFirefox").hide();
            handleScreenSharingAvailable();
        }
        else {
            // reason === 'user-rejected'
            console.warn("Firefox AddOn: " + addonMessage.reason);
            handleMissingFirefoxAddon();
        }
    }, false);
  }

  return false;
}

function handleMissingChromeExtension() {
  $("#screen-call").toggleClass("disabled", true);
  isScreenSharingAvailable = false;

  // show message "install extension"
  var buttonStr = "<button id='installButton' onclick='installChromeExtension()' id='install-button' class='btn btn-warning' title='Install Screen Sharing extension to present your desktop'><i class='fa fa-download fa-fw'></i></button>";

  $("#warningScreenSharingChrome").removeClass("hidden");
  $("#screenEnabled").hide();
  $("#screenEnabled").after(buttonStr);

  $("#installScreenSharingLink").on("click", function() {
    installChromeExtension();
  });

}

function handleMissingFirefoxAddon() {
  $("#screen-call").toggleClass("disabled", true);
  isScreenSharingAvailable = false;

  // show message "install addon"
  var buttonStr = "<button id='installButton' onclick='installFirefoxAddOn(); this.disabled = true;' class='btn btn-warning' title='Install Screen Sharing extension to present your desktop'><i class='fa fa-download fa-fw'></i></button>";
  $("#warningScreenSharingFirefox").removeClass("hidden");
  $("#screenEnabled").hide();
  $("#screenEnabled").after(buttonStr);

  $("#installScreenSharingLink").on("click", function() {
    installFirefoxAddOn();
  });
  $("#warningScreenSharingFirefox").show();
}

function handleScreenSharingAvailable() {
  $("#screenEnabled").show();
  $("#installButton").remove();
  $("#warningScreenSharing").hide();
  isScreenSharingAvailable = true;
}

function installFirefoxAddOn() {
    InstallTrigger.install({
        'Foo': {
            URL: 'https://addons.mozilla.org/firefox/downloads/latest/enable-screen-capturing/addon-655146-latest.xpi?src=dp-btn-primary',
            toString: function() {
                return this.URL;
            }
        }
    });
}

function installChromeExtension() {
  !!navigator.webkitGetUserMedia && !!window.chrome && !!chrome.webstore && !!chrome.webstore.install && chrome.webstore.install('https://chrome.google.com/webstore/detail/cpnlknclehfhfldcbmcalmobceenfjfd',
  function() {
    location.reload();
  },
  function(error) {
    console.error("Unable to install extension! " + error);
  });
}

function getChromeExtensionStatus(callback) {
    // https://chrome.google.com/webstore/detail/screen-capturing/cpnlknclehfhfldcbmcalmobceenfjfd
    var extensionid = 'cpnlknclehfhfldcbmcalmobceenfjfd';

    $.get('chrome-extension://' + extensionid + '/icon.png', function(data) {
      callback('installed');
    }).fail(function() {
      callback('not-installed');
    });
}


function fullscreen(){
  if (
    document.fullscreenElement ||
    document.webkitFullscreenElement ||
    document.mozFullScreenElement ||
    document.msFullscreenElement
  ) {
    if (document.exitFullscreen) {
      document.exitFullscreen();
    } else if (document.mozCancelFullScreen) {
      document.mozCancelFullScreen();
    } else if (document.webkitExitFullscreen) {
      document.webkitExitFullscreen();
    } else if (document.msExitFullscreen) {
      document.msExitFullscreen();
    }
  } else {
    element = $('#screenOutput').get(0);
    if (element.requestFullscreen) {
      req = element.requestFullscreen();
      call(req);
    } else if (element.mozRequestFullScreen) {
     req =  element.mozRequestFullScreen();
     call(req);
    } else if (element.webkitRequestFullscreen) {
      req = element.webkitRequestFullscreen(Element.ALLOW_KEYBOARD_INPUT);
  
      call(req);
    } else if (element.msRequestFullscreen) {
      req =  element.msRequestFullscreen();
      call(req);
    }
  }
}