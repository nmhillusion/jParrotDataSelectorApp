package tech.nmhillusion.jParrotDataSelectorApp.config;

import tech.nmhillusion.n2mix.constant.CommonConfigDataSourceValue;
import tech.nmhillusion.n2mix.helper.database.config.DataSourceProperties;
import tech.nmhillusion.n2mix.helper.database.config.DatabaseConfigHelper;

import javax.sql.DataSource;
import java.io.IOException;

/**
 * created by: nmhillusion
 * <p>
 * created date: 2025-05-10
 */
public class DatabaseConfig {

    public DataSource generateDataSource() {
        final DataSourceProperties dataSourceProperties = DataSourceProperties.generateFromDefaultDataSourceProperties(
                "default_"
                , CommonConfigDataSourceValue.MYSQL_DATA_SOURCE_CONFIG
                , "jdbc:mysql://localhost:3306/common_database"
                , "root"
                , ""
        );

        return DatabaseConfigHelper.INSTANCE.generateDataSource(
                dataSourceProperties
        );
    }

    public org.hibernate.SessionFactory generateDbSession() throws IOException {
        final DataSourceProperties dataSourceProperties = DataSourceProperties.generateFromDefaultDataSourceProperties(
                "default_"
                , CommonConfigDataSourceValue.MYSQL_DATA_SOURCE_CONFIG
                , "jdbc:mysql://localhost:3306/common_database"
                , "root"
                , ""
        );

        return DatabaseConfigHelper.INSTANCE.generateSessionFactory(
                dataSourceProperties
        );
    }

}
