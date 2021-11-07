package com.vv.personal.diurnal.dbi.controller;

import com.vv.personal.diurnal.artifactory.generated.ResponsePrimitiveProto;
import com.vv.personal.diurnal.dbi.auth.Authorizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;

import static com.vv.personal.diurnal.dbi.constants.Constants.APPLICATION_X_PROTOBUF;
import static com.vv.personal.diurnal.dbi.util.DiurnalUtil.generateResponsePrimitiveBool;
import static com.vv.personal.diurnal.dbi.util.DiurnalUtil.generateResponsePrimitiveString;

/**
 * @author Vivek
 * @since 07/03/21
 */
@Slf4j
@Secured("user")
@RestController("auth-controller")
@RequestMapping("/diurnal/auth")
public class AuthController {
    @Inject
    Authorizer authorizer;

    @GetMapping(value = "/generate/hash", produces = APPLICATION_X_PROTOBUF)
    public ResponsePrimitiveProto.ResponsePrimitive generateHash(@RequestBody String rawCred) {
        log.info("Rx-ed raw-cred '{}' to hash", rawCred);
        String hash = authorizer.encode(rawCred);
        log.info("Generated hash [{}]", hash);
        return generateResponsePrimitiveString(hash);
    }

    @GetMapping("/generate/manual/hash")
    public String generateHashManually(@RequestParam String rawCred) {
        return generateHash(rawCred).getResponse();
    }

    @GetMapping(value = "/verify/cred-hash", produces = APPLICATION_X_PROTOBUF)
    public ResponsePrimitiveProto.ResponsePrimitive verifyRawCredToHash(@RequestParam String rawCred,
                                                                        @RequestParam String hash) {
        return generateResponsePrimitiveBool(authorizer.hashMatches(rawCred, hash));
    }

    @GetMapping("/verify/manual/cred-hash")
    public Boolean verifyRawCredToHashManually(@RequestParam String rawCred,
                                               @RequestParam String hash) {
        return generateResponsePrimitiveBool(authorizer.hashMatches(rawCred, hash)).getBoolResponse();
    }
}