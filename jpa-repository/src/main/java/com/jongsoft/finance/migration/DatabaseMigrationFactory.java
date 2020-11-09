package com.jongsoft.finance.migration;

import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Factory;

@Factory
public class DatabaseMigrationFactory {

    @Context
    public DatasourceMigrationJpa datasourceMigration(MigrationDatasourceConfiguration configuration) {
        return new DatasourceMigrationJpa(configuration);
    }

}
