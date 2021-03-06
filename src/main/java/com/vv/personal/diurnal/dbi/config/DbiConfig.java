package com.vv.personal.diurnal.dbi.config;


import com.vv.personal.diurnal.dbi.interactor.diurnal.*;
import com.vv.personal.diurnal.dbi.util.DbiUtil;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

import static com.vv.personal.diurnal.dbi.constants.Constants.*;

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
    @Qualifier("DiurnalTableUserMapping")
    public DiurnalTableUserMapping diurnalTableUserMapping() {
        return new DiurnalTableUserMapping(TABLE_DIURNAL_USER_MAPPING, PRIMARY_COL_USER_MAPPING, DiurnalDbConnector(), cachedDiurnal(),
                DbiUtil::generateCreateTableSql, "diurnal.user_mapping");
    }

    @Bean
    @Qualifier("DiurnalTableEntry")
    public DiurnalTableEntry diurnalTableEntries() {
        return new DiurnalTableEntry(TABLE_DIURNAL_ENTRY, PRIMARY_COL_ENTRY, DiurnalDbConnector(), cachedDiurnal(),
                DbiUtil::generateCreateTableSql, "diurnal.entry");
    }

    @Bean
    @Qualifier("DiurnalTableTitleMapping")
    public DiurnalTableTitleMapping diurnalTableTitleMapping() {
        return new DiurnalTableTitleMapping(TABLE_DIURNAL_TITLE_MAPPING, PRIMARY_COL_TITLE_MAPPING, DiurnalDbConnector(), cachedDiurnal(),
                DbiUtil::generateCreateTableSql, "diurnal.title_mapping");
    }

    @Bean(initMethod = "start")
    @Scope("prototype")
    public StopWatch stopWatch() {
        return new StopWatch();
    }

    @PostConstruct
    public void postHaste() {
        diurnalDbis.add(diurnalTableUserMapping());
        diurnalDbis.add(diurnalTableEntries());
        diurnalDbis.add(diurnalTableTitleMapping());
    }

    public boolean isCreateTablesOnStartup() {
        return createTablesOnStartup;
    }

    public List<DiurnalDbi> getDiurnalDbis() {
        return diurnalDbis;
    }
}
