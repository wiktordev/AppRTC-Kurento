/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.lespace.webrtclibs.jwebrtc2;

import org.kurento.client.MediaPipeline;

/**
 * Room!
 * @author Nico Krause (nico@le-space.de)
 */
    class Room {

        public String roomName;
        public Sender sender;
        public String senderSdpOffer;
        public MediaPipeline pipeline;
        
        public Room(String roomName) {
            this.roomName = roomName;
            this.sender = new Sender();
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
    }


    
