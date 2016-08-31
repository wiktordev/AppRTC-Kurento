/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.lespace.webrtclibs.jwebrtc2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.servlet.http.HttpServletRequest;
import org.kurento.client.KurentoClient;
import org.kurento.client.MediaPipeline;

/**
 *
 * @author nico
 */
public class Utils {
    
    public static KurentoClient kurentoClient() {
        
      String kmsURL =  System.getProperty("DEFAULT_KMS_WS_URI");
      
      if(kmsURL==null || kmsURL.equals("")){
           kmsURL = Config.DEFAULT_KMS_WS_URI;
      }
      System.out.println("using kms.url:"+kmsURL); 
      
      return KurentoClient.create(System.getProperty("kms.url", kmsURL));
    }
    
    public static MediaPipeline getPipeline(Room room){
        if(room == null || room.equals("")) throw new IllegalArgumentException("room is null");
        
        if(room.getPipeline() != null){
            System.out.println("returning saved pipeline");
            return room.getPipeline();
        }
        
        System.out.println("creating new pipeline to kurento server: ");
        room.pipeline = kurentoClient().createMediaPipeline();
        return room.pipeline; 
    }
    public static String getBody(HttpServletRequest request) throws IOException {

    String body = null;
    StringBuilder stringBuilder = new StringBuilder();
    BufferedReader bufferedReader = null;

    try {
        
        InputStream inputStream = request.getInputStream();
        if (inputStream != null) {
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            char[] charBuffer = new char[128];
            int bytesRead = -1;
            while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                stringBuilder.append(charBuffer, 0, bytesRead);
            }
        } else {
            stringBuilder.append("");
        }
        
    } catch (IOException ex) {
        throw ex;
    } finally {
        if (bufferedReader != null) {
            try {
                bufferedReader.close();
            } catch (IOException ex) {
                throw ex;
            }
        }
    }

    body = stringBuilder.toString();
    return body;
    }
    
}
