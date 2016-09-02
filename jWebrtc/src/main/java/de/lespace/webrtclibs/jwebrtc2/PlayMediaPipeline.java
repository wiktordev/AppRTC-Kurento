package de.lespace.webrtclibs.jwebrtc2;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.websocket.Session;

import org.kurento.client.ErrorEvent;
import org.kurento.client.EventListener;
import org.kurento.client.KurentoClient;
import org.kurento.client.MediaPipeline;
import org.kurento.client.PlayerEndpoint;
import org.kurento.client.WebRtcEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

// TODO Maybe modify this class to replay any recorded talk between two (or more?) peers?

public class PlayMediaPipeline {

	private static final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-S");
	
	// TODO define as environment variables
	public static final String RECORDING_DIR = "file:///kurento/record/";
	public static final String RECORDING_PATH = RECORDING_DIR + df.format(new Date()) + "-";
	public static final String RECORDING_EXT = ".webm";

	private static final Logger log = LoggerFactory.getLogger(PlayMediaPipeline.class);

	private MediaPipeline pipeline;
	private WebRtcEndpoint webRtc;
	private PlayerEndpoint player;

	public PlayMediaPipeline(KurentoClient kurento, String user, final Session session) {
		// Media pipeline
		pipeline = kurento.createMediaPipeline();

		// Media Elements (WebRtcEndpoint, PlayerEndpoint)
		webRtc = new WebRtcEndpoint.Builder(pipeline).build();
		player = new PlayerEndpoint.Builder(pipeline, RECORDING_PATH + user + RECORDING_EXT).build();

		// Connection
		player.connect(webRtc);

		// Player listeners
		player.addErrorListener(new EventListener<ErrorEvent>() {
			@Override
			public void onEvent(ErrorEvent event) {
				log.info("ErrorEvent: {}", event.getDescription());
				sendPlayEnd(session);
			}
		});
	}

	public void sendPlayEnd(Session session) {
		try {
			JsonObject response = new JsonObject();
			response.addProperty("id", "playEnd");
			session.getBasicRemote().sendText(response.toString());
		} catch (IOException e) {
			log.error("Error sending playEndOfStream message", e);
		}
	}

	public void play() {
		player.play();
	}

	public String generateSdpAnswer(String sdpOffer) {
		return webRtc.processOffer(sdpOffer);
	}

	public MediaPipeline getPipeline() {
		return pipeline;
	}

	public WebRtcEndpoint getWebRtc() {
		return webRtc;
	}

	public PlayerEndpoint getPlayer() {
		return player;
	}

}
