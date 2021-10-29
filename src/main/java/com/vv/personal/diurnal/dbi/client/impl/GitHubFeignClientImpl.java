package com.vv.personal.diurnal.dbi.client.impl;

import com.google.common.collect.ImmutableMap;
import com.vv.personal.diurnal.dbi.config.DbiAccessConfig;
import com.vv.personal.diurnal.dbi.feign.GitHubFeignClient;
import com.vv.personal.diurnal.dbi.model.GitHubPayload;
import feign.Feign;
import feign.gson.GsonEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Map;

/**
 * @author Vivek
 * @since 30/10/21
 */
@Slf4j
@Component
public class GitHubFeignClientImpl {

    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_AUTH_TOKEN_FORMAT = "token %s";

    @Autowired
    private DbiAccessConfig dbiAccessConfig;

    public boolean backupAndUploadToGitHub(String backupData) {
        final ZonedDateTime zonedDateTime = ZonedDateTime.now();
        final GitHubFeignClient gitHubFeignClient = Feign.builder()
                .encoder(new GsonEncoder())
                .target(GitHubFeignClient.class, dbiAccessConfig.getBaseUrl());

        String data = getBase64(backupData);
        GitHubPayload gitHubPayload = GitHubPayload.builder()
                .message(String.format(dbiAccessConfig.getCommitMessage(), zonedDateTime))
                .content(data)
                .build();

        Map<String, String> map = ImmutableMap.<String, String>builder()
                .put(HEADER_AUTHORIZATION, String.format(HEADER_AUTH_TOKEN_FORMAT, dbiAccessConfig.getToken()))
                .build();

        log.info("Proceeding to writing backup of {} bytes!", data.getBytes().length);
        try {
            gitHubFeignClient.uploadBackup(dbiAccessConfig.getUser(), dbiAccessConfig.getRepo(),
                    String.format(dbiAccessConfig.getUserMapping().getFolder(), zonedDateTime.toLocalDate().toString()),
                    String.format(dbiAccessConfig.getUserMapping().getBackupFileName(), zonedDateTime.toInstant()),
                    gitHubPayload, map);
            log.info("Upload complete!");
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