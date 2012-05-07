/*
 * Copyright 2010 Abdullah Shaikh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */


package com.openforevent.gwt.gwtrpc.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;

import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.AbstractRemoteServiceServlet;
import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.RPCServletUtils;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.SerializationPolicyLoader;
import com.google.gwt.user.server.rpc.SerializationPolicyProvider;
import com.openforevent.events.client.GwtRpcService;
import com.openforevent.gwt.gwtrpc.serializer.AbstractPayloadSerializer;
import com.openforevent.gwt.gwtrpc.serializer.PayloadSerializer;
import com.openforevent.gwt.gwtrpc.util.GwtRpcPayload;

/**
 * The server side implementation of the RPC service.
 */

public class GwtRpcServiceImpl extends AbstractRemoteServiceServlet implements
                GwtRpcService, SerializationPolicyProvider {
       
    public static final String module = GwtRpcServiceImpl.class.getName();
       
    private HttpServletRequest request = null;
    private HttpServletResponse response = null;
    private final Map<String, SerializationPolicy> serializationPolicyCache = new HashMap<String, SerializationPolicy>();

    public GwtRpcServiceImpl(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }
       
    static SerializationPolicy loadSerializationPolicy(HttpServlet servlet,
                      HttpServletRequest request, String moduleBaseURL, String strongName) {
                    // The request can tell you the path of the web app relative to the
            // container root.
            String contextPath = request.getContextPath();
       
            String modulePath = null;
            if (moduleBaseURL != null) {
              try {
                modulePath = new URL(moduleBaseURL).getPath();
              } catch (MalformedURLException ex) {
                // log the information, we will default
                servlet.log("Malformed moduleBaseURL: " + moduleBaseURL, ex);
              }
            }
       
            SerializationPolicy serializationPolicy = null;
       
            /*
             * Check that the module path must be in the same web app as the servlet
             * itself. If you need to implement a scheme different than this, override
             * this method.
             */
            if (modulePath == null || !modulePath.startsWith(contextPath)) {
              String message = "ERROR: The module path requested, "
                  + modulePath
                  + ", is not in the same web application as this servlet, "
                  + contextPath
                  + ".  Your module may not be properly configured or your client and server code maybe out of date.";
              servlet.log(message, null);
            } else {
              // Strip off the context path from the module base URL. It should be a
              // strict prefix.
              String contextRelativePath = modulePath.substring(contextPath.length());
       
              String serializationPolicyFilePath = SerializationPolicyLoader.getSerializationPolicyFileName(contextRelativePath
                  + strongName);
       
              // Open the RPC resource file and read its contents.
              InputStream is = servlet.getServletContext().getResourceAsStream(
                  serializationPolicyFilePath);
              try {
                if (is != null) {
                  try {
                    serializationPolicy = SerializationPolicyLoader.loadFromStream(is,
                        null);
                  } catch (ParseException e) {
                    servlet.log("ERROR: Failed to parse the policy file '"
                        + serializationPolicyFilePath + "'", e);
                  } catch (IOException e) {
                    servlet.log("ERROR: Could not read the policy file '"
                        + serializationPolicyFilePath + "'", e);
                  }
                } else {
                  String message = "ERROR: The serialization policy file '"
                      + serializationPolicyFilePath
                      + "' was not found; did you forget to include it in this deployment?";
                  servlet.log(message);
                }
              } finally {
                if (is != null) {
                  try {
                    is.close();
                  } catch (IOException e) {
                    // Ignore this error
                  }
                }
              }
            }
       
            return serializationPolicy;
        }              
       
    public final SerializationPolicy getSerializationPolicy(String moduleBaseURL,String strongName) {
       
            SerializationPolicy serializationPolicy = getCachedSerializationPolicy(
                moduleBaseURL, strongName);
            if (serializationPolicy != null) {
              return serializationPolicy;
            }

            serializationPolicy = doGetSerializationPolicy(getThreadLocalRequest(),
                moduleBaseURL, strongName);

            if (serializationPolicy == null) {
              // Failed to get the requested serialization policy; use the default
              log(
                  "WARNING: Failed to get the SerializationPolicy '"
                      + strongName
                      + "' for module '"
                      + moduleBaseURL
                      + "'; a legacy, 1.3.3 compatible, serialization policy will be used.  You may experience SerializationExceptions as a result.",
                  null);
              serializationPolicy = RPC.getDefaultSerializationPolicy();
            }
       
            // This could cache null or an actual instance. Either way we will not
            // attempt to lookup the policy again.
            putCachedSerializationPolicy(moduleBaseURL, strongName, serializationPolicy);
       
            return serializationPolicy;
        }

    public String processCall(String payload) throws SerializationException {
            try {
              RPCRequest rpcRequest = RPC.decodeRequest(payload, this.getClass(), this);
              onAfterRequestDeserialized(rpcRequest);
              return RPC.invokeAndEncodeResponse(this, rpcRequest.getMethod(),
                  rpcRequest.getParameters(), rpcRequest.getSerializationPolicy(),
                  rpcRequest.getFlags());
            } catch (IncompatibleRemoteServiceException ex) {
              log(
                  "An IncompatibleRemoteServiceException was thrown while processing this call.",
          ex);
              return RPC.encodeResponseForFailure(null, ex);
            }
        }

        protected SerializationPolicy doGetSerializationPolicy(
                      HttpServletRequest request, String moduleBaseURL, String strongName) {
                return loadSerializationPolicy(this, request, moduleBaseURL, strongName);
        }
       
        protected void onAfterResponseSerialized(String serializedResponse) {
        }
       
        protected void onBeforeRequestDeserialized(String serializedRequest) {
        }
       
        protected boolean shouldCompressResponse(HttpServletRequest request,
              HttpServletResponse response, String responsePayload) {
            return RPCServletUtils.exceedsUncompressedContentLengthLimit(responsePayload);
        }

        private SerializationPolicy getCachedSerializationPolicy(
      String moduleBaseURL, String strongName) {
                synchronized (serializationPolicyCache) {
                        return serializationPolicyCache.get(moduleBaseURL + strongName);
                }
        }

        private void putCachedSerializationPolicy(String moduleBaseURL,
                      String strongName, SerializationPolicy serializationPolicy) {
            synchronized (serializationPolicyCache) {
              serializationPolicyCache.put(moduleBaseURL + strongName,
                  serializationPolicy);
            }
        }

        private void writeResponse(HttpServletRequest request,
           HttpServletResponse response, String responsePayload) throws IOException {
           boolean gzipEncode = RPCServletUtils.acceptsGzipEncoding(request)
                        && shouldCompressResponse(request, response, responsePayload);

           RPCServletUtils.writeResponse(getServletContext(), response,
           responsePayload, gzipEncode);
        }      

        @Override
        public final void processPost(HttpServletRequest request,
              HttpServletResponse response) throws IOException, ServletException,
              SerializationException {
        	
        	if(Debug.infoOn()) {
        		Debug.logInfo("processPost", module);
        	}
        	
            // Read the request fully.
            //
               
            //get the requestPayload from the request attribute instead of reading it from request
            //String requestPayload = readContent(request);
                String requestPayload = (String)request.getAttribute(GwtRpcPayload.REQUEST_PAYLOAD);

                 // Let subclasses see the serialized request.
            //
            onBeforeRequestDeserialized(requestPayload);
       
            // Invoke the core dispatching logic, which returns the serialized
            // result.
            //
            String responsePayload = processCall(requestPayload);
       
            // Let subclasses see the serialized response.
            //
            onAfterResponseSerialized(responsePayload);
       
            // Write the response.
            //
            writeResponse(request, response, responsePayload);
        }
       
        //
        /*public String dummy1(HashMap<String, String> parameters) {
                return null;
        }
       
        public HashMap<String, String> dummy2(HashMap<String, String> parameters) {
                return null;
        }
       
        public ArrayList<HashMap<String, String>> dummy3(HashMap<String, String> parameters) {
                return null;
        }
       
        public ArrayList<HashMap<String, Object>> dummy4(HashMap<String, String> parameters) {
                return null;
        }*/
        //

    public HashMap<String, Object> processRequest(HashMap<String, String> parameters) {

        HashMap<String, Object> result = null;
               
        if(Debug.infoOn()) {
            Debug.logInfo("In processRequest : parameters " + parameters, module);
            Debug.logInfo("In processRequest : request "+request.getAttributeNames().hasMoreElements(), module);            
        }

        //HttpServletRequest request = getThreadLocalRequest();
        //HttpServletResponse response = getThreadLocalResponse();

        Object ofbizPayLoad = request.getAttribute(GwtRpcPayload.OFBIZ_PAYLOAD);
        if(Debug.infoOn()) {
            Debug.logInfo("ofbizPayLoad : " + ofbizPayLoad, module);
        }

        String ofbizPayloadClassName = ofbizPayLoad.getClass().getName();
        if(Debug.infoOn()) {
             Debug.logInfo("ofbizPayloadClassName : " + ofbizPayloadClassName, module);
         }

         if("javolution.util.FastMap".equals(ofbizPayloadClassName)) {

             FastMap<String, Object> tmpFastMap = (FastMap<String, Object>)ofbizPayLoad;
             HashMap<String, Object> tmpHashMap = new HashMap<String, Object>();
                       
             Iterator<String> keys = tmpFastMap.keySet().iterator();

             while(keys.hasNext()) {
                 String key = keys.next();
                 Object value = tmpFastMap.get(key);

                 tmpHashMap.put(key, value);
              }

              ofbizPayLoad = tmpHashMap;
          }

          result = (HashMap<String, Object>)ofbizPayLoad;
          if(Debug.infoOn()) {
              Debug.logInfo("result : " + result, module);
          }

          Object resultValue = result.get(GwtRpcPayload.PAYLOAD);
          if(Debug.infoOn()) {
              Debug.logInfo("payload : " + resultValue, module);
          }

          if(resultValue != null) {

                if(resultValue instanceof Map<?, ?>) {
                       
                        if(Debug.infoOn()) {
                                Debug.logInfo("resultValue is Map", module);
                        }

                        String className = resultValue.getClass().getName();
                        if(Debug.infoOn()) {
                                Debug.logInfo("resultValue class : " + className, module);
                        }

                        Object servResult = null;

                        PayloadSerializer serializer = AbstractPayloadSerializer.getCachedPayloadSerializer(className);

                        if(serializer != null) {
                                try {
                                        servResult = serializer.serialize(resultValue);
                                } catch(SerializationException se) {
                                        System.err.println("se -> " + se);
                                }
                        } else {
                                servResult = resultValue;
                        }

                        /*if("org.ofbiz.entity.GenericValue".equals(className)) {

                                String fields = parameters.get("fields");
                                if(Debug.infoOn()) {
                                        Debug.logInfo("fields : " + fields, module);
                                }
                               
                                servResult = GwtRpcUtil.toStringMap((GenericValue)resultValue, fields);
                        }*/

                        result.put(GwtRpcPayload.PAYLOAD, servResult);
                }
                else
                if(resultValue instanceof List<?>) {

                        if(Debug.infoOn()) {
                                Debug.logInfo("resultValue is List", module);
                        }
                       
                        String fields = parameters.get("fields");
                        if(Debug.infoOn()) {
                                Debug.logInfo("fields : " + fields, module);
                        }
                       
                        //
                        List<?> lgv = (List<?>)resultValue;

                        String className = lgv.getClass().getName();
                        if(Debug.infoOn()) {
                                Debug.logInfo("resultValue (objects) class : " + className, module);
                        }

                        Object servResult = null;
                       
                        PayloadSerializer serializer = AbstractPayloadSerializer.getCachedPayloadSerializer(className);

                        if(serializer != null) {
                                try {
                                        servResult = serializer.serialize(resultValue);
                                } catch(SerializationException se) {
                                        System.err.println("se -> " + se);
                                }
                        } else {
                                servResult = resultValue;
                        }

                        result.put(GwtRpcPayload.PAYLOAD, servResult);
                }
               
                if(Debug.infoOn()) {
                                Debug.logInfo("result : " + result, module);
                        }

        }

        return result;
        }

        @Override
        public String dummy1(HashMap<String, String> parameters) {
                // TODO Auto-generated method stub
                return null;
        }

        @Override
        public HashMap<String, Object> dummy2(HashMap<String, Object> parameters) {
        	HashMap<String, String> pars = new HashMap<String, String>();
        	
        	Iterator it = parameters.keySet().iterator();
        	while(it.hasNext() == true){
        		String key = (String)it.next();
        		String value = (String)parameters.get(key);
        		pars.put(key, value);
        	}
        	
        	Object ofbizPayLoad = request.getAttribute(GwtRpcPayload.OFBIZ_PAYLOAD);
            if(Debug.infoOn()) {
                Debug.logInfo("ofbizPayLoad : " + ofbizPayLoad, module);
            }

            String ofbizPayloadClassName = ofbizPayLoad.getClass().getName();
            if(Debug.infoOn()) {
                 Debug.logInfo("ofbizPayloadClassName : " + ofbizPayloadClassName, module);
             }
        	
            HashMap<String, Object> result = (HashMap<String, Object>)ofbizPayLoad;
            HashMap<String, Object> returnResult = new HashMap<String, Object>();
            it = result.keySet().iterator();
            while(it.hasNext() == true){
            	String key = (String)it.next();
            	returnResult.put(key, result.get(key));
            }
            return this.processRequest(pars);
        }

        @Override
        public ArrayList<HashMap<String, String>> dummy3(
                        HashMap<String, String> parameters) {
                // TODO Auto-generated method stub
                return null;
        }

        @Override
        public ArrayList<HashMap<String, Object>> dummy4(
                        HashMap<String, String> parameters) {
                // TODO Auto-generated method stub
                return null;
        }

}


