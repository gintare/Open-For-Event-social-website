package com.openforevent.gwt.gwtrpc.serializer;

import java.util.ArrayList;

import javolution.util.FastList;

import com.google.gwt.user.client.rpc.SerializationException;

public class FastListSerializer extends AbstractPayloadSerializer<FastList<Object>> {

        @Override
        public Object serialize(FastList<Object> instance) throws SerializationException {

                ArrayList<Object> outList = new ArrayList<Object>();

                FastList<Object> inList = instance;
                
                for(Object obj : inList) {
                        
                        String className = obj.getClass().getName();
                        
                        PayloadSerializer serializer = getCachedPayloadSerializer(className);

                        if(serializer != null) {

                        try {
                                obj = serializer.serialize(obj);
                        } catch(SerializationException se) {
                                System.err.println("se -> " + se);
                        }
                }
                
                outList.add(obj);
                }

                return outList;
        }

        @Override
        public Object deserialize(FastList<Object> instance) throws SerializationException {

                return null;
        }
}

