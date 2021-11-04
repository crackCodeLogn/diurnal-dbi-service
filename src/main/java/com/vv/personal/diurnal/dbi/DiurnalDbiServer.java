package com.vv.personal.diurnal.dbi;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.annotations.QuarkusMain;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.event.Observes;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
@QuarkusMain
public class DiurnalDbiServer {
    private static final String HEROKU_SWAGGER_UI_URL = "https://%s/swagger-ui/index.html";
    private static final String SWAGGER_UI_URL = "http://%s:%s/swagger-ui/index.html";
    private static final String LOCALHOST = "localhost";
    private static final String QUARKUS_PORT = "quarkus.http.port";
    private static final String APP_NAME = "app.name";
    private static final String APP_HEROKU = "app.heroku";

    @ConfigProperty(name = QUARKUS_PORT)
    Integer port;
    @ConfigProperty(name = APP_NAME)
    String appName;
    @ConfigProperty(name = APP_HEROKU)
    String herokuHost;

    public static void main(String[] args) {
        Quarkus.run(args);
    }

    void onStart(@Observes StartupEvent startupEvent) {
        log.info("********* Starting '{}' *********", appName);
        String host = LOCALHOST;
        try {
            host = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.error("Failed to obtain ip address. ", e);
        }
        log.info("Expected Heroku Swagger running on: {}, exact url: {}",
                String.format(HEROKU_SWAGGER_UI_URL, herokuHost),
                String.format(SWAGGER_UI_URL, host, port));
    }

    void onStop(@Observes ShutdownEvent shutdownEvent) {
        log.info("********* Shutting down '{}' *********", appName);
    }
}