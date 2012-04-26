package com.openforevent.gwt.gwtrpc.serializer;

import java.util.HashMap;
import java.util.Map;

import org.ofbiz.base.util.Debug;

public abstract class AbstractPayloadSerializer<E> implements PayloadSerializer<E> {

        public static final String module = AbstractPayloadSerializer.class.getName();
       
        private static final Map<String, PayloadSerializer<?>> payloadSerializerCache = new HashMap<String, PayloadSerializer<?>>();
       
        static {
                Debug.logInfo("Loading serializers ....", module);
                loadSerializers();
        }

        public static void putCachedPayloadSerializer(String className, PayloadSerializer<?> serializer) {
            synchronized (payloadSerializerCache) {
                payloadSerializerCache.put(className, serializer);
            }
        }

        public static PayloadSerializer<?> getCachedPayloadSerializer(String className) {
                synchronized (payloadSerializerCache) {
                        return payloadSerializerCache.get(className);
                }
        }
       
        public static void loadSerializers() {
               
                GenericValueSerializer genericValueSerializer = new GenericValueSerializer();
                putCachedPayloadSerializer("org.ofbiz.entity.GenericValue", genericValueSerializer);
               
                HashMapSerializer hashMapSerializer = new HashMapSerializer();
                putCachedPayloadSerializer("java.util.HashMap", hashMapSerializer);
               
                ArrayListSerializer arrayListSerializer = new ArrayListSerializer();
                putCachedPayloadSerializer("java.util.ArrayList", arrayListSerializer);
               
                FastMapSerializer fastMapSerializer = new FastMapSerializer();
                putCachedPayloadSerializer("javolution.util.FastMap", fastMapSerializer);

                FastListSerializer fastListSerializer = new FastListSerializer();
                putCachedPayloadSerializer("javolution.util.FastList", fastListSerializer);
        }
}

