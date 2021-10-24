package com.vv.personal.diurnal.dbi.controller;

import com.vv.personal.diurnal.artifactory.generated.ResponsePrimitiveProto;
import com.vv.personal.diurnal.dbi.auth.Authorizer;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static com.vv.personal.diurnal.dbi.util.DiurnalUtil.generateResponsePrimitiveBool;
import static com.vv.personal.diurnal.dbi.util.DiurnalUtil.generateResponsePrimitiveString;

/**
 * @author Vivek
 * @since 07/03/21
 */
@RestController("auth-controller")
@RequestMapping("/diurnal/auth")
public class AuthController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private Authorizer authorizer;

    @ApiOperation(value = "generate hash", hidden = true)
    @GetMapping("/generate/hash")
    public ResponsePrimitiveProto.ResponsePrimitive generateHash(@RequestBody String rawCred) {
        LOGGER.info("Rx-ed raw-cred '{}' to hash", rawCred);
        String hash = authorizer.encode(rawCred);
        LOGGER.info("Generated hash [{}]", hash);
        return generateResponsePrimitiveString(hash);
    }

    @GetMapping("/generate/manual/hash")
    public String generateHashManually(@RequestParam String rawCred) {
        return generateHash(rawCred).getResponse();
    }

    @ApiOperation(value = "verify credential to hash", hidden = true)
    @GetMapping("/verify/cred-hash")
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
