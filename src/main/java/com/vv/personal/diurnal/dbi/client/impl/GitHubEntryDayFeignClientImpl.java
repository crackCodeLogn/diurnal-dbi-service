package com.vv.personal.diurnal.dbi.client.impl;

import org.springframework.stereotype.Component;

/**
 * @author Vivek
 * @since 30/10/21
 */
@Component
public class GitHubEntryDayFeignClientImpl extends AbstractGitHubFeignClientImpl {

    @Override
    String getFolderName() {
        return dbiAccessConfig.getEntryDay().getFolder();
    }

    @Override
    String getBackupFileName() {
        return dbiAccessConfig.getEntryDay().getBackupFileName();
    }
}
