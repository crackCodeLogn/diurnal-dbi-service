package com.vv.personal.diurnal.dbi.config;

import io.smallrye.config.ConfigMapping;

/**
 * @author Vivek
 * @since 08/04/21
 */
@ConfigMapping(prefix = "dbi.inactive.timeout")
public interface DbiInactiveTimeoutConfig {
    boolean enabled();

    Integer seconds();
}