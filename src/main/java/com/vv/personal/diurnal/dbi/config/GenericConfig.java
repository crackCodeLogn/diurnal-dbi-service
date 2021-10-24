package com.vv.personal.diurnal.dbi.config;

import org.apache.commons.lang3.time.StopWatch;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * @author Vivek
 * @since 05/03/21
 */
@Configuration
public class GenericConfig {

    @Scope("prototype")
    @Bean(initMethod = "start", destroyMethod = "stop")
    public StopWatch procureStopWatch() {
        return new StopWatch();
    }

}
