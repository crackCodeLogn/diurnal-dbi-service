package com.vv.personal.diurnal.dbi.message.writer;

import com.vv.personal.diurnal.artifactory.generated.ResponsePrimitiveProto;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * @author Vivek
 * @since 31/10/21
 */
@Provider
@Produces("application/x-protobuf")
public class ResponsePrimitiveMessageBodyWriter implements MessageBodyWriter<ResponsePrimitiveProto.ResponsePrimitive> {

    @Override
    public boolean isWriteable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return ResponsePrimitiveProto.ResponsePrimitive.class.isAssignableFrom(aClass);
    }

    @Override
    public long getSize(ResponsePrimitiveProto.ResponsePrimitive responsePrimitive, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return responsePrimitive.getSerializedSize();
    }

    @Override
    public void writeTo(ResponsePrimitiveProto.ResponsePrimitive responsePrimitive, Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> multivaluedMap, OutputStream outputStream) throws IOException, WebApplicationException {
        outputStream.write(responsePrimitive.toByteArray());
    }
}