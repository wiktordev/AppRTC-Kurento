/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.lespace.webrtclibs.jwebrtc2;


/**
 * Message
 * @author Nico Krause (nico@le-space.de)
 */
class Message {
        
        private String clientId;
        private String roomId;
        private String roomName;
        private String cmd;
        private String type;
        private String sdpOffer;
        private Candidate candidate;
        
        public String toString(){
                return "cmd:"+getCmd()+" room: "+getRoomName();
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
         * @return the cmd
         */
        public String getCmd() {
            return cmd;
        }

        /**
         * @param cmd the cmd to set
         */
        public void setCmd(String cmd) {
            this.cmd = cmd;
        }

        /**
         * @return the sdpOffer
         */
        public String getSdpOffer() {
            return sdpOffer;
        }

        /**
         * @param sdpOffer the sdpOffer to set
         */
        public void setSdpOffer(String sdpOffer) {
            this.sdpOffer = sdpOffer;
        }

        /**
         * @return the candidate
         */
        public Candidate getCandidate() {
            return candidate;
        }

        /**
         * @param candidate the candidate to set
         */
        public void setCandidate(Candidate candidate) {
            this.candidate = candidate;
        }

        /**
         * @return the roomId
         */
        public String getRoomId() {
            return roomId;
        }

        /**
         * @param roomId the roomId to set
         */
        public void setRoomId(String roomId) {
            this.roomId = roomId;
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
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }
    }
