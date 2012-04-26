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


package com.openforevent.gwt.gwtrpc.event;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ofbiz.base.util.Debug;
import org.ofbiz.webapp.control.ConfigXMLReader.Event;
import org.ofbiz.webapp.control.ConfigXMLReader.RequestMap;
import org.ofbiz.webapp.control.RequestHandler;
import org.ofbiz.webapp.event.EventHandler;
import org.ofbiz.webapp.event.EventHandlerException;

import com.openforevent.gwt.gwtrpc.util.GwtRpcPayload;
import com.openforevent.gwt.gwtrpc.util.GwtRpcPayloadUtil;
import com.openforevent.gwt.gwtrpc.util.GwtRpcServletUtil;

public class GwtRpcJavaEventHandler implements EventHandler {

    public static final String module = GwtRpcJavaEventHandler.class.getName();

    private ServletContext servletContext = null;
   
    public void init(ServletContext servletContext) throws EventHandlerException {
        this.servletContext = servletContext;
    }

    public String invoke(Event event, RequestMap requestMap, HttpServletRequest request, HttpServletResponse response) throws EventHandlerException {
       
        if(Debug.infoOn()) {
                Debug.logInfo("In GwtRpcJavaEventHandler", module);
        }

        String requestPayload = null;

        try {
               
                requestPayload = GwtRpcPayloadUtil.getRequestPayload(request);
               
        } catch(IOException ioe) {
                throw new EventHandlerException("Exception while getting requestPayload",ioe);
        } catch(ServletException se) {
                throw new EventHandlerException("Exception while getting requestPayload",se);
        }

        HashMap<String, String> gwtParameters = GwtRpcPayloadUtil.getParameters(requestPayload);
        if(Debug.infoOn()) {
                Debug.logInfo("gwtParameters : " + gwtParameters, module);
        }

        if(null != gwtParameters) {

                String javaEventName = event.invoke;
                if(Debug.infoOn()) {
                        Debug.logInfo("javaEventName : " + javaEventName, module);
                }

                Set<String> keys = gwtParameters.keySet();
                        Iterator<String> iter = keys.iterator();
                        while(iter.hasNext()) {
                                String key = iter.next();
                                request.setAttribute(key, gwtParameters.get(key));
                        }

                        RequestHandler requestHandler = (RequestHandler) request.getAttribute("_REQUEST_HANDLER_");
                        EventHandler javaEventHandler = requestHandler.getEventFactory().getEventHandler("java");
                       
                        String eventResult = null;
                        try {
                               
                                eventResult = javaEventHandler.invoke(new Event("java", event.path, event.invoke, true), null, request, response);
                                if(Debug.infoOn()) {
                                Debug.logInfo("eventResult : " + eventResult, module);
                        }
                               
                        } catch(EventHandlerException ehe) {
                                throw new EventHandlerException("Exception while executing java event : ",ehe);
                        }

                        Map<String, Object> resultMap = (Map<String, Object>) request.getAttribute("result");
               
                //ServletContext sc = (ServletContext)request.getAttribute("servletContext");
                //RequestDispatcher rd = sc.getRequestDispatcher("/gwtrpc");

                request.setAttribute(GwtRpcPayload.OFBIZ_PAYLOAD, resultMap);
                request.setAttribute(GwtRpcPayload.REQUEST_PAYLOAD, requestPayload);
               
                /*try {
                        rd.forward(request, response);
                } catch(IOException ioe) {
                throw new EventHandlerException("IO Exception while forwarding request to GWT RPC servlet  : ",ioe);
            } catch(ServletException se) {
                throw new EventHandlerException("Servlet Exception while forwarding request to GWT RPC servlet  : ",se);
            }*/
               
                try {
               
                GwtRpcServletUtil servletUtil = new GwtRpcServletUtil();
                String responsePayload = servletUtil.invokeServlet(request, response, requestPayload);
                servletUtil.writeResponse(servletContext, request, response, responsePayload);

            } catch(Exception e) {
                Debug.logError(e, "gwt remote servlet invocation failed " + e.getMessage(), module);
                throw new EventHandlerException(e);
            }

        } else {
                throw new EventHandlerException("GWT parameters are null  : " + gwtParameters);
        }

        return "success";
    }
}

