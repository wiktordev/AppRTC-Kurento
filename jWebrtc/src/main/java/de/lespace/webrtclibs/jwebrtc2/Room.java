/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.lespace.webrtclibs.jwebrtc2;

import java.util.HashMap;
import java.util.Map;
import javax.websocket.Session;
import org.kurento.client.MediaPipeline;
import org.kurento.client.WebRtcEndpoint;

/**
 * Room!
 * @author Nico Krause (nico@le-space.de)
 */
    class Room {

        public String roomName;
        public Sender sender;
        public String senderSdpOffer;
        public MediaPipeline pipeline;
        public Map<String, Receiver> receivers;  
                
        public Room(String roomName) {
            this.roomName = roomName;
            this.sender = new Sender();
            this.receivers = new HashMap();
        }
                    
        /**
         * @return the roomName
         */
        public String getRoomName() {
            return roomName;
        }

        /**
         * @param roomName the roomName to set
         */
        public void setRoomName(String roomName) {
            this.roomName = roomName;
        }

        /**
         * @return the sender
         */
        public Sender getSender() {
            return sender;
        }

        /**
         * @param sender the sender to set
         */
        public void setSender(Sender sender) {
            this.sender = sender;
        }

    /**
     * @return the senderSdpOffer
     */
    public String getSenderSdpOffer() {
        return senderSdpOffer;
    }

    /**
     * @param senderSdpOffer the senderSdpOffer to set
     */
    public void setSenderSdpOffer(String senderSdpOffer) {
        this.senderSdpOffer = senderSdpOffer;
    }

    /**
     * @return the pipeline
     */
    public MediaPipeline getPipeline() {
        return pipeline;
    }

    /**
     * @param pipeline the pipeline to set
     */
    public void setPipeline(MediaPipeline pipeline) {
        this.pipeline = pipeline;
    }
    
    public Receiver getOrCreateReceiver(String sessionId, Session websocket, WebRtcEndpoint endpoint){
        if(receivers.containsKey(sessionId)){
            return receivers.get(sessionId);
        }
        else{
                Receiver receiver = new Receiver();
                receiver.websocket = websocket;
                receiver.sessionId = sessionId;
                receiver.endpoint = endpoint;
                this.receivers.put(sessionId,receiver);
                return receiver;
        }
        
        
    }
    
    }


    
