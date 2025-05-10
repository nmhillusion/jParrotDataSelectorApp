package tech.nmhillusion.jParrotDataSelectorApp.state;

import tech.nmhillusion.jParrotDataSelectorApp.model.DatasourceModel;
import tech.nmhillusion.n2mix.type.Stringeable;

/**
 * created by: nmhillusion
 * <p>
 * created date: 2025-05-10
 */
public class ExecutionState extends Stringeable {
    private DatasourceModel datasourceModel;
    private String sqlData;

    public DatasourceModel getDatasourceModel() {
        return datasourceModel;
    }

    public ExecutionState setDatasourceModel(DatasourceModel datasourceModel) {
        this.datasourceModel = datasourceModel;
        return this;
    }

    public String getSqlData() {
        return sqlData;
    }

    public ExecutionState setSqlData(String sqlData) {
        this.sqlData = sqlData;
        return this;
    }
}
