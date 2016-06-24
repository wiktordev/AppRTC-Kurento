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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kurento.client.EventListener;
import org.kurento.client.IceCandidate;
import org.kurento.client.MediaPipeline;
import org.kurento.client.OnIceCandidateEvent;
import org.kurento.client.WebRtcEndpoint;
import org.kurento.jsonrpc.JsonUtils;

/** 
 * @ServerEndpoint gives the relative name for the end point
 * This will be accessed via ws://localhost:8080/EchoChamber/echo
 * Where "localhost" is the address of the host,
 * "EchoChamber" is the name of the package
 * and "echo" is the address to access this class from the server
 */
@ServerEndpoint("/ws") 
public class WebSocketServer {

    static Room createRoom(String roomName) {
        Room r = new Room(roomName);
        rooms.add(r);
        System.out.println("now rooms:"+rooms.toString()+" regisered.");
        return r;
    }
    
    private String sessionId;
    private Session session;
    private String serverUrl; //config.appRTCUrl;
    private String ws_uri; //config.ws_uri;
    private String port; //config.port;
    private static final Gson gson = new GsonBuilder().create();   
    private final String kurentoClient = null;
    public static List<Room> rooms = new ArrayList();
    private Room room;
    
    
    /**
     * @OnOpen allows us to intercept the creation of a new session.
     * The session class allows us to send data to the user.
     * In the method onOpen, we'll let the user know that the handshake was 
     * successful.
     */
    @OnOpen
    public void onOpen(Session session){
        this.sessionId = session.getId();
        this.session = session;
        System.out.println("apprtcWs opened with sessionId " + sessionId); 
    }
    
    @OnError
    public void onError(Session session, Throwable error){
        System.out.println("apprtcWs Error " + sessionId );
        if(error!=null)System.err.println(" error:"+ error); 
        //TODO 
        //getRoomBySession(sessionId)  
        //stopReceive(room)
        //stopSend(room()
    }
    
    /**
     * The user closes the connection.
     * Note: you can't send messages to the client from this method
     */
    @OnClose
    public void onClose(Session session){
        System.out.println("apprtcWs closed connection " + session.getId() + " ");
        //TODO 
        //getRoomBySession(sessionId)  
        //stopReceive(room)
        //stopSend(room()
    }
 
    /**
     * When a user sends a message to the server, this method will intercept the message
     * and allow us to react to it. For now the message is read as a String.
     */
    @OnMessage
    public void onMessage(String _message, Session session){
        
       // System.out.println("apprtcWs " + session.getId() + " received message "+ _message);
        JsonObject jsonMessage = gson.fromJson(_message, JsonObject.class);
        
        String cmd = jsonMessage.get("cmd").getAsString();
        switch (cmd) {
            case "register":
            {
                System.out.println("register callled");
                String roomid = jsonMessage.get("roomid").getAsString();
                
                Room room = getRoom(roomid);
                if(room ==null){
                    System.out.println("no such room  -creating room");
                    System.out.println("creating room");
                    room = createRoom(roomid);
                    System.out.println("room created!"+roomid);
                }
                
                room.sender.websocket = session;
                room.sender.sessionId = session.getId();
                
                System.out.println("registered sender with sessionId:"+room.sender.sessionId+" and room "+room.roomName);
            }
                break;
            case "startWebRtc":
            {
                String sdpOffer = jsonMessage.get("sdpOffer").getAsString();
                String roomName = jsonMessage.get("roomName").getAsString();
                
                Room room = getRoom(roomName);
                if(room ==null){
                   
                    System.err.println("responding to websocket: Room not found");
                    JsonObject response = new JsonObject();
                    response.add("msg", JsonUtils.toJsonObject(new JsonObject()));
                    response.addProperty("error", "Room not found");
                    synchronized (session) {
                    try {
                        session.getBasicRemote().sendText(response.toString());
                    } catch (IOException ex) {
                        Logger.getLogger(WebSocketServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                 }
                }else{
                    System.out.println("starting webrtc for room:"+roomName);
                    startWebRtc(room, sessionId, session, sdpOffer);
                }
                    
            }
                break;            
            case "onIceCandidate":
            {
                //System.out.println("onIceCandidate:"+jsonMessage.toString());
                String roomName = jsonMessage.get("roomName").getAsString();                
                Room room = getRoom(roomName);
                if (room==null) {
                    try {
                         JsonObject response = new JsonObject();
                         response.addProperty("id", "error");
                         response.addProperty("message","Room not found");
                         session.getBasicRemote().sendText(response.toString());
                    
                    } catch (IOException ex) {
                        Logger.getLogger(WebSocketServer.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    } 
                    break;
                 }
				
                //console.log('onIceCandidate called');
                Receiver receiver = room.receivers.get(session.getId());
                if (receiver == null) {
                        System.err.println("onIceCandidate no receivers");
                        break; 
                }
                System.out.println("onIceCandidate: room has current receiver:"+receiver.sessionId);
             
                JsonObject candidateJson = jsonMessage.get("candidate").getAsJsonObject();
                
                IceCandidate candidate = new IceCandidate(
                        candidateJson.get("candidate").getAsString(),
                        candidateJson.get("sdpMid").getAsString(), 
                        candidateJson.get("sdpMLineIndex").getAsInt()
                );
              
                if (receiver.endpoint!=null) {
                        receiver.endpoint.addIceCandidate(candidate);
                } else {
                        System.out.println("Queueing candidate");
                        receiver.candidateQueue.add(candidate);
                }
                
                break;
            }
            case "stop":

                break;
            case "send":
                System.out.println("stopped transmission");
                
                break; 
            default:
                throw new IllegalArgumentException("something else was called");
        }
    }
        /* 
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException ex) {
            ex.printStackTrace();
        }*/
   
 
    public static Room getRoom(String roomName) {
          // System.out.println("Looking for room:"+ roomName+" rooms size:"+rooms.size());
           for (int i = 0; i < rooms.size(); i++) {
                   if (rooms.get(i).getRoomName().equals(roomName)) {
                          // System.out.println("found room:"+rooms.get(i).getRoomName());
                           return rooms.get(i);
                   }
           }
           return null;
   }

    public static Room getRoomBySession(String sessionId) {
           System.out.println("Looking for room with session:");
           
           for (int i = 0; i < rooms.size(); i++) {
                   if (rooms.get(i).getSender()!=null && 
                           rooms.get(i).getSender().getSessionId().equals("sessionId")){
                           return rooms.get(i); //return callback(null, rooms[i]);
                   }
           }
           //return callback(null, null);
           return null;
    }

    private void startWebRtc(Room room, String sessionId, final Session session, String sdpOffer) {
        if(room == null || room.equals("")) throw new IllegalArgumentException("room is null");
        
        Sender sender = room.getSender();
        if(sender == null || sender.endpoint == null) throw new IllegalArgumentException("sender has no endpoint");
        
        final Receiver receiver = room.getOrCreateReceiver(sessionId, session, null);
        if(receiver == null) throw new IllegalArgumentException("receiver not created");
        
        MediaPipeline pipeline = room.pipeline;
        System.out.println("got pipeline");
        
        WebRtcEndpoint _webRtcEndpoint = new WebRtcEndpoint.Builder(pipeline).build();
        receiver.endpoint = _webRtcEndpoint;
        
        if (receiver.candidateQueue!=null) {
                while (receiver.candidateQueue.size()>0) {
                    System.out.println("adding ice candidates from candidateQueue:"+receiver.candidateQueue.size()+"' left");
                    IceCandidate candidate = receiver.candidateQueue.remove(receiver.candidateQueue.size()-1);
                    receiver.endpoint.addIceCandidate(candidate);
                }
        }
        
        String sdpAnswer = receiver.endpoint.processOffer(sdpOffer);
        sender.endpoint.connect(receiver.endpoint);
        System.out.println("connected sender endpoint with receiver endpoint");
         
       receiver.endpoint.addOnIceCandidateListener(new EventListener<OnIceCandidateEvent>() {
              @Override
              public void onEvent(OnIceCandidateEvent event) {
                System.out.println("receiver endpoint onIceCandidate:");
                JsonObject response = new JsonObject();
                response.addProperty("id", "iceCandidate");
                response.add("candidate", JsonUtils.toJsonObject(event.getCandidate()));
                  synchronized (session) {
                    try {
                        System.out.println("sending candidate to receiver.");
                        receiver.websocket.getBasicRemote().sendText(response.toString());
                    } catch (IOException ex) {
                        Logger.getLogger(WebSocketServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                 }
              }
        });
        
        System.out.println("sending sdpAnswer over websocket back to sender"+sdpAnswer);
        synchronized (receiver.websocket) {
            try {
                System.out.println("sdpAnswer sent to "+sender.websocket.getId());
                System.out.println(sdpAnswer);
                JsonObject sdpAnswerJson = new JsonObject();
                sdpAnswerJson.addProperty("id", "startResponse");
                sdpAnswerJson.addProperty("sdpAnswer", sdpAnswer);
					
                session.getBasicRemote().sendText(sdpAnswerJson.toString());
            
            } catch (IOException ex) {
                Logger.getLogger(WebSocketServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        receiver.endpoint.gatherCandidates();
        receiver.endpoint.connect(sender.endpoint);
        

        
        
    }
    
     
   
}