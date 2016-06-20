/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.lespace.webrtclibs.jwebrtc2;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author nico
 */
public class WebRTCServlet extends HttpServlet {
    
    String serverUrl = "192.168.43.251:8080";
    
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
        
            String ourPath = getOurPath(request);
            if(getOurPath(request).contains("join")) handleJoin(request, response);
           
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

}
