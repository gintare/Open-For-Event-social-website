package com.openforevent.gwt.gwtrpc.serializer;

import com.google.gwt.user.client.rpc.SerializationException;

public interface PayloadSerializer<E> {

        public Object serialize(E instance) throws SerializationException;
        public Object deserialize(E instance) throws SerializationException;
}


