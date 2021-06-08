package com.vv.personal.diurnal.ping.controller.health;

import com.vv.personal.diurnal.artifactory.generated.ResponsePrimitiveProto;
import com.vv.personal.diurnal.dbi.util.DiurnalUtil;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Vivek
 * @since 07/12/20
 */
@RestController("health-controller")
@RequestMapping("/health")
public class HealthController {
    private static final Logger LOGGER = LoggerFactory.getLogger(HealthController.class);

    @GetMapping("/ping")
    @ApiOperation(value = "ping for heartbeat", hidden = true)
    ResponsePrimitiveProto.ResponsePrimitive ping() {
        String pingResult = "ALIVE-" + System.currentTimeMillis();
        LOGGER.info("PINGING back with status {}", pingResult);
        return DiurnalUtil.generateResponsePrimitiveString(pingResult);
    }
}
