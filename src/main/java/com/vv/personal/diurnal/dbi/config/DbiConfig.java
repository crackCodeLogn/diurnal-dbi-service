package com.vv.personal.diurnal.dbi.config;


import com.vv.personal.diurnal.dbi.auth.Authorizer;
import com.vv.personal.diurnal.dbi.interactor.diurnal.cache.CachedDiurnal;
import com.vv.personal.diurnal.dbi.interactor.diurnal.dbi.tables.DiurnalTableEntryDay;
import com.vv.personal.diurnal.dbi.interactor.diurnal.dbi.tables.DiurnalTableUserMapping;
import com.vv.personal.diurnal.dbi.repository.EntryDayRepository;
import com.vv.personal.diurnal.dbi.repository.UserMappingRepository;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;

/**
 * @author Vivek
 * @since 23/02/21
 */
@Configuration
public class DbiConfig {

    @Value("${dbi.tables.create.onStartup:false}")
    private boolean createTablesOnStartup;

    @Value("${dbi.trialPeriodDays}")
    private int trialPeriodDays;

    @Bean
    public CachedDiurnal cachedDiurnal() {
        return new CachedDiurnal();
    }

    @Bean
    public Authorizer authorizer() {
        return new Authorizer(new Pbkdf2PasswordEncoder());
    }

    @Bean
    @Qualifier("DiurnalTableUserMapping")
    public DiurnalTableUserMapping diurnalTableUserMapping(UserMappingRepository userMappingRepository) {
        return new DiurnalTableUserMapping(userMappingRepository);
    }

    @Bean
    @Qualifier("DiurnalTableEntryDay")
    public DiurnalTableEntryDay diurnalTableEntryDays(EntryDayRepository entryDayRepository) {
        return new DiurnalTableEntryDay(entryDayRepository);
    }

    @Bean(initMethod = "start")
    @Scope("prototype")
    public StopWatch stopWatch() {
        return new StopWatch();
    }

    public int getTrialPeriodDays() {
        return trialPeriodDays;
    }
}