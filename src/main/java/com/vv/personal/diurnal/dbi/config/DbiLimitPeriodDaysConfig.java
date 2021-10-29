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
@ConfigurationProperties(prefix = "dbi.limit.period-days")
public class DbiLimitPeriodDaysConfig {
    private int cloud;
    private int trialPremium;
}