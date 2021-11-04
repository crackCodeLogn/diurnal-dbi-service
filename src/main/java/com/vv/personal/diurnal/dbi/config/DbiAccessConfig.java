package com.vv.personal.diurnal.dbi.config;

import io.smallrye.config.ConfigMapping;

/**
 * @author Vivek
 * @since 30/10/21
 */
@ConfigMapping(prefix = "dbi.access-gh")
public interface DbiAccessConfig {
    String baseUrl();

    String token();

    String repo();

    String user();

    String commitMessage();

    UserMapping userMapping();

    EntryDay entryDay();

    interface UserMapping {
        String folder();

        String backupFileName();
    }

    interface EntryDay {
        String folder();

        String backupFileName();
    }
}