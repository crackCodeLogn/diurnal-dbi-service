package com.vv.personal.diurnal.dbi.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * @author Vivek
 * @since 25/11/20
 */
@Component
public class ShutdownManager {

    @Autowired
    private ApplicationContext appContext;

    /*
     * Invoke with `0` to indicate no error or different code to indicate
     * abnormal exit. es: shutdownManager.initiateShutdown(0);
     **/
    public void initiateShutdown(int returnCode) {
        SpringApplication.exit(appContext, () -> returnCode);
        try {
            Thread.sleep(1200);
        } catch (InterruptedException ignored) {
        }
        System.exit(returnCode);
    }

}
