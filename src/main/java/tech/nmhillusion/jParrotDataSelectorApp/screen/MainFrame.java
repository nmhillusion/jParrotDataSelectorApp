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
import tech.nmhillusion.neon_di.annotation.Inject;
import tech.nmhillusion.neon_di.annotation.Neon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import static tech.nmhillusion.n2mix.helper.log.LogHelper.getLogger;

/**
 * created by: nmhillusion
 * <p>
 * created date: 2025-05-10
 */
@Neon
public class MainFrame extends JPanel {
    private static final EmptyBorderSize EMPTY_BORDER_SIZE = new EmptyBorderSize(2, 5, 2, 5);
    private final ExecutionState executionState;
    private final HeaderPanel headerPanel;
    private final SqlEditorPanel sqlEditorPanel;
    private final QueryResultPanel queryResultPanel;
    private final DatabaseLoader databaseLoader;

    public MainFrame(@Inject ExecutionState executionState,
                     @Inject HeaderPanel headerPanel,
                     @Inject SqlEditorPanel sqlEditorPanel,
                     @Inject QueryResultPanel queryResultPanel,
                     @Inject DatabaseLoader databaseLoader) throws IOException {
        this.executionState = executionState;
        this.headerPanel = headerPanel;
        this.sqlEditorPanel = sqlEditorPanel;
        this.queryResultPanel = queryResultPanel;
        this.databaseLoader = databaseLoader;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
//        setBackground(Color.CYAN);

        initComponents();
    }

    private void setHeightForComponent(Component comp, int height) {
        final Dimension preferredSize = new Dimension(Short.MAX_VALUE, height);

        comp.setPreferredSize(preferredSize);
        comp.setMaximumSize(preferredSize);
        comp.setMinimumSize(preferredSize);
    }

    private void initComponents() {
        {
            add(
                    headerPanel
            );
            setHeightForComponent(headerPanel, 50);
            headerPanel.setBorder(BorderFactory.createEmptyBorder(
                    EMPTY_BORDER_SIZE.top()
                    , EMPTY_BORDER_SIZE.left()
                    , EMPTY_BORDER_SIZE.bottom()
                    , EMPTY_BORDER_SIZE.right()
            ));
        }

        {
            add(
                    sqlEditorPanel
            );
            setHeightForComponent(sqlEditorPanel, 350);
            sqlEditorPanel.setBorder(BorderFactory.createEmptyBorder(
                    EMPTY_BORDER_SIZE.top()
                    , EMPTY_BORDER_SIZE.left()
                    , EMPTY_BORDER_SIZE.bottom()
                    , EMPTY_BORDER_SIZE.right()
            ));
        }

        {
            final JPanel btnExecPanel = new JPanel(new FlowLayout());

            final JButton btnExec = new JButton("Execute");
            btnExec.setPreferredSize(new Dimension(200, 30));
            btnExec.addActionListener(this::onClickExecSql);
            btnExec.setAlignmentX(Component.RIGHT_ALIGNMENT);
            btnExecPanel.add(btnExec);

            add(
                    btnExecPanel
            );
            setHeightForComponent(btnExecPanel, 50);
        }

        {
            add(
                    queryResultPanel
            );
            queryResultPanel.setBorder(BorderFactory.createEmptyBorder(
                    EMPTY_BORDER_SIZE.top()
                    , EMPTY_BORDER_SIZE.left()
                    , EMPTY_BORDER_SIZE.bottom()
                    , EMPTY_BORDER_SIZE.right()
            ));
        }
    }

    private void onClickExecSql(ActionEvent evt) {
        try {
            final String sqlTextAll = sqlEditorPanel.getSqlText();
            getLogger(this).info("exec for sql: {}", sqlTextAll);

            if (StringValidator.isBlank(sqlTextAll)) {
                JOptionPane.showMessageDialog(
                        evt.getSource() instanceof JButton ? (JButton) evt.getSource() : this
                        , "SQL is empty"
                        , "Warning"
                        , JOptionPane.WARNING_MESSAGE
                );

                return;
            }

            final List<String> sqlBlockList = Arrays.stream(StringUtil.trimWithNull(
                            sqlTextAll
                    ).split(";"))
                    .map(it -> StringUtil.trimWithNull(it) + ";")
                    .filter(Predicate.not(StringValidator::isBlank))
                    .toList();

            final String executionStateText = String.valueOf(executionState);
            getLogger(this).info("exec state: {}", executionStateText);

            final DatasourceModel datasourceModel = executionState.getDatasourceModel();

            getLogger(this).info("exec datasource: {}", datasourceModel);

            if (null == datasourceModel) {
                throw new IllegalStateException("Not found datasource");
            }

            final DatabaseExecutor databaseExecutor = databaseLoader.prepareDatabaseExecutor(datasourceModel, ex -> {
                getLogger(this).error(ex);

                JOptionPane.showMessageDialog(
                        evt.getSource() instanceof JButton ? (JButton) evt.getSource() : this
                        , "Error when connecting to database: " + ex.getMessage()
                        , "Error"
                        , JOptionPane.ERROR_MESSAGE
                );
            });

            final List<QueryResultModel> queryResultList = new ArrayList<>();
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

        } catch (Throwable ex) {
            getLogger(this).error(ex);
            JOptionPane.showMessageDialog(
                    evt.getSource() instanceof JButton ? (JButton) evt.getSource() : this
                    , "Error when executing sql: " + ex.getMessage()
                    , "Error"
                    , JOptionPane.ERROR_MESSAGE
            );
        }
    }

    record EmptyBorderSize(int top, int left, int bottom, int right) {
    }
}
