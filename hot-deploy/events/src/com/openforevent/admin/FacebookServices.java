package com.openforevent.admin;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.HashMap;

import org.apache.commons.io.IOUtils;
import org.ofbiz.base.util.Debug;
import org.ofbiz.entity.Delegator;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.LocalDispatcher;



public class FacebookServices {
	
	public static final String module = FacebookServices.class.getName();
	
	public static Map<String, Object> storeFacebookToken(DispatchContext ctx, Map<String, ? extends Object> context) throws IOException{
		 Delegator delegator = ctx.getDelegator();
         LocalDispatcher dispatcher = ctx.getDispatcher();
         String state = (String)context.get("state"); 
         String code = (String)context.get("code"); 
         String error = (String)context.get("error"); 
         String error_reason = (String)context.get("error_reason"); 
         String error_description = (String)context.get("error_description"); 
         
         Debug.logInfo("Facebook login : state="+state+", code="+code, module);
         
         if(code != null){
        	 
        	 //get short time access_token
        	 URL url = new URL("https://graph.facebook.com/oauth/access_token?" +
        	 		"client_id=283576871735609" +
        	 		"&redirect_uri=https://localhost:8443/events/control/storeFacebookToken" +
        	 		"&client_secret=5cf1fe4e531dff8de228bfac61b8fdfa" +
        	 		"&code="+code);
        	 httpConnection(url);
             StringWriter writer = new StringWriter();
             IOUtils.copy(inputStream, writer, "UTF-8");
             String theString = writer.toString();
             
             Debug.logInfo("Facebook access_token string = "+theString, module);
         }
         
         return new HashMap<String, Object>();
	}
	
	private InputStream httpConnection(Url url){
		HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
   	    urlConnection.setRequestMethod("GET");
        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);
        urlConnection.setUseCaches(false);
        urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        int responseCode = urlConnection.getResponseCode();
        InputStream inputStream;
        if (responseCode == HttpURLConnection.HTTP_OK) {
            inputStream = urlConnection.getInputStream();
        } else {
            inputStream = urlConnection.getErrorStream();
        }
        return inputStream;
	}

}
