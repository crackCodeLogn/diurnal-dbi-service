package com.vv.personal.diurnal.dbi.model;

import lombok.Builder;

/**
 * @author Vivek
 * @since 30/10/21
 */
@Builder
public class GitHubPayload {
    private String message;
    private String content; //Base64

    public GitHubPayload() {
    }

    public GitHubPayload(String message, String content) {
        this.message = message;
        this.content = content;
    }
}