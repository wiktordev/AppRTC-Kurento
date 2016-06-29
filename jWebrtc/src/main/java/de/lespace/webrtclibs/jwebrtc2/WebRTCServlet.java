/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.lespace.webrtclibs.jwebrtc2;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.kurento.client.EventListener;
import org.kurento.client.IceCandidate;
import org.kurento.client.KurentoClient;
import org.kurento.client.MediaPipeline;
import org.kurento.client.OnIceCandidateEvent;
import org.kurento.client.WebRtcEndpoint;
import org.kurento.jsonrpc.JsonUtils;

/**
 * WebRTCServlet which manages all communication between  web, android and ios WebRTC candidates, 
 * turn servers and kurento media server.
 * 
 * @author Nico Krause
 * @version 0.1
 */
public class WebRTCServlet extends HttpServlet {
    
    String default_KMS_WS_URI = "ws://192.168.43.251:8888/kurento";
    //String default_KMS_WS_URI = "ws://www.le-space.de:8888/kurento";
    String serverUrl = "192.168.43.251:8080/jWebrtc";
    //String turn = "{}";
    String turn = "{\n" +
                    "	\"username\": \"akashionata\",\n" +
                    "	\"password\": \"silkroad2015\",\n" +
                    "	\"uris\": [\n" +
                    "		\"turn:5.9.154.226:3478\",\n" +
                    "		\"turn:5.9.154.226:3478?transport=udp\",\n" +
                    "		\"turn:5.9.154.226:3478?transport=tcp\"\n" +
                    "	]\n" +
                    "}";
    
    private static final Gson gson = new GsonBuilder().create();
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handle(request, response);
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handle(request, response);
    } 
    
    public void handle(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
            String ourPath = getOurPath(request);
            if(getOurPath(request).contains("join")) handleJoin(request, response);
            if(getOurPath(request).contains("message")) handleMessage(request, response);
            if(getOurPath(request).contains("turn")) handleTurn(request, response);     
    }
    
    
    public String getOurPath(HttpServletRequest request){
            String context = request.getContextPath();
            String requestUri = request.getRequestURI();
            String ourPath = requestUri.substring(context.length());
            return ourPath;
    }
   
    private void handleJoin(HttpServletRequest request, HttpServletResponse response) 
            throws IOException, ServletException {
      
        String ourPath = getOurPath(request);
        int lastSlashPos = ourPath.lastIndexOf("/");
        String roomName = ourPath.substring(lastSlashPos+1);
        
        if(WebSocketServer.getRoom(roomName)==null){
            WebSocketServer.createRoom(roomName);
        }
        
        String clientId =  UUID.randomUUID().toString();

        System.out.println("joining room \t\t:"+ roomName);
        System.out.println("generated client Id\t:"+clientId);
        System.out.println("serverUrl from config\t:"+serverUrl);
        
        String responseJSON = "{"+
	    "\"params\" : {"+
	    "\"is_initiator\": true,"+
	    "\"room_link\": \"http://" + serverUrl + "/r/" + roomName+"\","+
	    "\"version_info\": {\"gitHash\": \"029b6dc4742cae3bcb6c5ac6a26d65167c522b9f\", \"branch\": \"master\", \"time\": \"Wed Dec 9 16:08:29 2015 +0100\"},"+
	    "\"messages\": [],"+
	    "\"error_messages\": [],"+
	    "\"client_id\": \""+clientId+"\","+
	    "\"bypass_join_confirmation\": \"false\","+
	    "\"media_constraints\": {\"audio\": true, \"video\": true},"+
	    "\"include_loopback_js\": \"\","+
	    "\"turn_url\": \"http://" + serverUrl + "/turn\","+
	    "\"is_loopback\": \"false\","+
	    "\"wss_url\": \"ws://" + serverUrl + "/ws\","+
	    "\"pc_constraints\": {\"optional\": []},"+
	    "\"pc_config\": {\"rtcpMuxPolicy\": \"require\", \"bundlePolicy\": \"max-bundle\", \"iceServers\": []},"+
	    "\"wss_post_url\": \"http://" + serverUrl + "\","+
	    "\"offer_options\": {},"+
	    "\"warning_messages\": [],"+
	    "\"room_id\": \""+roomName+"\","+
	    "\"turn_transports\": \"\""+
	    "},"+
	    "\"result\": \"SUCCESS\""+
	 "}";
        response.setContentType("application/json");
        
        PrintWriter out = response.getWriter();
        out.print(responseJSON);
        out.flush();
    }
    
    private void handleTurn(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        PrintWriter out = response.getWriter();
        out.print(turn);
        out.flush();
    }

    private void handleMessage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("test1...");
       
        String ourPath = getOurPath(request);
        String[] segs = ourPath.split(Pattern.quote( "/" ));
       
        String roomName = (segs[segs.length-2]!=null) ?  segs[segs.length-2]  :  "empty";
	String clientid = (segs[segs.length-1]!=null)? segs[segs.length-1] : "emptyID"; //not used ... 
        String body = Utils.getBody(request);
        System.out.println("test2...");
        
        JsonObject jsonMessage = gson.fromJson(body, JsonObject.class);
        String type = jsonMessage.get("type").getAsString();
         System.out.println("tes3...");
        Room room = WebSocketServer.getRoom(roomName);
        System.out.println("test4..."+roomName);
        if(room!=null){
            switch (type) {
                case "candidate":
                {
                    //String messageCandidate = jsonMessage.get("candidate").getAsString();
                   // String messageLabel = jsonMessage.get("label").getAsString();

                    IceCandidate candidate = new IceCandidate(
                             jsonMessage.get("candidate").getAsString(),
                             "sdparta_0",
                             jsonMessage.get("label").getAsInt()); // jsonMessage.get("sdpMid").getAsString(),
                    
                    Sender sender = room.getSender();
                    
                    if (sender.endpoint!=null) {
                        sender.endpoint.addIceCandidate(candidate);
                        
                    } else {
                        sender.candidateQueue.add(candidate);
                    }

                    break;
                }
                case "offer":
                {
                     System.out.println("offer wurde von android geschickt.");
                     //only necessary when register happend over websocket 
                    if (room.getSender() !=null && room.getSender().websocket !=null) {
                       System.out.println("websocket present startSendWebRtc");                     
                       startSendWebRtc(room,jsonMessage.get("sdp").getAsString());
                    }
                    else{ //no websocket is present
                            room.setSenderSdpOffer(jsonMessage.get("sdp").getAsString());
                    }
                    break;            
                }
                default:
                    throw new IllegalArgumentException("something else was called");
            }
                     
        }else{
            
            System.out.println("no room!!!!!!!!!!!!!");
        }
        
        String responseJSON = "{\n" +
            "\"result\": \"SUCCESS\"\n" +
            "}";
        
        PrintWriter out = response.getWriter();
        out.print(responseJSON);
        out.flush();
    
    }
    
  
    public KurentoClient kurentoClient() {
      return KurentoClient.create(System.getProperty("kms.url",default_KMS_WS_URI));
    }
    
    private MediaPipeline getPipeline(Room room){
        if(room == null || room.equals("")) throw new IllegalArgumentException("room is null");
        
        if(room.getPipeline() != null){
            System.out.println("returning saved pipeline");
            return room.getPipeline();
        }
        System.out.println("creating new pipeline to kurento server: "+default_KMS_WS_URI);
        room.pipeline = kurentoClient().createMediaPipeline();
        return room.pipeline; 
    }
    
    private void startSendWebRtc(Room room, String sdpOffer) {
       
        if(room == null || room.equals("")) throw new IllegalArgumentException("room is null");
        final Sender sender = room.getSender();
        if(sender == null) throw new IllegalArgumentException("no sender in room");  
        MediaPipeline pipeline = getPipeline(room);
        WebRtcEndpoint _webRtcEndpoint = new WebRtcEndpoint.Builder(pipeline).build();
        sender.endpoint = _webRtcEndpoint;
        System.out.println("endpoint of sender created.");
        if(sender.candidateQueue!=null & sender.candidateQueue.size()>0){
            while (sender.candidateQueue.size()>0) {
                System.out.println("adding ice candidates from candidateQueue:"+sender.candidateQueue.size()+"' left");
                IceCandidate candidate = sender.candidateQueue.remove(sender.candidateQueue.size()-1);
                sender.endpoint.addIceCandidate(candidate);    
            }
        }
        
        String sdpAnswer = sender.endpoint.processOffer(sdpOffer);
        System.out.println("got sdpAnswer after offer from sender.endpoint ");
        sender.endpoint.addOnIceCandidateListener(new EventListener<OnIceCandidateEvent>() {
              @Override
              public void onEvent(OnIceCandidateEvent event) {
                
                System.out.println("getting type:"+event.getType()+" sender.websocket.id:"+(sender.websocket.getId()));
               
                JsonObject response = new JsonObject();
                
                    JsonObject msgJson = new JsonObject();
                    msgJson.addProperty("type", "candidate");
                    msgJson.addProperty("label", event.getCandidate().getSdpMLineIndex());
                    msgJson.addProperty("id", event.getCandidate().getSdpMid());
                    msgJson.add("candidate", JsonUtils.toJsonObject(event.getCandidate()));
             
                response.add("msg", msgJson);
                response.addProperty("error","");

                synchronized (sender.websocket) {
                    try {
                        System.out.println("response to websocket: "+sender.websocket.getId());
                        sender.websocket.getBasicRemote().sendText(response.getAsString());
                    } catch (IOException ex) {
                        Logger.getLogger(WebSocketServer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                 }
              }
        });
       
        JsonObject sendSdpAnswer =  new JsonObject(); 
        
        JsonObject msgJson = new JsonObject();
        msgJson.addProperty("type", "answer");
        msgJson.addProperty("sdp", sdpAnswer);
        sendSdpAnswer.add("msg", msgJson);
        sendSdpAnswer.addProperty("error","");
        
        System.out.println("sending sdpAnswer over websocket back to sender"+msgJson.toString());
        synchronized (sender.websocket) {
            try {
                System.out.println("sdpAnswer sent to "+sender.websocket.getId());
                sender.websocket.getBasicRemote().sendText(sendSdpAnswer.toString());
            
            } catch (IOException ex) {
                Logger.getLogger(WebRTCServlet.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        sender.endpoint.gatherCandidates();
        
    }
    
    public String getServletInfo() {
        return "The WebRTCSerlvet for MSC handles communication between WebRTC candidates";
    }// </editor-fold>

}
