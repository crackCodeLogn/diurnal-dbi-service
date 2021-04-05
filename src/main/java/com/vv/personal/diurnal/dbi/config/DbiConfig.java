package com.vv.personal.diurnal.dbi.config;


import com.vv.personal.diurnal.dbi.auth.Authorizer;
import com.vv.personal.diurnal.dbi.constants.DbConstants;
import com.vv.personal.diurnal.dbi.interactor.diurnal.cache.CachedDiurnal;
import com.vv.personal.diurnal.dbi.interactor.diurnal.dbi.DiurnalDbi;
import com.vv.personal.diurnal.dbi.interactor.diurnal.dbi.tables.DiurnalTableEntryDay;
import com.vv.personal.diurnal.dbi.interactor.diurnal.dbi.tables.DiurnalTableUserMapping;
import com.vv.personal.diurnal.dbi.util.DbiUtil;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

import static com.vv.personal.diurnal.dbi.constants.DbConstants.*;

/**
 * @author Vivek
 * @since 23/02/21
 */
@Configuration
public class DbiConfig {

    private final List<DiurnalDbi> diurnalDbis = new ArrayList<>();

    @Value("${dbi.tables.create.onStartup:true}")
    private boolean createTablesOnStartup;

    @Bean(initMethod = "getDbConnection", destroyMethod = "closeDbConnection")
    public DbiConfigForDiurnal DiurnalDbConnector() {
        return new DbiConfigForDiurnal();
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
    @Qualifier("DiurnalTableUserMapping")
    public DiurnalTableUserMapping diurnalTableUserMapping() {
        return new DiurnalTableUserMapping(DbConstants.TABLE_DIURNAL_USER_MAPPING, DbConstants.PRIMARY_COL_USER_MAPPING, DiurnalDbConnector(), cachedDiurnal(),
                DbiUtil::generateCreateTableSql, DIURNAL_USER_MAPPING_SQL);
    }

    @Bean
    @Qualifier("DiurnalTableEntryDay")
    public DiurnalTableEntryDay diurnalTableEntryDays() {
        return new DiurnalTableEntryDay(TABLE_DIURNAL_ENTRY_DAY, PRIMARY_COL_ENTRY_DAY, DiurnalDbConnector(), cachedDiurnal(),
                DbiUtil::generateCreateTableSql, DIURNAL_ENTRY_DAY_SQL);
    }

    @Bean(initMethod = "start")
    @Scope("prototype")
    public StopWatch stopWatch() {
        return new StopWatch();
    }

    @PostConstruct
    public void postHaste() {
        diurnalDbis.add(diurnalTableUserMapping());
        diurnalDbis.add(diurnalTableEntryDays());
    }

    public boolean isCreateTablesOnStartup() {
        return createTablesOnStartup;
    }

    public List<DiurnalDbi> getDiurnalDbis() {
        return diurnalDbis;
    }
}
