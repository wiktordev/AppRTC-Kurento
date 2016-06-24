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
 * @author Nico Krause
 */
class Sender {
        
        public String sessionId;
        public String clientId;
        public Session websocket; 
        public WebRtcEndpoint endpoint; 
        public List<IceCandidate> candidateQueue = new ArrayList();
        
        public Sender(){
            
        }
        /**
         * @return the sessionId
         */
        public String getSessionId() {
            return sessionId;
        }

        /**
         * @param sessionId the sessionId to set
         */
        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }

        /**
         * @return the clientId
         */
        public String getClientId() {
            return clientId;
        }

        /**
         * @param clientId the clientId to set
         */
        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        /**
         * @return the websocket
         */
        public Session getWebsocket() {
            return websocket;
        }

        /**
         * @param websocket the websocket to set
         */
        public void setWebsocket(Session websocket) {
            this.websocket = websocket;
        }

        /**
         * @return the endpoint
         */
        public WebRtcEndpoint getEndpoint() {
            return endpoint;
        }

        /**
         * @param endpoint the endpoint to set
         */
        public void setEndpoint(WebRtcEndpoint endpoint) {
            this.endpoint = endpoint;
        }

        /**
         * @return the candidateQueue
         */
        public List getCandidateQueue() {
            return candidateQueue;
        }

        /**
         * @param candidateQueue the candidateQueue to set
         */
        public void setCandidateQueue(List candidateQueue) {
            this.candidateQueue = candidateQueue;
        }

    }
