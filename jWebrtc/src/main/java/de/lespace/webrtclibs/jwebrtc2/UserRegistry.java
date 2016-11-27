/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.lespace.webrtclibs.jwebrtc2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import javax.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Map of users registered in the system. This class has a concurrent hash map
 * to store users, using its name as key in the map.
 * 
 * @author Boni Garcia (bgarcia@gsyc.es)
 * @author Micael Gallego (micael.gallego@gmail.com)
 * @since 4.3.1
 */
public class UserRegistry {

	private ConcurrentHashMap<String, UserSession> usersByName = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, UserSession> usersBySessionId = new ConcurrentHashMap<>();
        
        private static final Logger log = LoggerFactory.getLogger(UserRegistry.class);
	
        public void register(UserSession user) {
                log.debug("registering user:",user.getName());
                UserSession unRegisteredUserSession = usersBySessionId.get(user.getSession().getId());
            
                //delete old sessions without name and replace with the same session but with name
                //it is necessary for the status which can be queried also in unregistered state
                if(unRegisteredUserSession!=null){
                    log.debug("removing unregisteredUserSession with name:"+unRegisteredUserSession.getName());
                    usersByName.remove(unRegisteredUserSession.getName());
                    usersBySessionId.remove(unRegisteredUserSession.getSession().getId());
                }
                
		usersByName.put(user.getName(), user);
		usersBySessionId.put(user.getSession().getId(), user);
            
	}

	public UserSession getByName(String name) {
		return usersByName.get(name);
	}

	/**
	 * Returns the user to whom the session belongs. If no user is found, null
	 * is returned.
	 * 
	 * @param session
	 * @return The corresponding user or null, if no such user is registered
	 */
	public UserSession getBySession(Session session) {
		return usersBySessionId.get(session.getId());
	}

	public boolean exists(String name) {
		return usersByName.keySet().contains(name);
	}

	public UserSession removeBySession(Session session) {
		final UserSession user = getBySession(session);
		if (user != null) {
			usersByName.remove(user.getName());
			usersBySessionId.remove(session.getId());
		}
		return user;
	}

	public List<String> getRegisteredUsers() {
		return Collections.list(usersByName.keys());
	}

	public List<UserSession> getUserSessions() {
		return new ArrayList<UserSession>(usersByName.values());
	}
        


}
