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
import javax.servlet.http.HttpSession;

import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.GroovyUtil;
import org.ofbiz.base.util.UtilHttp;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.webapp.control.ConfigXMLReader.Event;
import org.ofbiz.webapp.control.ConfigXMLReader.RequestMap;
import org.ofbiz.webapp.event.EventHandler;
import org.ofbiz.webapp.event.EventHandlerException;

import com.openforevent.gwt.gwtrpc.util.GwtRpcPayload;
import com.openforevent.gwt.gwtrpc.util.GwtRpcPayloadUtil;
import com.openforevent.gwt.gwtrpc.util.GwtRpcServletUtil;

public class GwtRpcGroovyEventHandler implements EventHandler {

    public static final String module = GwtRpcGroovyEventHandler.class.getName();
    
    private ServletContext servletContext = null;

    public void init(ServletContext servletContext) throws EventHandlerException {
        this.servletContext = servletContext;
    }

    public String invoke(Event event, RequestMap requestMap, HttpServletRequest request, HttpServletResponse response) throws EventHandlerException {

        if(Debug.infoOn()) {
                Debug.logInfo("In GwtRpcGroovyEventHandler", module);
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

                HttpSession session = request.getSession();
                
                Map<String, Object> groovyContext = FastMap.newInstance();
                groovyContext.put("request", request);
            groovyContext.put("response", response);
            groovyContext.put("session", session);
            groovyContext.put("dispatcher", request.getAttribute("dispatcher"));
            groovyContext.put("delegator", request.getAttribute("delegator"));
            groovyContext.put("security", request.getAttribute("security"));
            groovyContext.put("locale", UtilHttp.getLocale(request));
            groovyContext.put("timeZone", UtilHttp.getTimeZone(request));
            groovyContext.put("userLogin", session.getAttribute("userLogin"));
            
            Map<String, Object> parameters = UtilHttp.getCombinedMap(request, UtilMisc.toSet("delegator", "dispatcher", "security", "locale", "timeZone", "userLogin"));
            if(Debug.infoOn()) {
                        Debug.logInfo("parameters : " + parameters, module);
                }
            
            Set<String> keys = gwtParameters.keySet();
                Iterator<String> iter = keys.iterator();
                
                while(iter.hasNext()) {
                        String key = iter.next();
                        parameters.put(key, gwtParameters.get(key));
                }
            
                groovyContext.put("parameters", parameters);
                
                Object result = null;
                
                try {
                        result = GroovyUtil.runScriptAtLocation(event.path + event.invoke, groovyContext);
                        if(Debug.infoOn()) {
                        Debug.logInfo("groovy script result : " + result, module);
                }
                } catch(GeneralException ge) {
                        throw new EventHandlerException("Exception while executing groovy script : ",ge); 
                }

                Map<String, Object> resultMap = (Map<String, Object>) result;

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
