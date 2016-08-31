package de.lespace.webrtclibs.jwebrtc2;

import java.text.SimpleDateFormat;
import java.util.Date;

/*
 * (C) Copyright 2014 Kurento (http://kurento.org/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import org.kurento.client.KurentoClient;
import org.kurento.client.MediaPipeline;
import org.kurento.client.RecorderEndpoint;
import org.kurento.client.WebRtcEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Media Pipeline (WebRTC endpoints, i.e. Kurento Media Elements) and
 * connections for the 1 to 1 video communication.
 * 
 * @author Boni Garcia (bgarcia@gsyc.es)
 * @author Micael Gallego (micael.gallego@gmail.com)
 * @since 4.3.1
 */
public class CallMediaPipeline {
	private static final Logger log = LoggerFactory.getLogger(CallMediaPipeline.class);
	
	private static final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-S");

	// TODO define as environment variables
	public static final String RECORDING_DIR = "file:///var/kurento/";
	
//	public static final String RECORDING_PATH = RECORDING_DIR + df.format(new Date()) + "-";
	public static final String RECORDING_EXT = ".webm";

	private MediaPipeline pipeline;
	private WebRtcEndpoint callerWebRtcEp;
	private WebRtcEndpoint calleeWebRtcEp;
	private RecorderEndpoint calleeRecorder;
	private RecorderEndpoint callerRecorder;

	public CallMediaPipeline(KurentoClient kurento, String from, String to) {
		String date = df.format(new Date());
		
		try {
			this.pipeline = kurento.createMediaPipeline();
			this.callerWebRtcEp = new WebRtcEndpoint.Builder(pipeline).build();
			this.calleeWebRtcEp = new WebRtcEndpoint.Builder(pipeline).build();
			
			this.callerRecorder = new RecorderEndpoint.Builder(pipeline, RECORDING_DIR + date + "-" + from + RECORDING_EXT).build();
			this.calleeRecorder = new RecorderEndpoint.Builder(pipeline, RECORDING_DIR + date + "-" + to + RECORDING_EXT).build();

			this.callerWebRtcEp.connect(this.calleeWebRtcEp);
			this.callerWebRtcEp.connect(this.callerRecorder);
			
			this.calleeWebRtcEp.connect(this.callerWebRtcEp);
			this.calleeWebRtcEp.connect(this.calleeRecorder);
		} catch (Throwable t) {
			if (this.pipeline != null) {
				pipeline.release();
			}
			log.error("Unable to create instance of CallMediaPipeline!", t.getMessage());
		}
	}

	public String generateSdpAnswerForCaller(String sdpOffer) {
		return callerWebRtcEp.processOffer(sdpOffer);
	}

	public String generateSdpAnswerForCallee(String sdpOffer) {
		return calleeWebRtcEp.processOffer(sdpOffer);
	}

	public void release() {
		if (pipeline != null) {
			pipeline.release();
		}
	}

	public WebRtcEndpoint getCallerWebRtcEp() {
		return callerWebRtcEp;
	}

	public WebRtcEndpoint getCalleeWebRtcEp() {
		return calleeWebRtcEp;
	}

	public void record() {
		log.debug("Start recording...");
		calleeRecorder.record();
		callerRecorder.record();
	}

	public MediaPipeline getPipeline() {
		return pipeline;
	}

}
