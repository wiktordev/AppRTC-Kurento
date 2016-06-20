package de.lespace.webrtclibs.jwebrtc2;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;

/** 
 * @ServerEndpoint gives the relative name for the end point
 * This will be accessed via ws://localhost:8080/EchoChamber/echo
 * Where "localhost" is the address of the host,
 * "EchoChamber" is the name of the package
 * and "echo" is the address to access this class from the server
 */
@ServerEndpoint("/ws") 
public class WebSocketServer {

    static void createRoom(String roomName) {
        Room newRoom = new Room(roomName);
        
    }
    
    private String sessionId;
    private String serverUrl; //config.appRTCUrl;
    private String ws_uri; //config.ws_uri;
    private String port; //config.port;

    private final String kurentoClient = null;
    public static List<Room> rooms = new ArrayList();
    
    
    /**
     * @OnOpen allows us to intercept the creation of a new session.
     * The session class allows us to send data to the user.
     * In the method onOpen, we'll let the user know that the handshake was 
     * successful.
     */
    @OnOpen
    public void onOpen(Session session){
        this.sessionId = session.getId();
        System.out.println("apprtcWs opened with sessionId " + sessionId); 
        /*try {
           // session.getBasicRemote().sendText("Connection from J2EE Established");
        } catch (IOException ex) {
            ex.printStackTrace();
        }*/
    }
    
    @OnError
    public void onError(Session session, Throwable error){
        System.out.println("apprtcWs Connection " + sessionId + " error:"+ error.toString()); 
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
        System.out.println("apprtcWs Connection " + session.getId() + " closed");
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
        
        //parse json message
        //System.out.println("messsage:"+_message);
        Message message = new Gson().fromJson(_message, Message.class);
        
        String clientId = (message.getClientId()!=null) ? message.getClientId() : "empty";
	String roomId   = (message.getRoomId()!=null) ? message.getRoomId() : "emptyID";
        String roomName = (message.getRoomName()!=null) ? message.getRoomName() : "emptyID";
       
        System.out.println("apprtcWs " + session.getId() + " received message "+ message);
        System.out.println("apprtcWs  clientId:" + clientId+ " roomId:"+roomId);
        switch (message.getCmd()) {
        case "register":
            
            break;
        case "startWebRtc":
            String sdpOffer = message.getSdpOffer();
            //String roomName = message.getRoomName();
            
            System.out.println("sdpOffer:"+sdpOffer);
            System.out.println("roomName:"+roomName);
            
            
            break;            
        case "onIceCandidate":
            
            break;
        case "stop":
            
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
           System.out.println("Looking for room:"+ roomName);
           for (int i = 0; i < rooms.size(); i++) {
                   if (rooms.get(i).getRoomName().equals(roomName)) {
                           return rooms.get(i);
                   }
           }
           return null;
   }

    public static Room getRoomBySession(String sessionId) {
           System.out.println("Looking for room with session:");
           
           for (int i = 0; i < rooms.size(); i++) {
                   if (rooms.get(i).getSender()!=null && 
                           rooms.get(i).getSender().sessionId.equals("sessionId")){
                           return rooms.get(i); //return callback(null, rooms[i]);
                   }
           }
           //return callback(null, null);
           return null;
    }
    
     
   
}