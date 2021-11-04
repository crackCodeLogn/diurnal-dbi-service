package com.vv.personal.diurnal.dbi.config;

import io.smallrye.config.ConfigMapping;

/**
 * @author Vivek
 * @since 29/10/21
 */
@ConfigMapping(prefix = "dbi.shutdown.exit")
public interface DbiShutdownExitCodeConfig {
    int code();
}