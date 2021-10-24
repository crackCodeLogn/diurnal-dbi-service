package com.vv.personal.diurnal.ping.config;

import com.vv.personal.diurnal.ping.processor.Pinger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Vivek
 * @since 07/12/20
 */
@Configuration
public class HealthConfig {

    @Value("${ping.timeout:7}")
    private int pingTimeout;

    @Value("${ping.retry.count:5}")
    private int pingRetryCount;

    @Value("${ping.retry.timeout:3}")
    private int pingRetryTimeout;

    @Bean(destroyMethod = "destroyExecutor")
    public Pinger pinger() {
        return new Pinger(pingTimeout, pingRetryCount, pingRetryTimeout);
    }
}
