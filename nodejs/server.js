/*
 * (C) Copyright 2016 Ape Unit GmbH (http://apeunit.com/)
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Developed by Max Dörfler <max@apeunit.com>
 */

var express = require('express');
var session = require('express-session');
var cookieParser = require('cookie-parser');
var bodyParser = require('body-parser');
var ws = require('ws');
var kurento = require('kurento-client');
var shortid = require('shortid');
var config = require('./config');

var Sender = require('./sender');
var Receiver = require('./receiver');
var Room = require('./room');

// var as_uri = config.as_uri;
var serverUrl = config.appRTCUrl;
var ws_uri = config.ws_uri;
var port = config.port;

var kurentoClient = null;
var rooms = [];

var app = express();
app.use(bodyParser.json({
	type: 'text'
}));

app.use(cookieParser());

var sessionHandler = session({
	secret: 'none',
	rolling: true,
	resave: true,
	saveUninitialized: true
});

app.use(sessionHandler);

var server = app.listen(port, function() {
	console.log('Server started on port', port);
});

app.use(function(req, res, next) {
	console.log(req.protocol, req.method, req.path, req.get('Content-Type'));
	next();
});


var apprtcWs = new ws.Server({
	server: server,
	path: '/ws'
});

apprtcWs.on('connection', function(ws) {
	var sessionId = null;
	var request = ws.upgradeReq;
	var response = {
		writeHead: {}
	};

	sessionHandler(request, response, function(err) {
		sessionId = request.session.id;
		console.log('apprtcWs received with sessionId ' + sessionId);
	});

	ws.on('error', function(error) {
		console.log('apprtcWs Connection ' + sessionId + ' error', error);
		getRoomBySession(sessionId, function(err, room) {
			stopReceive(room);
			stopSend(room);
		});
	});

	ws.on('close', function() {
		console.log('apprtcWs Connection ' + sessionId + ' closed');
		getRoomBySession(sessionId, function(err, room) {
			stopReceive(room);
			stopSend(room);
		});
	});

	ws.on('message', function(_message) {
		var message = JSON.parse(_message);
		console.log('apprtcWs ' + sessionId + ' received message ', message);
		var clientId = message.clientid ? message.clientid : "empty";
		var roomname = message.roomid ? message.roomid : "emptyID";
	
		switch (message.cmd) {
			case 'register':
				console.log('register called');
				getRoom(roomname, function(err, room) {
					if (err) {
						console.error(err);
						ws.send(JSON.stringify({
							msg: {},
							error: err
						}));
					}
					
					if (!room) {
						console.error("Room not found");
						ws.send(JSON.stringify({
							msg: {},
							error: 'Room not found'
						}));
					}

					if (!room.sender) {
						room.sender = new Sender({
							sessionId: sessionId,
							clientId: clientId,
							websocket: ws
						});
					} else {
						room.sender.websocket = ws;
						room.sender.clientId = clientId;
						room.sender.sessionId = sessionId;
					}

					// console.log('sender created', room.sender);
					// console.log(room);
					//TODO: what if already offered?
					if (room.senderSdpOffer) {
						console.log('TODO: got the sdpOffer first');
					}
				});
				break;

			case 'startWebRtc':
				
				var sdpOffer = message.sdpOffer;
				var roomName = message.roomName;
				getRoom(roomName, function(err, room) {
					if (!room) {
						console.log('Room not found sending error via websocket!');
						return ws.send(JSON.stringify({
							id: 'error',
							message: 'Room not found'
						}));
					}
					console.log('starting WebRTC anyways');
					// sessionId = request.session.id;
					startWebRtc(room, sessionId, ws, sdpOffer, function(error, sdpAnswer) {
						if (error) {
							console.log(error);
							return ws.send(JSON.stringify({
								id: 'error',
								message: error
							}));
						}
						console.log("startWebRtc response:", sdpAnswer);
						ws.send(JSON.stringify({
							id: 'startResponse',
							sdpAnswer: sdpAnswer
						}));
					});
				});
				break;

			case 'onIceCandidate':
				var roomName = message.roomName;
				getRoom(roomName, function(err, room) {
					if (!room) {
						return ws.send(JSON.stringify({
							id: 'error',
							message: 'Room not found'
						}));
					}
					onIceCandidate(room, sessionId, message.candidate);
				});
				break;

			case 'stop':
				var roomName = message.roomName;
				getRoom(roomName, function(err, room) {
					if (room) {
						stopReceive(room);
					}
				});
				break;

			default:
				console.log('something else called');
		}
	});
});

app.all('/join/:roomname', function(req, res) {
  console.log('join called', req.body);
  var roomName = req.params.roomname ? req.params.roomname : "empty";

	//create room
	getRoom(roomName, function(err, room) {
		if (err) {
			console.error(err);
			return res.json({
				"result": "ERROR"
			});
		}
		if (!room) {
			room = new Room({
				roomName: roomName
			});
			rooms.push(room);
		}
		console.log(room);

		//generate a client ID
		var clientId = shortid.generate();

		var response = {
	    "params": {
	      "is_initiator": "true",
	      "room_link": "http://" + serverUrl + "/r/" + roomName,
	      "version_info": "{\"gitHash\": \"029b6dc4742cae3bcb6c5ac6a26d65167c522b9f\", \"branch\": \"master\", \"time\": \"Wed Dec 9 16:08:29 2015 +0100\"}",
	      "messages": [],
	      "error_messages": [],
	      "client_id": clientId,
	      "bypass_join_confirmation": "false",
	      "media_constraints": "{\"audio\": true, \"video\": true}",
	      "include_loopback_js": "",
	      "turn_url": "http://" + serverUrl + "/turn",
	      "is_loopback": "false",
	      "wss_url": "ws://" + serverUrl + "/ws",
	      "pc_constraints": "{\"optional\": []}",
	      "pc_config": "{\"rtcpMuxPolicy\": \"require\", \"bundlePolicy\": \"max-bundle\", \"iceServers\": []}",
	      "wss_post_url": "http://" + serverUrl + "",
	      "offer_options": "{}",
	      "warning_messages": [],
	      "room_id": roomName,
	      "turn_transports": ""
	    },
	    "result": "SUCCESS"
	  };
		res.json(response);
	});
});

app.all('/leave/:roomname/:clientId', function(req, res) {
  console.log('leave called', req.body);
  var roomName = req.params.roomname ? req.params.roomname : "empty";
  var clientId = req.params.clientId ? req.params.clientId : "emptyID";
	getRoom(roomName, function(err, room) {
		if (room) {
			stopSend(room);
		}
		res.send('todo');
	});
});

app.all('/turn', function(req, res) {
  console.log('turn called', req.body);

  var response = config.turn;
  res.json(response);
});

app.all('/message/:roomname/:clientId', function(req, res) {
	console.log('message called', req.body.type);
	var roomName = req.params.roomname ? req.params.roomname : "empty";
	var clientId = req.params.clientId ? req.params.clientId : "emptyID";
	var message = req.body;
	getRoom(roomName, function(err, room) {
		if (err) {
			res.json({
				"result": "ERROR",
				"error": err
			});
		}
		if (!room) {
			//I dunno
		}

		// console.log(message, roomName, id);
		switch (message.type) {
			case 'candidate':
				var sender = room.sender;
				console.log('candidate', message.candidate);
				var rewrittenCandidate = {
					candidate: message.candidate,
					sdpMid: 'sdparta_0',
					sdpMLineIndex: message.label
				};
				// console.log(rewrittenCandidate);

				var candidate = kurento.register.complexTypes.IceCandidate(rewrittenCandidate);

				if (sender.endpoint) {
					console.info('appRTC Ice Candidate addIceCandidate', candidate);
					sender.endpoint.addIceCandidate(candidate);
				} else {
					//TODO:
					console.info('appRTC Ice Candidate  Queueing candidate', sender.candidateQueue);
					sender.candidateQueue.push(candidate);
				}
				break;
			case 'offer':
				if (room.sender && room.sender.websocket) {
					var sender = room.sender;
					console.log('yay websocket is present');
					
					var onCandidate = function(event) {
						// console.log("onCandidate");
					var candidate = kurento.register.complexTypes.IceCandidate(event.candidate);
						var candidateAnswer = {
							msg: {
								type: 'candidate',
								label: event.candidate.sdpMLineIndex,
								id: event.candidate.sdpMid,
								candidate: event.candidate.candidate
							},
							error: ''
						};
						sender.websocket.send(JSON.stringify(candidateAnswer));
					};

					startSendWebRtc(room, message.sdp, onCandidate, function(error, sdpAnswer) {
						console.log('started webrtc in POST', error);
						var sendSdpAnswer = {
							msg: {
								type: 'answer',
								sdp: sdpAnswer
							},
							error: ''
						};

						sender.websocket.send(JSON.stringify(sendSdpAnswer));
						// sendSendStatus();
					});
				} else {
					console.log('no websocket is present');
					room.senderSdpOffer = message.sdp;
				}
				break;
			default:
				console.log('default');
		}
		//just send success
		res.json({
			"result": "SUCCESS"
		});
	});
});

function getKurentoClient(callback) {
	if (kurentoClient !== null) {
		return callback(null, kurentoClient);
	}
	kurento(ws_uri, function(error, _kurentoClient) {
		if (error) {
			console.log("Could not find media server at address " + ws_uri);
			return callback("Could not find media server at address" + ws_uri + ". Exiting with error " + error);
		}

		kurentoClient = _kurentoClient;
		callback(null, kurentoClient);
	});
}

function getPipeline(room, callback) {
	if (!room) {
		return callback('No Room');
	}

	if (room.pipeline !== null) {
		console.log('saved pipeline');
		return callback(null, room.pipeline);
	}
	getKurentoClient(function(error, kurentoClient) {
		if (error) {
			return callback(error);
		}
		kurentoClient.create('MediaPipeline', function(error, _pipeline) {
			if (error) {
				return callback(error);
			}
			room.pipeline = _pipeline;
			return callback(null, room.pipeline);
		});
	});
};

function getRoom(roomName, callback) {
	console.log("Looking for room:", roomName);
	for (var i = 0; i < rooms.length; i++) {
		if (rooms[i].roomName == roomName) {
			console.log('found room'+roomName);
			return callback(null, rooms[i]);
		}
		console.log('did not find room'+roomName);
	}
	console.log('did not find room'+roomName);
	return callback(null, null);
};

function getRoomBySession(sessionId, callback) {
	console.log("Looking for room with session:", sessionId);
	for (var i = 0; i < rooms.length; i++) {
		if (rooms[i].sender && rooms[i].sender.sessionId == sessionId) {
			return callback(null, rooms[i]);
		}
	}
	return callback(null, null);
};

function startSendWebRtc(room, sdpOffer, onCandidate, callback) {
	//create webrtc endpoint
	getPipeline(room, function(error, pipeline) {
		if (error) {
			return callback(error);
		}
		var sender = room.sender;
		pipeline.create('WebRtcEndpoint', function(error, _webRtcEndpoint) {
			if (error) {
				return callback(error);
			}
			console.log("got send webrtc webRtcEndpoint");
			// rtpEndpoint = _webRtcEndpoint;
			sender.endpoint = _webRtcEndpoint;

			console.log("read queue:", sender.candidateQueue);
			if (sender.candidateQueue) {
				while (sender.candidateQueue.length) {
					console.log("adding candidate from queue");
					var candidate = sender.candidateQueue.shift();
					sender.endpoint.addIceCandidate(candidate);
				}
			}

			sender.endpoint.processOffer(sdpOffer, function(error, sdpAnswer) {
				if (error) {
					sender.endpoint.release();
					return callback(error);
				}

				sender.endpoint.on('OnIceCandidate', function(event) {
					onCandidate(event);
				});

				sender.endpoint.gatherCandidates(function(error) {
					if (error) {
						stopReceive(sessionId);
						return callback(error);
					}
				});

				console.log("sending sdp answer");
				return callback(null, sdpAnswer);
			});
		});
	});
};

function startWebRtc(room, sessionId, ws, sdpOffer, callback) {
	
	if (!room) {
		return callback('startWebRtc: No Room');
	}

	var sender = room.sender;
	if (!sender || !sender.endpoint) {
		return callback('No Sending Endpoint');
	}

	var receiver = room.getOrCreateReceiver({
		sessionId: sessionId,
		websocket: ws
	});
	if (!receiver) {
		return callback('Error getting or creating Receiver');
	}
	var pipeline = room.pipeline;
	console.log('got pipeline');
	//create webrtc endpoint
	console.log("Creating WebRtcEndpoint");
	pipeline.create('WebRtcEndpoint', function(error, _webRtcEndpoint) {
		if (error) {
			return callback(error);
		}
		console.log("got webRtcEndpoint");
		// webRtcEndpoint = _webRtcEndpoint;
		receiver.videoEndpoint = _webRtcEndpoint;

		if (receiver.candidateQueueVideo) {
			while (receiver.candidateQueueVideo.length) {
				console.log("adding candidate from queue");
				var candidate = receiver.candidateQueueVideo.shift();
				receiver.videoEndpoint.addIceCandidate(candidate);
			}
		}

		receiver.videoEndpoint.processOffer(sdpOffer, function(error, sdpAnswer) {
			if (error) {
				receiver.videoEndpoint.release();
				return callback(error);
			}
			sender.endpoint.connect(receiver.videoEndpoint, function(error) {
				if (error) {
					receiver.videoEndpoint.release();
					console.log(error);
					return callback(error);
				}

				receiver.videoEndpoint.on('OnIceCandidate', function(event) {
					var candidate = kurento.register.complexTypes.IceCandidate(event.candidate);
					ws.send(JSON.stringify({
						id: 'iceCandidate',
						candidate: candidate
					}));
				});

				receiver.videoEndpoint.gatherCandidates(function(error) {
					if (error) {
						stopReceive(sessionId);
						return callback(error);
					}
				});

				receiver.videoEndpoint.connect(sender.endpoint, function(error) {
					if (error) {
						receiver.videoEndpoint.release();
						console.log(error);
						return callback(error);
					}
				});

				return callback(null, sdpAnswer);
			});
		});
	});
}

function onIceCandidate(room, sessionId, _candidate) {
	var candidate = kurento.register.complexTypes.IceCandidate(_candidate);

	console.log('onIceCandidate called');
	var receiver = room.receivers[sessionId];
	if (!receiver) {
		console.log('onIceCandidate no receivers');
		return null;//callback('Error getting Receiver');
	}
	//console.log('onIceCandidate receiver', receiver);

	if (receiver.videoEndpoint) {
		console.info('Adding candidate');
		receiver.videoEndpoint.addIceCandidate(candidate);
	} else {
		console.info('Queueing candidate');
		receiver.candidateQueueVideo.push(candidate);
	}
}

function stopSend(room) {
  console.log("stopSend");
	if (!room) {
		console.error("no room");
		return;
	}
	if (room.pipeline) {
		room.pipeline.release();
	}
	if (room.sender && room.sender.endpoint) {
		room.sender.endpoint.release();
		room.sender.endpoint = null;
	}
	// room.sender = null;
	var index = rooms.indexOf(room);
	if (index > -1) {
		rooms.splice(index, 1);
	}
  //TODO: release all receivers?
};

function stopReceive(room) {
	console.log('TODO: stopReceive', room);
	if (!room) {
		console.error("stopReceive no room");
		return;
	}
  // var receiver = receivers[sessionId];
  // if (receiver && receiver.videoEndpoint) {
  //   receiver.videoEndpoint.release();
  //   console.log("Released receiving videoEndpoint");
  // }
  // if (receiver && receiver.audioEndpoint) {
  //   receiver.audioEndpoint.release();
  //   console.log("Released receiving audioEndpoint");
  // }
}

app.use(express.static('static'));
