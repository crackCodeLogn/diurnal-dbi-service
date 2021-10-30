package com.vv.personal.diurnal.dbi.feign;

import com.vv.personal.diurnal.dbi.model.GitHubPayload;
import feign.HeaderMap;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

import java.util.Map;

/**
 * @author Vivek
 * @since 29/10/21
 */
public interface GitHubFeignClient {

    @RequestLine("PUT /repos/{user}/{repo}/contents/{folderPath}/{name}")
    @Headers("Content-Type: application/json")
    void uploadBackup(@Param("user") String user,
                      @Param("repo") String repo,
                      @Param("folderPath") String folderPath,
                      @Param("name") String fileName,
                      GitHubPayload gitHubPayload,
                      @HeaderMap Map<String, String> headerMap);
}