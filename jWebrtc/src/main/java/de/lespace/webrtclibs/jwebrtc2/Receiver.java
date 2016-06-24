/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.lespace.webrtclibs.jwebrtc2;

import java.util.ArrayList;
import java.util.List;
import javax.websocket.Session;
import org.kurento.client.IceCandidate;
import org.kurento.client.WebRtcEndpoint;

/**
 *
 * @author nico
 */
public class Receiver {
    
    public Receiver(){}
    
	public String sessionId;
	public Session websocket;
        public WebRtcEndpoint endpoint;
	public List<IceCandidate> candidateQueue = new ArrayList();
        
}
