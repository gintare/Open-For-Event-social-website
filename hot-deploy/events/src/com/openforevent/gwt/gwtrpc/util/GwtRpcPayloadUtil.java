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

package com.openforevent.gwt.gwtrpc.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.ofbiz.base.util.Debug;
import org.ofbiz.service.ServiceUtil;

import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.RPCServletUtils;

public class GwtRpcPayloadUtil extends ServiceUtil {

    public static final String module = GwtRpcPayloadUtil.class.getName();

    public static String getRequestPayload(HttpServletRequest request) throws ServletException, IOException {

        //String requestPayload = RPCServletUtils.readContentAsUtf8(request, true);
    	String requestPayload = RPCServletUtils.readContentAsGwtRpc(request);

        if(Debug.infoOn()) {
                Debug.logInfo("request : " + request.getRequestURI(), module);
                Debug.logInfo("requestPayload : " + requestPayload, module);
        }

        return requestPayload;
    }

    public static HashMap<String, String> getParameters(String requestPayload) {

        HashMap<String, String> parameters = null;
       
        RPCRequest rpcRequest = RPC.decodeRequest(requestPayload);
        Object[] obj = rpcRequest.getParameters();
        parameters = (HashMap<String, String>)obj[0];

        if(Debug.infoOn()) {
                Debug.logInfo("rpc request method : " + rpcRequest.getMethod(), module);
                Debug.logInfo("rpc request serialization policy : " + rpcRequest.getSerializationPolicy(), module);
                Debug.logInfo("rpc request flags : " + rpcRequest.getFlags(), module);
               
                Debug.logInfo("rpc request parameters : " + obj, module);
                Debug.logInfo("parameters map : " + parameters, module);
        }

        return parameters;
    }

    public static Map<String, Object> returnSuccessWithPayload(Object payload) {
        Map<String, Object> result = returnSuccess();
        result.put(GwtRpcPayload.PAYLOAD, payload);
        return result;
    }

    public static Map<String, Object> returnSuccessWithPayload(Object payload, String successMessage) {
        Map<String, Object> result = returnSuccess(successMessage);
        result.put(GwtRpcPayload.PAYLOAD, payload);
        return result;
    }

    public static Map<String, Object> returnSuccessWithPayload(Object payload, List<String> successMessageList) {
        Map<String, Object> result = returnSuccess(successMessageList);
        result.put(GwtRpcPayload.PAYLOAD, payload);
        return result;
    }
}

