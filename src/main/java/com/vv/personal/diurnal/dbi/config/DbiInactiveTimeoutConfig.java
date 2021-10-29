package com.vv.personal.diurnal.dbi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Vivek
 * @since 08/04/21
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "dbi.inactive.timeout")
public class DbiInactiveTimeoutConfig {
    private boolean enabled;
    private Integer seconds;
}