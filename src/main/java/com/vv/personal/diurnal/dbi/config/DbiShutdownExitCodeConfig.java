package com.vv.personal.diurnal.dbi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Vivek
 * @since 29/10/21
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "dbi.shutdown.exit")
public class DbiShutdownExitCodeConfig {
    private int code;
}