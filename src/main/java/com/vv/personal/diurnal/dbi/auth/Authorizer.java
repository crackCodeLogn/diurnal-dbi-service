package com.vv.personal.diurnal.dbi.auth;

import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @author Vivek
 * @since 09/01/21
 */
public class Authorizer {
    private final PasswordEncoder passwordEncoder;

    public Authorizer(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public String encode(String raw) {
        return passwordEncoder.encode(raw);
    }

    public boolean hashMatches(String incomingRawCred, String hash) {
        return passwordEncoder.matches(incomingRawCred, hash);
    }
}