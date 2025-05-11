package tech.nmhillusion.jParrotDataSelectorApp.model;

import tech.nmhillusion.n2mix.constant.CommonConfigDataSourceValue;
import tech.nmhillusion.n2mix.type.Stringeable;

/**
 * created by: nmhillusion
 * <p>
 * created date: 2025-05-10
 */
public class DatasourceModel extends Stringeable {
    private String dataSourceName;
    private CommonConfigDataSourceValue.DataSourceConfig dataSourceConfig;
    private String jdbcUrl;
    private String username;
    private String password;

    public String getDataSourceName() {
        return dataSourceName;
    }

    public DatasourceModel setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
        return this;
    }

    public CommonConfigDataSourceValue.DataSourceConfig getDataSourceConfig() {
        return dataSourceConfig;
    }

    public DatasourceModel setDataSourceConfig(CommonConfigDataSourceValue.DataSourceConfig dataSourceConfig) {
        this.dataSourceConfig = dataSourceConfig;
        return this;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public DatasourceModel setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public DatasourceModel setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public DatasourceModel setPassword(String password) {
        this.password = password;
        return this;
    }

    @Override
    public String toString() {
        return dataSourceName;
    }
}
