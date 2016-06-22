/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.lespace.webrtclibs.jwebrtc2;

/**
 * Candidate
 * @author Nico Krause (nico@le-space.de)
 */
class Candidate {
        
        public String candidate;
        public String sdpMid; 
        public String sdpMLineIndex;

        /**
         * @return the candidate
         */
        public String getCandidate() {
            return candidate;
        }

        /**
         * @param candidate the candidate to set
         */
        public void setCandidate(String candidate) {
            this.candidate = candidate;
        }

        /**
         * @return the sdpMid
         */
        public String getSdpMid() {
            return sdpMid;
        }

        /**
         * @param sdpMid the sdpMid to set
         */
        public void setSdpMid(String sdpMid) {
            this.sdpMid = sdpMid;
        }

        /**
         * @return the sdpMLineIndex
         */
        public String getSdpMLineIndex() {
            return sdpMLineIndex;
        }

        /**
         * @param sdpMLineIndex the sdpMLineIndex to set
         */
        public void setSdpMLineIndex(String sdpMLineIndex) {
            this.sdpMLineIndex = sdpMLineIndex;
        }
        
        public String toString(){
            return  "candidate:"+candidate+" sdpMid:"+sdpMid+" sdpMLineIndex:"+sdpMLineIndex;
        }
    }
