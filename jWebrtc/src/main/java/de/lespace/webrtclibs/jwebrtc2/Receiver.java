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

    private String sessionId;
    private Session websocket;
    private WebRtcEndpoint endpoint;
    private List<IceCandidate> candidateQueue = new ArrayList();

    public Receiver() {}

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Session getWebsocket() {
        return websocket;
    }

    public void setWebsocket(Session websocket) {
        this.websocket = websocket;
    }

    public WebRtcEndpoint getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(WebRtcEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    public List<IceCandidate> getCandidateQueue() {
        return candidateQueue;
    }

    public void setCandidateQueue(List<IceCandidate> candidateQueue) {
        this.candidateQueue = candidateQueue;
    }

}
