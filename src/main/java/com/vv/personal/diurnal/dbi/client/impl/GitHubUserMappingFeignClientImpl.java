package com.vv.personal.diurnal.dbi.client.impl;

import org.springframework.stereotype.Component;

/**
 * @author Vivek
 * @since 30/10/21
 */
@Component
public class GitHubUserMappingFeignClientImpl extends AbstractGitHubFeignClientImpl {

    @Override
    String getFolderName() {
        return dbiAccessConfig.userMapping().folder();
    }

    @Override
    String getBackupFileName() {
        return dbiAccessConfig.userMapping().backupFileName();
    }
}