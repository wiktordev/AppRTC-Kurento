/*
 * (C) Copyright 2016 Le Space UG
 */
var getCurrentScript = function () {
  if (document.currentScript) {
    return document.currentScript.src;
  } else {
    var scripts = document.getElementsByTagName('script');
    return scripts[scripts.length-1].src;

  }
};

var getCurrentServer = function(scriptPath){
      var l = document.createElement("a");
      l.href = scriptPath;
      return l.hostname;
}

var server = getCurrentServer();

        
var ws = new WebSocket('wss://' + server + '/jWebrtc/ws');

var localVideo;
var remoteVideo;
var miniVideo; 
var icons; 
var webRtcPeer;
var response;
var callerMessage;
var from = "";
var myConsultant = {name: '', status: ''};
var configuration = {"iceServers":[{"urls":"stun:webrtc.a-fk.de:3478"},{"urls":"turn:webrtc.a-fk.de:3478","username":"webrtc","credential":"fondkonzept"}]};

var registerName = null;
var registerState = null;
var callState = null;

const NO_CALL = 0;					// client is idle
const PROCESSING_CALL = 1;                              // client is about to call someone (ringing the phone)
const IN_CALL = 2;					// client is talking with someone
const IN_PLAY = 4;
const NOT_REGISTERED = 0;
const REGISTERING = 1;
const REGISTERED = 2;

function setRegisterState(nextState) {
	switch (nextState) {
	case NOT_REGISTERED:
		setCallState(NO_CALL);
		break;
	case REGISTERING:
		break;
	case REGISTERED:
		setCallState(NO_CALL);
		break;
	default:
		return;
	}
	registerState = nextState;
}


function setCallState(nextState) {
	switch (nextState) {
	case NO_CALL:
		
		disableButton('#terminate');
                deactivate(this.remoteVideo);
                deactivate(this.miniVideo);
                deactivate(this.localVideo);
                this.deactivate(icons);
                this.activate('#confirm-join-div'); //this.rejoinDiv_
                enableButton('#call', 'call()');
		break;
	case PROCESSING_CALL:
		disableButton('#call');
		disableButton('#terminate');
		break;
	case IN_CALL:
		disableButton('#call');
                miniVideo.src = localVideo.src;
                activate(this.remoteVideo);
                this.activate(icons); //this.rejoinDiv_
                activate(this.miniVideo);
                this.deactivate('#confirm-join-div'); //this.rejoinDiv_
                activate(this.miniVideo);
		enableButton('#terminate', 'stop()');
		break;
	default:
		return;
	}
	callState = nextState;
}

window.onload = function() {
        $("#webrtc-call").load("call.html");
        setRegisterState(NOT_REGISTERED);
     	
	ws.onopen = function() {
		console.log("ws connection now open");
                requestAppConfig();
                myConsultant.name = $('#webrtc-online-status').attr('data-peer');
		checkOnlineStatus(myConsultant);
	}
}

window.onbeforeunload = function() {
	ws.close();
}

ws.onmessage = function(message) {
	var parsedMessage = JSON.parse(message.data);
	console.info('Received message: ' + message.data);
        
        if(parsedMessage.params){
            readAppConfig(parsedMessage);
        }
        else{
            switch (parsedMessage.id) {
            case 'registerResponse':
                    registerResponse(parsedMessage);
                    break;
            case 'registeredUsers':
                    // server sends a list of all registered users including the user on this client
                    // updateRegisteredUsers(JSON.parse(parsedMessage.response));
                    break;
            case 'callResponse':
                    callResponse(parsedMessage);
                    break;
            case 'incomingCall':
                    incomingCall(parsedMessage);
                    break;
            case 'startCommunication':
                    startCommunication(parsedMessage);
                    break;
            case 'stopCommunication':
                    console.info('Communication ended by remote peer');
                    stop(true);
                    break;
            case 'iceCandidate':
                    webRtcPeer.addIceCandidate(parsedMessage.candidate, function(error) {
                            if (error)
                                    return console.error('Error adding candidate: ' + error);
                    });
                    break;
            case 'responseOnlineStatus':
                    setOnlineStatus(parsedMessage);
                    break;
            default:
                    //console.error('Unrecognized message', parsedMessage);
            }
        }
}
function requestAppConfig(){
        console.log('requesting app config');
	var message = {
		id : 'appConfig',
                type: 'browser'
	};
	sendMessage(message);
}

function readAppConfig(message) {
	if (message.params ) {
                    configuration = message.params.pc_config;
	}
	if(message.result=="SUCCESS") return true;
}

function checkOnlineStatus(user) {
	var message = {
		id : 'checkOnlineStatus',
		user : user.name
	};
	sendMessage(message);
}

function setOnlineStatus(message) {
	var statusTextElement = $("#webrtc-online-status");
	if (message.message == myConsultant.name) {
		myConsultant.status = message.response;
	}
        from = message.myUsername;
        console.log('setting online status done: myUsername is:'+from);
	statusTextElement.text(myConsultant.name + ' is ' + myConsultant.status);
        if(myConsultant.status=='online'){
            enableButton('#call', 'call()');
        }else{
            disableButton('#call');
        }
}

function registerResponse(message) {
	if (message.response == 'accepted') {
		setRegisterState(REGISTERED);
                from = message.myUsername;
                console.log( "registerResponse:"+message.message+ " : "+from);
	} else {
		setRegisterState(NOT_REGISTERED);
		var errorMessage = message.message ? message.message
				: 'Unknown reason for register rejection.';
		console.log(errorMessage);
		alert('Error registering user. See console for further information.');
	}
}


function callResponse(message) {
	if (message.response != 'accepted') {
		console.info('Call not accepted by peer. Closing call');
		var errorMessage = message.message ? message.message
				: 'Unknown reason for call rejection.';
		console.log(errorMessage);
		stop();
	} else {
		setCallState(IN_CALL);
		webRtcPeer.processAnswer(message.sdpAnswer, function(error) {
			if (error)
				return console.error(error);
		});
	}
}

function startCommunication(message) {
	setCallState(IN_CALL);
	webRtcPeer.processAnswer(message.sdpAnswer, function(error) {
		if (error)
			return console.error(error);
	});
}

function incomingCall(message) {
	// If busy just reject without disturbing user
	if (callState != NO_CALL) {
		var response = {
			id : 'incomingCallResponse',
			from : message.from,
			callResponse : 'reject',
			message : 'bussy'
		};
		return sendMessage(response);
	}

	setCallState(PROCESSING_CALL);
	if (confirm('User ' + message.from
			+ ' is calling you. Do you accept the call?')) {

		console.log("accepting call");
		showSpinner(localVideo, remoteVideo);
               
                from = message.from;
		var options = {
			localVideo : localVideo,
			remoteVideo : remoteVideo,
			onicecandidate : onIceCandidate,
			onerror : onError
		}
                
                options.configuration  = configuration;
		webRtcPeer = new kurentoUtils.WebRtcPeer.WebRtcPeerSendrecv(options,
				function(error) {
					if (error) {
						return console.error(error);
					}
					webRtcPeer.generateOffer(onOfferIncomingCall);
                });

	} else {
		var response = {
			id : 'incomingCallResponse',
			from : message.from,
			callResponse : 'reject',
			message : 'user declined'
		};
		sendMessage(response);
		stop();
	}
}

function onOfferIncomingCall(error, offerSdp) {
	if (error)
		return console.error("Error generating the offer");
	var response = {
		id : 'incomingCallResponse',
		from : from,
		callResponse : 'accept',
		sdpOffer : offerSdp
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
		id : 'register',
		name : name
	};
	sendMessage(message);
	document.getElementById('peer').focus();
}

function call() {
	setCallState(PROCESSING_CALL);
        localVideo = document.getElementById('local-video');		// <video>-element
        remoteVideo = document.getElementById('remote-video');
        miniVideo = document.getElementById('mini-video');
        icons = document.getElementById('icons');
	showSpinner(localVideo, remoteVideo);

	var options = {
		localVideo : localVideo,
		remoteVideo : remoteVideo,
		onicecandidate : onIceCandidate,
		onerror : onError
	}
	webRtcPeer = new kurentoUtils.WebRtcPeer.WebRtcPeerSendrecv(options,
			function(error) {
				if (error) {
					return console.error(error);
				}
				webRtcPeer.generateOffer(onOfferCall);
			});
}
function onOfferCall(error, offerSdp) {
	if (error) {
		return console.error('Error generating the offer');
	}
	console.log('Invoking SDP offer callback function : calling:'+myConsultant.name+ ' from:'+from);
	var message = {
		id : 'call',
                from: from,
                to: myConsultant.name,
		sdpOffer : offerSdp
	};
	sendMessage(message);
}



function stop(message) {
	console.log("Stopping");
	var stopMessageId = (callState == IN_CALL || callState == PROCESSING_CALL) ? 'stop' : 'stopPlay';
	setCallState(NO_CALL);
	if (webRtcPeer) {
                

                webRtcPeer.dispose();
                webRtcPeer = null;
   

		if (!message) {
			var message = {
				id : stopMessageId
			}
			sendMessage(message);
		}
                
           
	}
	hideSpinner(localVideo, remoteVideo);
        miniVideo.display = 'block';
}

function onError() {
	setCallState(NO_CALL);
}

function onIceCandidate(candidate) {
	console.log("Local candidate " + JSON.stringify(candidate));

	var message = {
		id : 'onIceCandidate',
		candidate : candidate
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

function disableButton(id) {
	$(id).attr('disabled', true);
	$(id).removeAttr('onclick');
}

function enableButton(id, functionName) {
	$(id).attr('disabled', false);
	$(id).attr('onclick', functionName);
}
function activate(id) {
	$(id).removeClass("hidden").addClass( "active" );
}
function deactivate(id) {
	$(id).removeClass( "active" ).addClass("hidden");
}


//function registerUser(name) {
//	setRegisterState(REGISTERING);
//
//	var message = {
//		id : 'register',
//		name : name
//	};
//	sendMessage(message);
//}


/**
 * Lightbox utility (to display media pipeline image in a modal dialog)
 
$(document).delegate('*[data-toggle="lightbox"]', 'click', function(event) {
	event.preventDefault();
	$(this).ekkoLightbox();
});*/


//function onOfferPlay(error, offerSdp) {
//	if (error) {
//		return console.error('Error generating the offer');
//	}
//	console.log('Invoking SDP offer callback function');
//	var message = {
//		id : 'play',
//		user : document.getElementById('peer').value,
//		sdpOffer : offerSdp
//	};
//	sendMessage(message);
//}



//function updateRegisteredUsers(userList) {
//	console.log("User list: " + userList);
//	var peers = $("#peer");
//	var name;
//	for (var i = 0; i < userList.length; i++) {
//		name = userList[i];
//		if (name != $('#name').val()) {
//			peers.append($("<option />").val(name).text(name));
//		}
//	}
//}

//function playResponse(message) {
//	if (message.response != 'accepted') {
//		hideSpinner(remoteVideo);
//		document.getElementById('miniVideo').style.display = 'block';
//		alert(message.error);
//		document.getElementById('peer').focus();
//		setCallState(NO_CALL);
//	} else {
//		setCallState(IN_PLAY);
//		webRtcPeer.processAnswer(message.sdpAnswer, function(error) {
//			if (error)
//				return console.error(error);
//		});
//	}
//}

//
//function play() {
//	var peer = document.getElementById('peer').value;
//	if (peer == '') {
//		window.alert('You must specify the peer name');
//		document.getElementById('peer').focus;
//		return;
//	}
//
//	document.getElementById('miniVideo').display = 'none';
//	setCallState(IN_PLAY);
//	showSpinner(remoteVideo);
//
//	var options = {
//		remoteVideo: remoteVideo,
//		onicecandidate: onIceCandidate
//	}
//	webRtcPeer = new kurentoUtils.WebRtcPeer.WebRtcPeerRecvonly(options,
//		function(error) {
//			if (error) {
//				return console.error(error);
//			}
//			this.generateOffer(onOfferPlay);
//		}
//	)
//}
//


//function playEnd() {
//	setCallState(NO_CALL);
//	hideSpinner(localVideo, remoteVideo);
//	document.getElementById('miniVideo').style.display = 'block';
//}
