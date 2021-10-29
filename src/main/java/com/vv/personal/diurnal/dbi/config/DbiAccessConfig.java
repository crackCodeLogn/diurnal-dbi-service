package com.vv.personal.diurnal.dbi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Vivek
 * @since 30/10/21
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "dbi.access-gh")
public class DbiAccessConfig {
    private String baseUrl;
    private String token;
    private String repo;
    private String user;
    private String commitMessage;
    private UserMapping userMapping;

    @Data
    public static class UserMapping {
        private String folder;
        private String backupFileName;
    }
}