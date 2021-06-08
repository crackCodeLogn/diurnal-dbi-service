package com.vv.personal.diurnal.dbi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author Vivek
 * @since 08/04/21
 */
@Configuration
public class TimerConfig {

    @Value("${dbi.inactive.timeout.enabled:false}")
    private boolean dbiInactiveTimeoutEnabled;

    @Value("${dbi.inactive.timeout.seconds:30}")
    private Integer dbiInactiveTimeoutSeconds;

    @Value("${dbi.shutdown.exit.code:0}")
    private Integer dbiShutdownExitCode;

    public boolean isDbiInactiveTimeoutEnabled() {
        return dbiInactiveTimeoutEnabled;
    }

    public Integer getDbiInactiveTimeoutSeconds() {
        return dbiInactiveTimeoutSeconds;
    }

    public Integer getDbiShutdownExitCode() {
        return dbiShutdownExitCode;
    }
}
