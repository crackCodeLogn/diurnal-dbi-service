package com.vv.personal.diurnal.ping.controller.health;

import com.vv.personal.diurnal.artifactory.generated.ResponsePrimitiveProto;
import com.vv.personal.diurnal.dbi.util.DiurnalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.vv.personal.diurnal.dbi.constants.Constants.APPLICATION_X_PROTOBUF;

/**
 * @author Vivek
 * @since 07/12/20
 */
@Slf4j
@RestController("health-controller")
@RequestMapping("/health")
public class HealthController {

    @GetMapping(value = "/ping", produces = APPLICATION_X_PROTOBUF)
    public ResponsePrimitiveProto.ResponsePrimitive ping() {
        String pingResult = "ALIVE-" + System.currentTimeMillis();
        log.info("PINGING back with status {}", pingResult);
        return DiurnalUtil.generateResponsePrimitiveString(pingResult);
    }
}