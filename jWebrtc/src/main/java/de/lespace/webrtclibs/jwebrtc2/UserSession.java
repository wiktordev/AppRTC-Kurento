/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.lespace.webrtclibs.jwebrtc2;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.kurento.client.IceCandidate;
import org.kurento.client.WebRtcEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.google.gson.JsonObject;
import javax.websocket.Session;

/**
 * User session.
 *
 * @author Boni Garcia (bgarcia@gsyc.es)
 * @author Micael Gallego (micael.gallego@gmail.com)
 * @since 4.3.1
 */
public class UserSession {

  private static final Logger log = LoggerFactory.getLogger(UserSession.class);

  private final String name;
  private final Session session;

  private String sdpOffer;
  private String sdpOfferScreen;
  private String callingTo;
  private String callingFrom;
  private WebRtcEndpoint webRtcEndpoint;
  private WebRtcEndpoint webRtcScreenEndpoint;
  private final List<IceCandidate> candidateList = new ArrayList<IceCandidate>();
  
  private final List<IceCandidate> candidateListScreen = new ArrayList<IceCandidate>();

  public UserSession(Session session, String name) {
    this.session = session;
    this.name = name;
  }
  
  public Session getSession() {
    return session;
  }
  
  public String getName() {
    return name;
  }

  public String getSdpOffer() {
    return sdpOffer;
  }

  public void setSdpOffer(String sdpOffer) {
    this.sdpOffer = sdpOffer;
  }
  public String getSdpOfferScreen() {
    return sdpOfferScreen;
  }

  public void setSdpOfferScreen(String sdpOffer) {
    this.sdpOfferScreen = sdpOffer;
  }
  public String getCallingTo() {
    return callingTo;
  }

  public void setCallingTo(String callingTo) {
    this.callingTo = callingTo;
  }

  public String getCallingFrom() {
    return callingFrom;
  }

  public void setCallingFrom(String callingFrom) {
    this.callingFrom = callingFrom;
  }

  public void sendMessage(JsonObject message) throws IOException {
    
    if(session.isOpen()){
        log.debug("Sending message from user '{}': {}", name, message);
        session.getBasicRemote().sendText(message.toString());
    }
    else 
        log.debug("session of user '{}' is closed.", name);
  }

  public String getSessionId() {
    return session.getId();
  }

  public void setWebRtcEndpoint(WebRtcEndpoint webRtcEndpoint) {
    this.webRtcEndpoint = webRtcEndpoint;

    for (IceCandidate e : candidateList) {
      this.webRtcEndpoint.addIceCandidate(e);
    }
    this.candidateList.clear();
  }
  
 public void setWebRtcScreenEndpoint(WebRtcEndpoint webRtcEndpoint) {
    this.webRtcScreenEndpoint = webRtcEndpoint;

    for (IceCandidate e : candidateListScreen) {
      this.webRtcScreenEndpoint.addIceCandidate(e);
    }
    this.candidateListScreen.clear();
  }

  public void addCandidate(IceCandidate candidate) {
    if (this.webRtcEndpoint != null) {
      this.webRtcEndpoint.addIceCandidate(candidate);
    } else {
      candidateList.add(candidate);
    }
  }
  
 public void addCandidateScreen(IceCandidate candidate) {
    if (this.webRtcScreenEndpoint != null) {
      this.webRtcScreenEndpoint.addIceCandidate(candidate);
    } else {
      candidateListScreen.add(candidate);
    }
  }

  public void clear() {
    this.webRtcEndpoint = null;
    this.webRtcScreenEndpoint = null;
    this.candidateList.clear();
    this.candidateListScreen.clear();
  }
  
    public boolean isBusy() {
            return this.webRtcEndpoint != null;
    }

  
}

