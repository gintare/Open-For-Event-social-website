package com.openforevent.gwt.gwtrpc.serializer;

import org.ofbiz.entity.GenericValue;

import com.google.gwt.user.client.rpc.SerializationException;
import com.openforevent.gwt.gwtrpc.util.GwtRpcUtil;

public class GenericValueSerializer extends AbstractPayloadSerializer<GenericValue> {

        @Override
        public Object serialize(GenericValue instance) throws SerializationException {
                return GwtRpcUtil.toStringMap(instance, null);
        }
        
        @Override
        public Object deserialize(GenericValue instance) throws SerializationException {

                return null;
        }
}

