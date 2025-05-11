package tech.nmhillusion.jParrotDataSelectorApp.loader;

import tech.nmhillusion.jParrotDataSelectorApp.helper.PathHelper;
import tech.nmhillusion.jParrotDataSelectorApp.model.DatasourceModel;
import tech.nmhillusion.n2mix.constant.CommonConfigDataSourceValue;
import tech.nmhillusion.n2mix.exception.InvalidArgument;
import tech.nmhillusion.n2mix.helper.YamlReader;
import tech.nmhillusion.n2mix.helper.database.config.DataSourceProperties;
import tech.nmhillusion.n2mix.helper.database.config.DatabaseConfigHelper;
import tech.nmhillusion.n2mix.helper.database.query.DatabaseExecutor;
import tech.nmhillusion.n2mix.helper.database.query.DatabaseHelper;
import tech.nmhillusion.n2mix.helper.log.LogHelper;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * created by: nmhillusion
 * <p>
 * created date: 2025-05-10
 */
public class DatabaseLoader {
    private static final Pattern JDBC_URL_PATTERN = Pattern.compile("jdbc:(\\w+?)://.+?", Pattern.CASE_INSENSITIVE);

    private Optional<CommonConfigDataSourceValue.DataSourceConfig> getDataSourceConfigByDbTypeName(String jdbcUrl) {
        if (jdbcUrl == null) {
            return Optional.empty();
        }

        final var matcher = JDBC_URL_PATTERN.matcher(jdbcUrl.trim());

        if (!matcher.find()) {
            return Optional.empty();
        }

        final String dbTypeName = matcher.group(1);

        return switch (dbTypeName.toLowerCase()) {
            case "mysql" -> Optional.of(CommonConfigDataSourceValue.MYSQL_DATA_SOURCE_CONFIG);
            case "oracle" -> Optional.of(CommonConfigDataSourceValue.ORACLE_DATA_SOURCE_CONFIG);
            case "sqlserver" -> Optional.of(CommonConfigDataSourceValue.SQL_SERVER_DATA_SOURCE_CONFIG);
            default -> throw new IllegalStateException("Unexpected value: " + jdbcUrl);
        };
    }

    public DataSource generateDataSource(DataSourceProperties dataSourceProperties) {
        return DatabaseConfigHelper.INSTANCE.generateDataSource(
                dataSourceProperties
        );
    }

    public DatabaseExecutor prepareDatabaseExecutor(DatasourceModel datasourceModel) throws IOException, InvalidArgument {
        final DataSourceProperties dataSourceProperties = DataSourceProperties.generateFromDefaultDataSourceProperties(
                datasourceModel.getDataSourceName()
                , datasourceModel.getDataSourceConfig()
                , datasourceModel.getJdbcUrl()
                , datasourceModel.getUsername()
                , datasourceModel.getPassword()
        );

        final DataSource dataSource = generateDataSource(
                dataSourceProperties
        );

        final org.hibernate.SessionFactory sessionFactory = generateDbSession(
                dataSourceProperties
        );

        final DatabaseHelper databaseHelper = new DatabaseHelper(dataSource, sessionFactory);
        return databaseHelper.getExecutor();
    }

    public org.hibernate.SessionFactory generateDbSession(DataSourceProperties dataSourceProperties) throws IOException {
        return DatabaseConfigHelper.INSTANCE.generateSessionFactory(
                dataSourceProperties
        );
    }

    public List<DatasourceModel> loadDatasourceModels() throws IOException {
        final Path pathOfResource = PathHelper.getPathOfResource("config/db.example.yml");
        try (final InputStream fis = Files.newInputStream(pathOfResource)) {
            final List<?> databaseRawList = new YamlReader(fis).getProperty("databases", List.class);

            final List<DatasourceModel> resultList = new ArrayList<>();
            for (Object databaseRaw : databaseRawList) {
                if (databaseRaw instanceof Map<?, ?> databaseMap) {
                    final DatasourceModel datasourceModel = new DatasourceModel();

                    final String jdbcUrl = databaseMap.get("jdbcUrl").toString();
                    final Optional<CommonConfigDataSourceValue.DataSourceConfig> dataSourceConfigOpt = getDataSourceConfigByDbTypeName(jdbcUrl);

                    if (dataSourceConfigOpt.isEmpty()) {
                        LogHelper.getLogger(this).error(
                                "config for database: {}"
                                , databaseMap
                        );
                        continue;
                    }

                    datasourceModel.setDataSourceName(databaseMap.get("name").toString());
                    datasourceModel.setDataSourceConfig(
                            dataSourceConfigOpt.get()
                    );
                    datasourceModel.setJdbcUrl(jdbcUrl);
                    datasourceModel.setUsername(databaseMap.get("username").toString());
                    datasourceModel.setPassword(databaseMap.get("password").toString());

                    resultList.add(datasourceModel);
                }
            }

            return resultList;
        }

    }
}
