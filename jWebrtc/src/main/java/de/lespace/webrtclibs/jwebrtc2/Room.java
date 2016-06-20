/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.lespace.webrtclibs.jwebrtc2;

/**
 * Room!
 * @author Nico Krause (nico@le-space.de)
 */
    class Room {

        public String roomName;
        public Sender sender;
        
        public Room(String roomName) {
            this.roomName = roomName;
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
    }


    class Sender {
        
        public String sessionId;

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

    }
