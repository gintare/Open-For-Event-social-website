package com.openforevent.gwt.gwtrpc.serializer;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.SerializationException;

public class ArrayListSerializer extends AbstractPayloadSerializer<ArrayList<Object>> {

        @Override
        public Object serialize(ArrayList<Object> instance) throws SerializationException {

                ArrayList<Object> outList = new ArrayList<Object>();

                ArrayList<Object> inList = instance;
                
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
        public Object deserialize(ArrayList<Object> instance) throws SerializationException {

                return null;
        }
}

