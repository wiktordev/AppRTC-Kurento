/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.lespace.webrtclibs.jwebrtc2;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.UUID;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.kurento.client.KurentoClient;

/**
 *
 * @author nico
 */
public class WebRTCServlet extends HttpServlet {
    
    String default_KMS_WS_URI = "";
    String serverUrl = "192.168.43.251:8080/jWebrtc";
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

    public KurentoClient kurentoClient() {
      return KurentoClient.create(System.getProperty("kms.url",default_KMS_WS_URI));
    }
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
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
        
        //String responseJSON = "{\"params\": {\"test\":\"test2\"}}";
        
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
    
        /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    private void handleMessage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        String ourPath = getOurPath(request);
        String[] segs = ourPath.split(Pattern.quote( "/" ));
        
        /*
        System.out.println("path parts:"+segs.length);
        
        for (int x = 0; x < segs.length;x++){
            System.out.println("path part:"+segs[x]);
        }
        */
        
        String roomName = (segs[segs.length-2]!=null) ?  segs[segs.length-2]  :  "empty";
	String clientId = (segs[segs.length-1]!=null)? segs[segs.length-1] : "emptyID";
        String body = getBody(request);
        
        System.out.println("roomName:"+roomName);
        System.out.println("clientId:"+clientId);
        //System.out.println("message"+body);
       
        String responseJSON = "{\"test\": \"test\"}";
        
        if(WebSocketServer.getRoom(roomName)==null){
           
            JsonObject jsonMessage = gson.fromJson(body, JsonObject.class);
    
            Room room =  WebSocketServer.getRoom(roomName);
            System.out.println("type:"+jsonMessage.get("type"));
            
            switch (jsonMessage.get("type").getAsString()) {
                case "candidate":
                    Sender sender = room.sender;
                    System.out.println("candidate:"+jsonMessage.get("candidate").getAsString());
                    

                    break;
                case "offer":
                    String sdpOffer = jsonMessage.get("sdpOffer").getAsString();
                    //String roomName = message.getRoomName();

                    System.out.println("sdpOffer:"+sdpOffer);
                    System.out.println("roomName:"+roomName);


                    break;            
                    
                default:
                    throw new IllegalArgumentException("something else was called");
            }
                     
        }
        
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
    
    public static String getBody(HttpServletRequest request) throws IOException {

    String body = null;
    StringBuilder stringBuilder = new StringBuilder();
    BufferedReader bufferedReader = null;

    try {
        
        InputStream inputStream = request.getInputStream();
        if (inputStream != null) {
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            char[] charBuffer = new char[128];
            int bytesRead = -1;
            while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                stringBuilder.append(charBuffer, 0, bytesRead);
            }
        } else {
            stringBuilder.append("");
        }
        
    } catch (IOException ex) {
        throw ex;
    } finally {
        if (bufferedReader != null) {
            try {
                bufferedReader.close();
            } catch (IOException ex) {
                throw ex;
            }
        }
    }

    body = stringBuilder.toString();
    return body;
}

}
