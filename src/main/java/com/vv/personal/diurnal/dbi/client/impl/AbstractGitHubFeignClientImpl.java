package com.vv.personal.diurnal.dbi.client.impl;

import com.google.common.collect.ImmutableMap;
import com.vv.personal.diurnal.dbi.config.DbiAccessConfig;
import com.vv.personal.diurnal.dbi.feign.GitHubFeignClient;
import com.vv.personal.diurnal.dbi.model.GitHubPayload;
import feign.Feign;
import feign.gson.GsonEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Map;

/**
 * @author Vivek
 * @since 30/10/21
 */
@Slf4j
public abstract class AbstractGitHubFeignClientImpl {

    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_AUTH_TOKEN_FORMAT = "token %s";

    @Autowired
    protected DbiAccessConfig dbiAccessConfig;

    abstract String getFolderName();

    abstract String getBackupFileName();

    public boolean backupAndUploadToGitHub(String backupData) {
        final ZonedDateTime zonedDateTime = ZonedDateTime.now();
        final GitHubFeignClient gitHubFeignClient = Feign.builder()
                .encoder(new GsonEncoder())
                .target(GitHubFeignClient.class, dbiAccessConfig.baseUrl());

        String data = getBase64(backupData);
        GitHubPayload gitHubPayload = GitHubPayload.builder()
                .message(String.format(dbiAccessConfig.commitMessage(), zonedDateTime))
                .content(data)
                .build();

        Map<String, String> headerMap = ImmutableMap.<String, String>builder()
                .put(HEADER_AUTHORIZATION, String.format(HEADER_AUTH_TOKEN_FORMAT, dbiAccessConfig.token()))
                .build();

        String folder = String.format(getFolderName(), zonedDateTime.toLocalDate().toString());
        String fileName = String.format(getBackupFileName(), zonedDateTime.toInstant());
        log.info("Proceeding to writing backup of {} bytes at '{}/{}'!", data.getBytes().length, folder, fileName);
        try {
            gitHubFeignClient.uploadBackup(headerMap, dbiAccessConfig.user(), dbiAccessConfig.repo(), folder, fileName,
                    gitHubPayload);
            log.info("Upload complete of '{}/{}'!", folder, fileName);
            return true;
        } catch (Exception e) {
            log.error("Failed to upload backup to github on '{}'. ", zonedDateTime, e);
        }
        return false;
    }

    private String getBase64(String data) {
        return Base64.getEncoder().encodeToString(data.getBytes());
    }
}