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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ofbiz.base.util.UtilGenerics;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.RPCServletUtils;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.SerializationPolicyProvider;


public class GwtRpcServletUtil {

    private static final String module = GwtRpcServletUtil.class.getName();

    private static final SerializationPolicy serializationPolicy = RPC.getDefaultSerializationPolicy();
    
    private  final SerializationPolicyProvider serializationPolicyProvider = new SerializationPolicyProvider() {
        @Override
        public SerializationPolicy getSerializationPolicy(String moduleBaseURL, String serializationPolicyStrongName) {
            return serializationPolicy;
            
        }
    };
    
    public String invokeServlet(HttpServletRequest request, HttpServletResponse response, String requestPayload)
                                                                                        throws InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException, ClassNotFoundException, SerializationException, IOException {

        RemoteService target = getRemoteServiceTarget("com.openforevent.gwt.gwtrpc.server.GwtRpcServiceImpl", request, response);
        RPCRequest rpcRequest = RPC.decodeRequest(requestPayload, target.getClass(), serializationPolicyProvider);

        String responsePayload = RPC.invokeAndEncodeResponse(target, rpcRequest.getMethod(), rpcRequest.getParameters(), serializationPolicy, rpcRequest.getFlags());
        return responsePayload;
    }

    public RemoteService getRemoteServiceTarget(String path, HttpServletRequest request, HttpServletResponse response) 
                                                                                                        throws ClassNotFoundException, SecurityException, NoSuchMethodException, InstantiationException,
        IllegalAccessException, IllegalArgumentException, InvocationTargetException {

        Class<? extends RemoteService> cls = UtilGenerics.cast(Class.forName(path));
        Constructor<? extends RemoteService> cstr = cls.getConstructor(HttpServletRequest.class,
        HttpServletResponse.class);
        return cstr.newInstance(request, response);
    }

    protected boolean shouldCompressResponse(HttpServletRequest request, HttpServletResponse response, String responsePayload) {
        return RPCServletUtils.exceedsUncompressedContentLengthLimit(responsePayload);
    }

    public void writeResponse(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response, String responsePayload) throws IOException {
          
        boolean gzipEncode = RPCServletUtils.acceptsGzipEncoding(request) && shouldCompressResponse(request, response, responsePayload);
        gzipEncode = false;
        RPCServletUtils.writeResponse(servletContext, response, responsePayload, gzipEncode);
    }
}

