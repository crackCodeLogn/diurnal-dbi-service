package com.vv.personal.diurnal.dbi.config;

import com.vv.personal.diurnal.dbi.auth.Authorizer;
import com.vv.personal.diurnal.dbi.interactor.diurnal.cache.CachedDiurnal;
import com.vv.personal.diurnal.dbi.interactor.diurnal.dbi.tables.DiurnalTableEntryDay;
import com.vv.personal.diurnal.dbi.interactor.diurnal.dbi.tables.DiurnalTableUserMapping;
import com.vv.personal.diurnal.dbi.repository.EntryDayRepository;
import com.vv.personal.diurnal.dbi.repository.UserMappingRepository;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;

/**
 * @author Vivek
 * @since 29/10/21
 */
@Configuration
public class BeanStore {

    @Bean
    @Scope("prototype")
    public StopWatch procureStopWatch() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        return stopWatch;
    }

    @Bean
    public CachedDiurnal cachedDiurnal() {
        return new CachedDiurnal();
    }

    @Bean
    public Authorizer authorizer() {
        return new Authorizer(new Pbkdf2PasswordEncoder());
    }

    @Bean
    public DiurnalTableUserMapping diurnalTableUserMapping(UserMappingRepository userMappingRepository) {
        return new DiurnalTableUserMapping(userMappingRepository);
    }

    @Bean
    public DiurnalTableEntryDay diurnalTableEntryDays(EntryDayRepository entryDayRepository) {
        return new DiurnalTableEntryDay(entryDayRepository);
    }
}