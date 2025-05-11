package tech.nmhillusion.jParrotDataSelectorApp.screen;

import tech.nmhillusion.jParrotDataSelectorApp.loader.DatabaseLoader;
import tech.nmhillusion.jParrotDataSelectorApp.model.DatasourceModel;
import tech.nmhillusion.jParrotDataSelectorApp.model.QueryResultModel;
import tech.nmhillusion.jParrotDataSelectorApp.screen.panel.HeaderPanel;
import tech.nmhillusion.jParrotDataSelectorApp.screen.panel.QueryResultPanel;
import tech.nmhillusion.jParrotDataSelectorApp.screen.panel.SqlEditorPanel;
import tech.nmhillusion.jParrotDataSelectorApp.state.ExecutionState;
import tech.nmhillusion.n2mix.helper.database.query.DatabaseExecutor;
import tech.nmhillusion.n2mix.helper.database.query.ExtractResultToPage;
import tech.nmhillusion.n2mix.model.database.DbExportDataModel;
import tech.nmhillusion.n2mix.util.StringUtil;
import tech.nmhillusion.n2mix.validator.StringValidator;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;

import static tech.nmhillusion.n2mix.helper.log.LogHelper.getLogger;

/**
 * created by: nmhillusion
 * <p>
 * created date: 2025-05-10
 */
public class MainFrame extends JPanel {
    private final ExecutionState executionState = new ExecutionState();
    private final HeaderPanel headerPanel = new HeaderPanel(executionState);
    private final SqlEditorPanel sqlEditorPanel = new SqlEditorPanel(executionState);
    private final QueryResultPanel queryResultPanel = new QueryResultPanel(executionState);
    private final DatabaseLoader databaseLoader = new DatabaseLoader();
    private final Map<String, DatabaseExecutor> datasourceExecutorMap = new TreeMap<>();

    public MainFrame() throws IOException {
        setLayout(new GridBagLayout());
//        setBackground(Color.CYAN);

        initComponents();
    }

    private void initComponents() throws IOException {
        int rowIdx = 0;

        final Insets defaultInsets = new Insets(2, 5, 2, 5);

        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = rowIdx++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbc.insets = defaultInsets;

        add(
                headerPanel, gbc
        );

        gbc.gridy = rowIdx++;
        add(
                new JSeparator(), gbc
        );

        gbc.gridy = rowIdx++;
        add(
                sqlEditorPanel, gbc
        );

        gbc.gridy = rowIdx++;
        add(
                new JSeparator(), gbc
        );

        gbc.gridy = rowIdx++;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        final JButton btnExec = new JButton("Execute");
        btnExec.setPreferredSize(new Dimension(200, 30));
        btnExec.addActionListener(e -> {
            try {
                onClickExecSql();
            } catch (Throwable ex) {
                throw new RuntimeException(ex);
            }
        });
        add(
                btnExec, gbc
        );

        gbc.gridy = rowIdx++;
        add(
                new JSeparator(), gbc
        );

        gbc.gridy = rowIdx++;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        add(
                queryResultPanel, gbc
        );
    }

    private void onClickExecSql() throws Throwable {
        final String sqlTextAll = sqlEditorPanel.getSqlText();
        getLogger(this).info("exec for sql: {}", sqlTextAll);

        java.util.List<String> sqlBlockList = Arrays.stream(StringUtil.trimWithNull(
                        sqlTextAll
                ).split(";"))
                .map(String::trim)
                .filter(Predicate.not(StringValidator::isBlank))
                .toList();

        getLogger(this).info("exec state: {}", executionState);

        final DatasourceModel datasourceModel = executionState.getDatasourceModel();

        getLogger(this).info("exec datasource: {}", datasourceModel);

        if (null == datasourceModel) {
            throw new IllegalStateException("Not found datasource");
        }

        final DatabaseExecutor databaseExecutor = datasourceExecutorMap.computeIfAbsent(datasourceModel.getDataSourceName()
                , datasourceName -> {
                    try {
                        return databaseLoader.prepareDatabaseExecutor(datasourceModel);
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                });

        java.util.List<QueryResultModel> queryResultList = new ArrayList<>();
        for (final String sqlText : sqlBlockList) {
            final DbExportDataModel dbExportDataModel = databaseExecutor.doReturningWork(conn ->
                    conn.doReturningPreparedStatement(sqlText, preparedStatement_ -> {
                        final ResultSet resultSet = preparedStatement_.executeQuery();

                        return ExtractResultToPage.buildDbExportDataModel(resultSet);
                    }));

            queryResultList.add(new QueryResultModel(sqlText, dbExportDataModel));
        }

        queryResultPanel.showResult(
                queryResultList
        );
    }
}
