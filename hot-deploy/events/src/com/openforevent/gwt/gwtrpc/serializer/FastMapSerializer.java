package com.openforevent.gwt.gwtrpc.serializer;

import java.util.HashMap;
import java.util.Map;

import javolution.util.FastMap;

import com.google.gwt.user.client.rpc.SerializationException;

public class FastMapSerializer extends AbstractPayloadSerializer<FastMap<String, Object>> {

        @Override
        public Object serialize(FastMap<String, Object> instance) throws SerializationException {
               
                HashMap<String, Object> outMap = new HashMap<String, Object>();
       
                FastMap<String, Object> inMap = instance;
               
                for (Map.Entry<String, Object> entry : inMap.entrySet()) {
                       
                        Object obj = entry.getValue();
                        String className = obj.getClass().getName();

                        PayloadSerializer serializer = getCachedPayloadSerializer(className);

                        if(serializer != null) {

                        try {
                                obj = serializer.serialize(obj);
                        } catch(SerializationException se) {
                                System.err.println("se -> " + se);
                        }
                }

                        outMap.put(entry.getKey(), obj);
            }
               
        return outMap;
                //return GwtRpcUtil.toObjectMap(instance);
        }

        @Override
        public Object deserialize(FastMap<String, Object> instance) throws SerializationException {

                return null;
        }
}


