package com.openforevent.main;
/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.StringUtil;
import org.ofbiz.base.util.UtilGenerics;
import org.ofbiz.base.util.UtilHttp;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.base.util.UtilXml;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.transaction.GenericTransactionException;
import org.ofbiz.entity.transaction.TransactionUtil;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ModelService;
import org.ofbiz.service.ServiceUtil;
import org.ofbiz.webapp.stats.VisitHandler;
import org.w3c.dom.Document;

import org.apache.commons.io.IOUtils;

public class UserPosition {
	
	public static final String module = UserPosition.class.getName();
    public static final String resource = "EventsUiLabels";
    public static final int DEFAULT_TX_TIMEOUT = 600;
    public static final String default_ip = "78.60.178.80";
    public static final String ipinfodb_key = "6043b416d7e84cc4317df12d3a0011739c17bc8a5a77fbb4d8d7e9a3f24e5e32";
    
    public static String sendVisitID(HttpServletRequest request, HttpServletResponse response) {
    	      HttpSession session = request.getSession(true);
    	      GenericValue visit = VisitHandler.getVisit(request.getSession());
    	      String visitId = visit.getString("visitId");
    	      session.setAttribute("visitId", visitId);     
        return visitId;
    }
    
    public static Map<String, Object> userPositionByIP(DispatchContext ctx, Map<String, ? extends Object> context) throws IOException{
    	    URL url = new URL("http://api.ipinfodb.com/v3/ip-city/?" +
	    		"key="+ipinfodb_key+"&"+
	    		"&ip="+default_ip+
	    		"&format=xml");
	   //URL url = new URL("http://ipinfodb.com/ip_query.php");
          HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
    
       // Set properties of the connection
          urlConnection.setRequestMethod("GET");
          urlConnection.setDoInput(true);
          urlConnection.setDoOutput(true);
          urlConnection.setUseCaches(false);
          urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

       // Retrieve the output
       int responseCode = urlConnection.getResponseCode();
           InputStream inputStream;
       if (responseCode == HttpURLConnection.HTTP_OK) {
           inputStream = urlConnection.getInputStream();
       } else {
           inputStream = urlConnection.getErrorStream();
       }
    
          StringWriter writer = new StringWriter();
          IOUtils.copy(inputStream, writer, "UTF-8");
          String theString = writer.toString();
          
          Debug.logInfo("Get user position by IP stream is = ", theString);
          
          Map<String, Object> paramOut = FastMap.newInstance();   
          paramOut.put("stream", theString);
       return paramOut;
    }
    
    public static Map<String, Object> coordsOfUserPosition(DispatchContext ctx, Map<String, ? extends Object> context) throws IOException{  	    
    	return null;
    }
    
    public static Map<String, Object> locationPropsFromCoords(DispatchContext ctx, Map<String, ? extends Object> context) throws IOException{
    	return null;
    }
    
    public static Map<String, Object> userLocationProperties(DispatchContext ctx, Map<String, ? extends Object> context) throws IOException{
    	    Delegator delegator = ctx.getDelegator();
          LocalDispatcher dispatcher = ctx.getDispatcher();
          String visitId = (String)context.get("visitId"); 

       // get user coords
       coordsOfUserPosition(ctx, context);
       
          Map<String, Object> paramOut = FastMap.newInstance();   
          paramOut.put("geoName", "");
          paramOut.put("abbreviation", "");
       return paramOut;
       
    }

}





