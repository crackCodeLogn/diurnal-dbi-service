package com.vv.personal.diurnal.dbi;

import com.vv.personal.diurnal.dbi.config.DbiConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static com.vv.personal.diurnal.dbi.constants.Constants.*;

@Slf4j
@ComponentScan({"com.vv.personal.diurnal.dbi", "com.vv.personal.diurnal.ping"})
@SpringBootApplication
public class DiurnalDbiServer {
    @Autowired
    private Environment environment;

    @Autowired
    private DbiConfig dbiConfig;

    public static void main(String[] args) {
        SpringApplication.run(DiurnalDbiServer.class, args);
    }

    @Bean
    ProtobufHttpMessageConverter protobufHttpMessageConverter() {
        return new ProtobufHttpMessageConverter();
    }

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.vv.personal.diurnal"))
                .build();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void firedUpAllCylinders() {
        String host = LOCALHOST;
        try {
            host = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.error("Failed to obtain ip address. ", e);
        }
        String port = environment.getProperty(LOCAL_SPRING_PORT);
        String herokuHost = environment.getProperty(SPRING_APPLICATION_HEROKU);
        log.info("'{}' activation is complete! Expected Heroku Swagger running on: {}, exact url: {}",
                environment.getProperty("spring.application.name"),
                String.format(HEROKU_SWAGGER_UI_URL, herokuHost),
                String.format(SWAGGER_UI_URL, host, port));

        //dbiCacheController.populateAllRefCache();
        //log.info("Prepped overall cache => {}", dbiCacheController.displayAllRefTableCache());
    }
}