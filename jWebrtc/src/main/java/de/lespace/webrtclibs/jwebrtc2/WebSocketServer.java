package de.lespace.webrtclibs.jwebrtc2;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kurento.client.EventListener;
import org.kurento.client.IceCandidate;
import org.kurento.client.OnIceCandidateEvent;
import org.kurento.jsonrpc.JsonUtils;
import org.slf4j.LoggerFactory;

/**
 * @ServerEndpoint gives the relative name for the end point This will be
 *                 accessed via ws://localhost:8080/EchoChamber/echo Where
 *                 "localhost" is the address of the host, "EchoChamber" is the
 *                 name of the package and "echo" is the address to access this
 *                 class from the server
 */
@ServerEndpoint("/ws")
public class WebSocketServer {

	private static final Gson gson = new GsonBuilder().create();
	private final ConcurrentHashMap<String, CallMediaPipeline> pipelines = new ConcurrentHashMap(); // <String,
																									// CallMediaPipeline>
	public static UserRegistry registry = new UserRegistry();
	private static final org.slf4j.Logger log = LoggerFactory.getLogger(WebSocketServer.class);

	@OnOpen
	public void onOpen(Session session) {
		// System.out.println("apprtcWs opened with sessionId " +
		// session.getId());
		log.debug("apprtcWs opened with sessionId {}", session.getId());
	}

	@OnError
	public void onError(Session session, Throwable error) {
		// System.out.println("apprtcWs Error " + session.getId() );
		log.debug("apprtcWs Error [{}]", session.getId());
		error.getStackTrace();
		if (error != null) {
			// System.err.println(" error:"+ error);
			log.error("Error: {}", error.getLocalizedMessage());
		}
	}

	/**
	 * The user closes the connection. Note: you can't send messages to the
	 * client from this method
	 */
	@OnClose
	public void onClose(Session session) {
		// System.out.println("apprtcWs closed connection " + session.getId() +
		// " ");
		log.debug("apprtcWs closed connection [{}]", session.getId());
		try {
			stop(session, true);
		} catch (IOException ex) {
			// Logger.getLogger(WebSocketServer.class.getName()).log(Level.SEVERE,
			// null, ex);
			log.error(ex.getLocalizedMessage(), ex);
		}
	}

	/**
	 * When a user sends a message to the server, this method will intercept the
	 * message and allow us to react to it. For now the message is read as a
	 * String.
	 * 
	 * @param _message
	 *            the json message
	 * @param session
	 *            the websocket session
	 */
	@OnMessage
	public void onMessage(String _message, Session session) {

//		System.out.println("apprtcWs " + session.getId() + " received message " + _message);
		log.debug("apprtcWs [{}] received message: {}", session.getId(), _message);
		JsonObject jsonMessage = gson.fromJson(_message, JsonObject.class);

		UserSession user = registry.getBySession(session);

		if (user != null) {
			log.debug("Incoming message from user '{}': {}", user.getName(), jsonMessage);
		} else {
			log.debug("Incoming message from new user: {}", jsonMessage);
		}

		switch (jsonMessage.get("id").getAsString()) {
		case "appConfig":
			try {
				appConfig(session, jsonMessage);
			} catch (IOException e) {
				handleErrorResponse(e, session, "appConfigResponse");
			}
			break;
		case "register":
			try {
				boolean registered = register(session, jsonMessage);
				// if(registered)
				sendRegisteredUsers();
			} catch (Exception e) {
				e.printStackTrace();
				handleErrorResponse(e, session, "registerResponse");
			}
			break;
		case "call":
			try {
				call(user, jsonMessage);
			} catch (Exception e) {
				handleErrorResponse(e, session, "callResponse");
			}
			break;
		case "incomingCallResponse": {
			try {
				incomingCallResponse(user, jsonMessage);
			} catch (IOException ex) {
				Logger.getLogger(WebSocketServer.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
			break;
		case "onIceCandidate": {

			if (user != null) {
				// this is how it works when it comes from a android
				if (jsonMessage.has("sdpMLineIndex") && jsonMessage.has("sdpMLineIndex")) {

//					System.out.println("apprtcWs candidate is coming from android or ios ");
					log.debug("apprtcWs candidate is coming from android or ios");

					IceCandidate candAndroid = new IceCandidate(jsonMessage.get("candidate").getAsString(),
							jsonMessage.get("sdpMid").getAsString(), jsonMessage.get("sdpMLineIndex").getAsInt());

					user.addCandidate(candAndroid);
				} else {
					// this is how it works when it comes from a browser
//					System.out.println("apprtcWs candidate is coming from web");
					log.debug("apprtcWs candidate is coming from web");
					
					JsonObject candidate = jsonMessage.get("candidate").getAsJsonObject();
					IceCandidate candWeb = new IceCandidate(candidate.get("candidate").getAsString(),
							candidate.get("sdpMid").getAsString(), candidate.get("sdpMLineIndex").getAsInt());
					user.addCandidate(candWeb);
				}

			}
			break;
		}
		case "stop": {
			try {
				stop(session, false);
			} catch (IOException ex) {
//				Logger.getLogger(WebSocketServer.class.getName()).log(Level.SEVERE, null, ex);
				log.error(ex.getLocalizedMessage(), ex);
			}
		}
			break;
		default:
			break;
		}
	}

	private void handleErrorResponse(Exception throwable, Session session, String responseId) {
		try {
			stop(session, false);
		} catch (IOException ex) {
//			Logger.getLogger(WebSocketServer.class.getName()).log(Level.SEVERE, null, ex);
			log.error(ex.getLocalizedMessage(), ex);
		}
		log.error(throwable.getMessage(), throwable);
		JsonObject response = new JsonObject();
		response.addProperty("id", responseId);
		response.addProperty("response", "rejected");
		response.addProperty("message", throwable.getMessage());
		try {
			session.getBasicRemote().sendText(response.toString());
		} catch (IOException ex) {
//			Logger.getLogger(WebSocketServer.class.getName()).log(Level.SEVERE, null, ex);
			log.error(ex.getLocalizedMessage(), ex);
		}
	}

	private void appConfig(Session session, JsonObject jsonMessage) throws IOException {

		// when removing unecessary params - check android for reading
		// nullpointer
		String responseJSON = "{" + "\"params\" : {" + "\"is_initiator\": true,"
				+ "\"version_info\": {\"gitHash\": \"029b6dc4742cae3bcb6c5ac6a26d65167c522b9f\", \"branch\": \"master\", \"time\": \"Wed Dec 9 16:08:29 2015 +0100\"},"
				+ "\"messages\": []," + "\"error_messages\": []," + "\"client_id\": \"\","
				+ "\"bypass_join_confirmation\": \"false\","
				+ "\"media_constraints\": {\"audio\": true, \"video\": true}," + "\"include_loopback_js\": \"\","
				+ "\"turn_url\": \"http://" + Config.serverUrl + "/turn\"," + "\"is_loopback\": \"false\","
				+ "\"pc_constraints\": {\"optional\": []},"
				+ "\"pc_config\": {\"rtcpMuxPolicy\": \"require\", \"bundlePolicy\": \"max-bundle\", \"iceServers\": "
				+ Config.turn + "}," + "\"offer_options\": {}," + "\"warning_messages\": []," +
				// "\"room_id\": "+jsonMessage.get("roomId").toString()+","+
				"\"turn_transports\": \"\"" + "}," + "\"result\": \"SUCCESS\"" + "}";

		session.getBasicRemote().sendText(responseJSON);
		Logger.getLogger(WebSocketServer.class.getName()).log(Level.INFO, "send app config to :" + session.getId());
		log.info("send app config to: {}", session.getId());
	}

	private boolean register(Session session, JsonObject jsonMessage) throws IOException {

		String name = jsonMessage.getAsJsonPrimitive("name").getAsString();
		Logger.getLogger(WebSocketServer.class.getName()).log(Level.INFO, "register called:" + name);
		log.info("register called: {}", name);
		
		boolean registered = false;
		UserSession caller = new UserSession(session, name);
		String response = "accepted";
		String message = "";
		if (name.isEmpty()) {
			response = "rejected";
			message = "empty user name";
		} else if (registry.exists(name)) {
			response = "skipped";
			message = "user " + name + " already registered";
		} else {
			registry.register(caller);
			registered = true;
		}

		JsonObject responseJSON = new JsonObject();
		responseJSON.addProperty("id", "registerResponse");
		responseJSON.addProperty("response", response);
		responseJSON.addProperty("message", message);
		caller.sendMessage(responseJSON);

//		Logger.getLogger(WebSocketServer.class.getName()).log(Level.INFO, "Sent response: " + responseJSON);
		log.info("Sent response: {}", responseJSON);
		return registered;
	}

	private void sendRegisteredUsers() throws IOException {
		List<String> userList = registry.getRegisteredUsers();
		String userListJson = new Gson().toJson(userList);

		JsonObject responseJSON = new JsonObject();
		responseJSON.addProperty("id", "registeredUsers");
		responseJSON.addProperty("response", userListJson);
		responseJSON.addProperty("message", "");

//		Logger.getLogger(WebSocketServer.class.getName()).log(Level.INFO,
//				"Updating user list on clients: " + responseJSON);
		log.info("Updating user list on clients: {}", responseJSON);

		for (UserSession userSession : registry.getUserSessions()) {
			userSession.sendMessage(responseJSON);
		}
	}

	private void call(UserSession caller, JsonObject jsonMessage) throws IOException {
		String to = jsonMessage.get("to").getAsString();
		String from = jsonMessage.get("from").getAsString();

//		System.out.println("call from :" + from + " to:" + to);
		log.info("call from [{}] to [{}]", from, to);

		JsonObject response = new JsonObject();

		if (registry.exists(to)) {
			caller.setSdpOffer(jsonMessage.getAsJsonPrimitive("sdpOffer").getAsString());
			caller.setCallingTo(to);

			response.addProperty("id", "incomingCall");
			response.addProperty("from", from);

			UserSession callee = registry.getByName(to);
			System.out.println("callee:" + callee.getName() + " sending response:" + response.toString());
			log.info("callee [{}] sending response [{}]", callee.getName(), response.toString());

			callee.sendMessage(response);
			callee.setCallingFrom(from);
		} else {
//			System.out.println("to does not exist!");
			log.warn("[{}] does not exist! Rejecting call.", to);
			
			response.addProperty("id", "callResponse");
			response.addProperty("response", "rejected: user '" + to + "' is not registered");

			caller.sendMessage(response);
		}
	}

	private void incomingCallResponse(final UserSession callee, JsonObject jsonMessage) throws IOException {

		String callResponse = jsonMessage.get("callResponse").getAsString();
		String from = jsonMessage.get("from").getAsString();
		final UserSession calleer = registry.getByName(from);
		String to = calleer.getCallingTo();

		if ("accept".equals(callResponse)) {

//			System.out.println("Accepted call from '" + from + "' to " + to + "");
			log.info("Accepted call from [{}] to [{}]", from, to);

			CallMediaPipeline pipeline = null;
			try {
				pipeline = new CallMediaPipeline(Utils.kurentoClient());
				pipelines.put(calleer.getSessionId(), pipeline);
				pipelines.put(callee.getSessionId(), pipeline);
				System.out.println("created both pipelines...");
				callee.setWebRtcEndpoint(pipeline.getCalleeWebRtcEp());
				pipeline.getCalleeWebRtcEp().addOnIceCandidateListener(new EventListener<OnIceCandidateEvent>() {
					@Override
					public void onEvent(OnIceCandidateEvent event) {
						JsonObject response = new JsonObject();
						response.addProperty("id", "iceCandidate");
						response.add("candidate", JsonUtils.toJsonObject(event.getCandidate()));
						try {
							synchronized (callee.getSession()) {
								callee.getSession().getBasicRemote().sendText(response.toString());
							}
						} catch (IOException e) {
							log.error(e.getMessage(), e);
						}
					}
				});

				calleer.setWebRtcEndpoint(pipeline.getCallerWebRtcEp());
//				System.out.println("created both webrtcendpoints...");
				log.debug("created both webrtcendpoints...");
				
				pipeline.getCallerWebRtcEp().addOnIceCandidateListener(new EventListener<OnIceCandidateEvent>() {

					@Override
					public void onEvent(OnIceCandidateEvent event) {
						JsonObject response = new JsonObject();
						response.addProperty("id", "iceCandidate");
						response.add("candidate", JsonUtils.toJsonObject(event.getCandidate()));
						try {
							synchronized (calleer.getSession()) {
								calleer.getSession().getBasicRemote().sendText(response.toString());
							}
						} catch (IOException e) {
							log.error(e.getMessage(), e);
						}
					}
				});

				System.out.println("preparing sending startCommunication to called person...");
				log.debug("preparing sending startCommunication to called person...");

				String calleeSdpOffer = jsonMessage.get("sdpOffer").getAsString();
				String calleeSdpAnswer = pipeline.generateSdpAnswerForCallee(calleeSdpOffer);
//				System.out.println("i have callee offer and answer as it seems");
				log.debug("i have callee offer and answer as it seems");

				JsonObject startCommunication = new JsonObject();
				startCommunication.addProperty("id", "startCommunication");
				startCommunication.addProperty("sdpAnswer", calleeSdpAnswer);

				synchronized (callee) {
//					System.out.println("sending startCommunication message to callee");
					log.debug("sending startCommunication message to callee");
					callee.sendMessage(startCommunication);
				}

				pipeline.getCalleeWebRtcEp().gatherCandidates();

				String callerSdpOffer = registry.getByName(from).getSdpOffer();
				String callerSdpAnswer = pipeline.generateSdpAnswerForCaller(callerSdpOffer);
				JsonObject response = new JsonObject();
				response.addProperty("id", "callResponse");
				response.addProperty("response", "accepted");
				response.addProperty("sdpAnswer", callerSdpAnswer);

				synchronized (calleer) {
//					System.out.println("sending callResponse message to caller");
					log.debug("sending callResponse message to caller");
					calleer.sendMessage(response);
				}

				pipeline.getCallerWebRtcEp().gatherCandidates();

			} catch (Throwable t) {

				log.error(t.getMessage(), t);
//				System.err.println("rejecting call reason:" + t.getMessage());
				log.warn("Rejecting call! Reason: {}", t.getMessage());

				if (pipeline != null) {
					pipeline.release();
				}

				pipelines.entrySet();
				pipelines.remove(calleer.getSessionId());
				pipelines.remove(callee.getSessionId());

				JsonObject response = new JsonObject();
				response.addProperty("id", "callResponse");
				response.addProperty("response", "rejected");
				calleer.sendMessage(response);

				response = new JsonObject();
				response.addProperty("id", "stopCommunication");
				callee.sendMessage(response);
			}

		} else {
			JsonObject response = new JsonObject();
			response.addProperty("id", "callResponse");
			response.addProperty("response", "rejected");
			calleer.sendMessage(response);
		}
	}

	public void stop(Session session, boolean killSession) throws IOException {

		String sessionId = session.getId();

		if (pipelines.containsKey(sessionId)) {
//			System.out.println("stopping  media connection of websocket id:" + sessionId);
			log.info("Stopping  media connection of websocket id [{}]", sessionId);
			
			CallMediaPipeline pipeline = pipelines.remove(sessionId);
			pipeline.release();

			// Both users can stop the communication. A 'stopCommunication'
			// message will be sent to the other peer.
			UserSession stopperUser = registry.getBySession(session);
			if (stopperUser != null) {
				UserSession stoppedUser = (stopperUser.getCallingFrom() != null)
						? registry.getByName(stopperUser.getCallingFrom())
						: stopperUser.getCallingTo() != null ? registry.getByName(stopperUser.getCallingTo()) : null;

				if (stoppedUser != null) {
					JsonObject message = new JsonObject();
					message.addProperty("id", "stopCommunication");
					stoppedUser.sendMessage(message);
					stoppedUser.clear();
				}
				stopperUser.clear();
			}

		}
		if (killSession) {
			System.out.println("killing usersession from of websocket id:" + sessionId);
			log.info("Killing usersession from of websocket id [{}]", sessionId);
			
			registry.removeBySession(session);
			sendRegisteredUsers(); // update userlist on all clients when
									// somebody disconnects
		} // remove usre from session must register again at the moment right or
			// not?

	}

}