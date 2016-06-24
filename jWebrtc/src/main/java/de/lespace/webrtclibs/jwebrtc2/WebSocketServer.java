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
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kurento.client.EventListener;
import org.kurento.client.IceCandidate;
import org.kurento.client.OnIceCandidateEvent;
import org.kurento.jsonrpc.JsonUtils;
import org.slf4j.LoggerFactory;

/** 
 * @ServerEndpoint gives the relative name for the end point
 * This will be accessed via ws://localhost:8080/EchoChamber/echo
 * Where "localhost" is the address of the host,
 * "EchoChamber" is the name of the package
 * and "echo" is the address to access this class from the server
 */
@ServerEndpoint("/ws") 
public class WebSocketServer {
    
    private static final Gson gson = new GsonBuilder().create();   
    private final ConcurrentHashMap<String, CallMediaPipeline> pipelines = new ConcurrentHashMap(); //<String, CallMediaPipeline>
    public static UserRegistry registry = new UserRegistry();
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(WebSocketServer.class);
    
    @OnOpen
    public void onOpen(Session session){
        System.out.println("apprtcWs opened with sessionId " + session.getId()); 
    }
    
    @OnError
    public void onError(Session session, Throwable error){
        System.out.println("apprtcWs Error " + session.getId() );
        if(error!=null)System.err.println(" error:"+ error); 
    }
    
    /**
     * The user closes the connection.
     * Note: you can't send messages to the client from this method
     */
    @OnClose
    public void onClose(Session session){
        System.out.println("apprtcWs closed connection " + session.getId() + " ");
    }
 
    /**
     * When a user sends a message to the server, this method will intercept the message
     * and allow us to react to it. For now the message is read as a String.
     */
    @OnMessage
    public void onMessage(String _message, Session session){
        
        System.out.println("apprtcWs " + session.getId() + " received message "+ _message);
        JsonObject jsonMessage = gson.fromJson(_message, JsonObject.class);
        UserSession user = registry.getBySession(session);

        if (user != null) {
          log.debug("Incoming message from user '{}': {}", user.getName(), jsonMessage);
        } else {
          log.debug("Incoming message from new user: {}", jsonMessage);
        }

        switch (jsonMessage.get("id").getAsString()) {
          case "register":
            try {
              register(session, jsonMessage);
            } catch (Exception e) {
              handleErrorResponse(e, session, "resgisterResponse");
            }
            break;
          case "call":
            try {
              call(user, jsonMessage);
            } catch (Exception e) {
              handleErrorResponse(e, session, "callResponse");
            }
            break;
          case "incomingCallResponse":
            {
                try{
                    incomingCallResponse(user, jsonMessage);
                } catch (IOException ex) {
                    Logger.getLogger(WebSocketServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            break;
          case "onIceCandidate": {
            JsonObject candidate = jsonMessage.get("candidate").getAsJsonObject();
            if (user != null) {
              IceCandidate cand = new IceCandidate(candidate.get("candidate").getAsString(),
                  candidate.get("sdpMid").getAsString(), candidate.get("sdpMLineIndex").getAsInt());
              user.addCandidate(cand);
            }
            break;
          }
          case "stop":
            {
                try {
                    stop(session);
                } catch (IOException ex) {
                    Logger.getLogger(WebSocketServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            break;
          default:
            break;
        }
    }

    private void handleErrorResponse(Exception throwable, Session session, String responseId){
        try {
            stop(session);
        } catch (IOException ex) {
            Logger.getLogger(WebSocketServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        log.error(throwable.getMessage(), throwable);
        JsonObject response = new JsonObject();
        response.addProperty("id", responseId);
        response.addProperty("response", "rejected");
        response.addProperty("message", throwable.getMessage());
        try {
            session.getBasicRemote().sendText(response.toString());
        } catch (IOException ex) {
            Logger.getLogger(WebSocketServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

  private void register(Session session, JsonObject jsonMessage) throws IOException {
    String name = jsonMessage.getAsJsonPrimitive("name").getAsString();
    Logger.getLogger(WebSocketServer.class.getName()).log(Level.INFO, "register called:"+name);
    UserSession caller = new UserSession(session, name);
    String responseMsg = "accepted";
    if (name.isEmpty()) {
      responseMsg = "rejected: empty user name";
    } else if (registry.exists(name)) {
      responseMsg = "rejected: user '" + name + "' already registered";
    } else {
      registry.register(caller);
    }

    JsonObject response = new JsonObject();
    response.addProperty("id", "resgisterResponse");
    response.addProperty("response", responseMsg);
    caller.sendMessage(response);
  }

  private void call(UserSession caller, JsonObject jsonMessage) throws IOException {
    String to = jsonMessage.get("to").getAsString();
    String from = jsonMessage.get("from").getAsString();
    JsonObject response = new JsonObject();

    if (registry.exists(to)) {
      
      caller.setSdpOffer(jsonMessage.getAsJsonPrimitive("sdpOffer").getAsString());
      caller.setCallingTo(to);

      response.addProperty("id", "incomingCall");
      response.addProperty("from", from);

      UserSession callee = registry.getByName(to);
      callee.sendMessage(response);
      callee.setCallingFrom(from);
    } else {
      
      response.addProperty("id", "callResponse");
      response.addProperty("response", "rejected: user '" + to + "' is not registered");

      caller.sendMessage(response);
    }
  }

  private void incomingCallResponse(final UserSession callee, JsonObject jsonMessage)
      throws IOException {
    
    String callResponse = jsonMessage.get("callResponse").getAsString();
    String from = jsonMessage.get("from").getAsString();
    final UserSession calleer = registry.getByName(from);
    String to = calleer.getCallingTo();

    if ("accept".equals(callResponse)) {
      log.debug("Accepted call from '{}' to '{}'", from, to);

      CallMediaPipeline pipeline = null;
      try {
        pipeline = new CallMediaPipeline(Utils.kurentoClient());
        pipelines.put(calleer.getSessionId(), pipeline);
        pipelines.put(callee.getSessionId(), pipeline);

        callee.setWebRtcEndpoint(pipeline.getCalleeWebRtcEp());
        pipeline.getCalleeWebRtcEp()
            .addOnIceCandidateListener(new EventListener<OnIceCandidateEvent>() {
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
                  log.debug(e.getMessage());
                }
              }
            });

        calleer.setWebRtcEndpoint(pipeline.getCallerWebRtcEp());
        pipeline.getCallerWebRtcEp()
            .addOnIceCandidateListener(new EventListener<OnIceCandidateEvent>() {

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
                  log.debug(e.getMessage());
                }
              }
            });

        String calleeSdpOffer = jsonMessage.get("sdpOffer").getAsString();
        String calleeSdpAnswer = pipeline.generateSdpAnswerForCallee(calleeSdpOffer);
        JsonObject startCommunication = new JsonObject();
        startCommunication.addProperty("id", "startCommunication");
        startCommunication.addProperty("sdpAnswer", calleeSdpAnswer);

        synchronized (callee) {
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
          calleer.sendMessage(response);
        }

        pipeline.getCallerWebRtcEp().gatherCandidates();

      } catch (Throwable t) {
          
        log.error(t.getMessage(), t);

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

  public void stop(Session session) throws IOException {
    
    String sessionId = session.getId();
    
    if (pipelines.containsKey(sessionId)) {
       
      CallMediaPipeline pipeline = pipelines.remove(sessionId);
      pipeline.release();

      // Both users can stop the communication. A 'stopCommunication'
      // message will be sent to the other peer.
      UserSession stopperUser = registry.getBySession(session);
      if (stopperUser != null) {
        UserSession stoppedUser = (stopperUser.getCallingFrom() != null)
            ? registry.getByName(stopperUser.getCallingFrom())
            : stopperUser.getCallingTo() != null ? registry.getByName(stopperUser.getCallingTo())
                : null;

        if (stoppedUser != null) {
          JsonObject message = new JsonObject();
          message.addProperty("id", "stopCommunication");
          stoppedUser.sendMessage(message);
          stoppedUser.clear();
        }
        stopperUser.clear();
      }

    }
  }



 



     
   
}