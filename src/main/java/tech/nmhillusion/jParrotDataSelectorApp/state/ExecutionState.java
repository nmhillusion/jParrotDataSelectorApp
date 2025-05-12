package tech.nmhillusion.jParrotDataSelectorApp.state;

import tech.nmhillusion.jParrotDataSelectorApp.model.DatasourceModel;
import tech.nmhillusion.n2mix.type.Stringeable;
import tech.nmhillusion.neon_di.annotation.Neon;

import java.util.ArrayList;
import java.util.List;

/**
 * created by: nmhillusion
 * <p>
 * created date: 2025-05-10
 */
@Neon
public class ExecutionState extends Stringeable {
    private final List<ExecutionStateListener> listeners = new ArrayList<>();
    private DatasourceModel datasourceModel;
    private String sqlData;

    public DatasourceModel getDatasourceModel() {
        return datasourceModel;
    }

    public ExecutionState setDatasourceModel(DatasourceModel datasourceModel) {
        this.datasourceModel = datasourceModel;
        triggerListeners();
        return this;
    }

    public String getSqlData() {
        return sqlData;
    }

    public ExecutionState setSqlData(String sqlData) {
        this.sqlData = sqlData;
        triggerListeners();
        return this;
    }

    public void addListener(ExecutionStateListener listener) {
        listeners.add(listener);
    }

    private void triggerListeners() {
        listeners.forEach(ExecutionStateListener::updated);
    }
}
