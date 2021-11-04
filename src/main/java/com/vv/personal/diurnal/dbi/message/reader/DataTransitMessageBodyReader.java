package com.vv.personal.diurnal.dbi.message.reader;

import com.vv.personal.diurnal.artifactory.generated.DataTransitProto;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * @author Vivek
 * @since 31/10/21
 */
@Provider
@Consumes("application/x-protobuf")
public class DataTransitMessageBodyReader implements MessageBodyReader<DataTransitProto.DataTransit> {

    @Override
    public boolean isReadable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return DataTransitProto.DataTransit.class.isAssignableFrom(aClass);
    }

    @Override
    public DataTransitProto.DataTransit readFrom(Class<DataTransitProto.DataTransit> aClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> multivaluedMap, InputStream inputStream) throws IOException, WebApplicationException {
        return DataTransitProto.DataTransit.parseFrom(inputStream.readAllBytes());
    }
}