package com.vv.personal.diurnal.dbi.message.writer;

import com.vv.personal.diurnal.artifactory.generated.UserMappingProto;

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
 * @since 05/11/21
 */
@Provider
@Produces("application/x-protobuf")
public class UserMappingMessageBodyWriter implements MessageBodyWriter<UserMappingProto.UserMapping> {

    @Override
    public boolean isWriteable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return UserMappingProto.UserMapping.class.isAssignableFrom(aClass);
    }

    @Override
    public long getSize(UserMappingProto.UserMapping userMapping, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return userMapping.getSerializedSize();
    }

    @Override
    public void writeTo(UserMappingProto.UserMapping userMapping, Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> multivaluedMap, OutputStream outputStream) throws IOException, WebApplicationException {
        outputStream.write(userMapping.toByteArray());
    }
}