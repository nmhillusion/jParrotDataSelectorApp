package tech.nmhillusion.jParrotDataSelectorApp.model;

import tech.nmhillusion.n2mix.model.database.DbExportDataModel;

/**
 * created by: nmhillusion
 * <p>
 * created date: 2025-05-11
 */
public record QueryResultModel(String sqlText, long totalRows, DbExportDataModel dbExportDataModel) {
}
