package com.openforevent.gwt.gwtrpc.serializer;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.rpc.SerializationException;

public class HashMapSerializer extends AbstractPayloadSerializer<HashMap<String, Object>> {

        @Override
        public Object serialize(HashMap<String, Object> instance) throws SerializationException {

                HashMap<String, Object> outMap = new HashMap<String, Object>();
        
                HashMap<String, Object> inMap = instance;
                
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
        public Object deserialize(HashMap<String, Object> instance) throws SerializationException {

                return null;
        }
}

