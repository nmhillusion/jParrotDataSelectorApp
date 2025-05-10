package tech.nmhillusion.jParrotDataSelectorApp.state;

import tech.nmhillusion.n2mix.type.Stringeable;

/**
 * created by: nmhillusion
 * <p>
 * created date: 2025-05-10
 */
public class ExecutionState extends Stringeable {
    private String sqlData;

    public String getSql() {
        return sqlData;
    }

    public ExecutionState setSql(String sql) {
        this.sqlData = sql;
        return this;
    }
}
