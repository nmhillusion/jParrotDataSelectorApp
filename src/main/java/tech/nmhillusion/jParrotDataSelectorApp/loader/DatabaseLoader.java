package tech.nmhillusion.jParrotDataSelectorApp.loader;

import tech.nmhillusion.jParrotDataSelectorApp.constant.DbNameType;
import tech.nmhillusion.jParrotDataSelectorApp.helper.PathHelper;
import tech.nmhillusion.jParrotDataSelectorApp.model.DatasourceModel;
import tech.nmhillusion.jParrotDataSelectorApp.model.QueryResultModel;
import tech.nmhillusion.n2mix.constant.CommonConfigDataSourceValue;
import tech.nmhillusion.n2mix.helper.YamlReader;
import tech.nmhillusion.n2mix.helper.database.config.DataSourceProperties;
import tech.nmhillusion.n2mix.helper.database.config.DatabaseConfigHelper;
import tech.nmhillusion.n2mix.helper.database.query.DatabaseExecutor;
import tech.nmhillusion.n2mix.helper.database.query.DatabaseHelper;
import tech.nmhillusion.n2mix.helper.database.query.ExtractResultToPage;
import tech.nmhillusion.n2mix.model.database.DbExportDataModel;
import tech.nmhillusion.n2mix.type.function.VoidFunction;
import tech.nmhillusion.neon_di.annotation.Neon;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Pattern;

import static tech.nmhillusion.n2mix.helper.log.LogHelper.getLogger;

/**
 * created by: nmhillusion
 * <p>
 * created date: 2025-05-10
 */
@Neon
public class DatabaseLoader {
    private static final Pattern JDBC_URL_PATTERN = Pattern.compile("jdbc:(\\w+?):.+?", Pattern.CASE_INSENSITIVE);
    private final Map<String, DatabaseExecutor> datasourceExecutorMap = new TreeMap<>();
    private final int MAX_ROWS_OF_QUERY;

    public DatabaseLoader() throws IOException {
        MAX_ROWS_OF_QUERY = getAppConfig("query.maxRows", Integer.class);
    }

    private <T> T getAppConfig(String configKey, Class<T> clazz2Cast) throws IOException {
        final Path configPath = PathHelper.getPathOfResource("config/app.config.yml");
        try (final InputStream fis = Files.newInputStream(configPath)) {
            return new YamlReader(fis).getProperty(configKey, clazz2Cast);
        }
    }

    private Optional<DbNameType> getDbTypeNameByJdbcUrl(String jdbcUrl) {
        if (jdbcUrl == null) {
            return Optional.empty();
        }

        final var matcher = JDBC_URL_PATTERN.matcher(jdbcUrl.trim());

        if (!matcher.find()) {
            return Optional.empty();
        }

        final String dbTypeName = matcher.group(1);

        return Optional.ofNullable(
                DbNameType.from(dbTypeName)
        );
    }

    private Optional<CommonConfigDataSourceValue.DataSourceConfig> getDataSourceConfigByJdbcUrl(String jdbcUrl) {
        final Optional<DbNameType> dbTypeNameOpt = getDbTypeNameByJdbcUrl(jdbcUrl);

        return dbTypeNameOpt.flatMap(dbTypeName ->
                switch (dbTypeName) {
                    case MY_SQL -> Optional.of(CommonConfigDataSourceValue.MYSQL_DATA_SOURCE_CONFIG);
                    case ORACLE -> Optional.of(CommonConfigDataSourceValue.ORACLE_DATA_SOURCE_CONFIG);
                    case SQL_SERVER -> Optional.of(CommonConfigDataSourceValue.SQL_SERVER_DATA_SOURCE_CONFIG);
                    default -> throw new IllegalStateException("Unexpected value: " + jdbcUrl);
                }
        );
    }

    public DataSource generateDataSource(DataSourceProperties dataSourceProperties) {
        return DatabaseConfigHelper.INSTANCE.generateDataSource(
                dataSourceProperties
        );
    }

    public DatabaseExecutor prepareDatabaseExecutor(DatasourceModel datasourceModel, VoidFunction<Throwable> exceptionCallback) {
        return datasourceExecutorMap.computeIfAbsent(datasourceModel.getDataSourceName(), dsName -> {
            try {
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
            } catch (Exception ex) {
                exceptionCallback.apply(ex);
                return null;
            }
        });
    }

    public org.hibernate.SessionFactory generateDbSession(DataSourceProperties dataSourceProperties) throws IOException {
        return DatabaseConfigHelper.INSTANCE.generateSessionFactory(
                dataSourceProperties
        );
    }

    public List<DatasourceModel> loadDatasourceModels() throws IOException {
        final Path pathOfResource = PathHelper.getPathOfResource("config/db.yml");
        try (final InputStream fis = Files.newInputStream(pathOfResource)) {
            final List<?> databaseRawList = new YamlReader(fis).getProperty("databases", List.class);

            final List<DatasourceModel> resultList = new ArrayList<>();
            for (Object databaseRaw : databaseRawList) {
                if (databaseRaw instanceof Map<?, ?> databaseMap) {
                    final DatasourceModel datasourceModel = new DatasourceModel();

                    final String jdbcUrl = databaseMap.get("jdbcUrl").toString();
                    final Optional<CommonConfigDataSourceValue.DataSourceConfig> dataSourceConfigOpt = getDataSourceConfigByJdbcUrl(jdbcUrl);

                    if (dataSourceConfigOpt.isEmpty()) {
                        getLogger(this).error(
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

                    if (resultList
                            .stream()
                            .anyMatch(ds ->
                                    ds.getDataSourceName().equalsIgnoreCase(datasourceModel.getDataSourceName())
                            )
                    ) {
                        throw new IllegalStateException("Duplicate datasource name: " + datasourceModel.getDataSourceName());
                    }

                    resultList.add(datasourceModel);
                }
            }

            return resultList;
        }
    }

    public void checkConnection(DatasourceModel datasourceModel, VoidFunction<Throwable> exceptionCallback) {
        try {
            final DatabaseExecutor databaseExecutor = prepareDatabaseExecutor(datasourceModel, exceptionCallback);
            if (null == databaseExecutor) {
                throw new IllegalStateException("databaseExecutor is null");
            }

            final Optional<DbNameType> dbTypeNameOpt = getDbTypeNameByJdbcUrl(datasourceModel.getJdbcUrl());
            if (dbTypeNameOpt.isEmpty()) {
                throw new IllegalStateException("Database TypeName is empty");
            }

            final String testConnectionQuery = getAppConfig("database-validation." + dbTypeNameOpt.get().getValue(), String.class);
            getLogger(this).info("testConnectionQuery[{}]: {}", dbTypeNameOpt.get(), testConnectionQuery);

            databaseExecutor.doWork(conn -> {
                conn.doPreparedStatement(testConnectionQuery, PreparedStatement::executeQuery);
            });
        } catch (Throwable ex) {
            getLogger(this).error(ex);
            exceptionCallback.apply(ex);
        }
    }

    private long countRowsOfQuery(DatabaseExecutor databaseExecutor, String sqlText) throws Throwable {
        final String countQuery = MessageFormat.format(
                "select count(*) from ( {0} ) as sub"
                , sqlText
        );

        getLogger(this).info("countQuery: {}", countQuery);

        return databaseExecutor.doReturningWork(conn ->
                conn.doReturningPreparedStatement(countQuery, preparedStatement_ -> {
                    final ResultSet resultSet = preparedStatement_.executeQuery();

                    if (resultSet.next()) {
                        return resultSet.getLong(1);
                    }

                    return 0L;
                })
        );
    }

    private String wrapQueryWithLimitRow(DatasourceModel datasourceModel, String sqlText) {
        final Optional<DbNameType> dbTypeName = getDbTypeNameByJdbcUrl(datasourceModel.getJdbcUrl());
        if (dbTypeName.isEmpty()) {
            getLogger(this).error("Cannot get dbTypeName");
            return sqlText;
        }

        return switch (dbTypeName.get()) {
            case ORACLE -> MessageFormat.format(
                    """
                            select *
                            from ({0}) as subquery
                            where rownum <= {1}
                            """
                    , sqlText
                    , MAX_ROWS_OF_QUERY
            );

            case SQL_SERVER -> MessageFormat.format(
                    """
                            select top {0} *
                            from ({1}) as subquery
                            """
                    , MAX_ROWS_OF_QUERY
                    , sqlText
            );

            case MY_SQL -> MessageFormat.format(
                    """
                            select *
                            from ({0}) as subquery
                            limit {1}
                            """
                    , sqlText
                    , MAX_ROWS_OF_QUERY
            );

            default -> sqlText;
        };
    }

    public QueryResultModel execSqlQuery(DatasourceModel datasourceModel, DatabaseExecutor databaseExecutor, String sqlText) throws Throwable {
        final long totalRows = countRowsOfQuery(databaseExecutor, sqlText);
        String formalSqlText;

        if (totalRows > MAX_ROWS_OF_QUERY) {
            formalSqlText = wrapQueryWithLimitRow(datasourceModel, sqlText);
        } else {
            formalSqlText = sqlText;
        }

        getLogger(this).info("formal exec sql: {}", formalSqlText);
        final DbExportDataModel dbExportDataModel = databaseExecutor.doReturningWork(conn ->
                conn.doReturningPreparedStatement(formalSqlText, preparedStatement_ -> {
                    final ResultSet resultSet = preparedStatement_.executeQuery();

                    return ExtractResultToPage.buildDbExportDataModel(resultSet);
                }));

        return new QueryResultModel(
                sqlText
                , totalRows
                , dbExportDataModel
        );
    }

}
