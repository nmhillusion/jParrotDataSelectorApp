package tech.nmhillusion.jParrotDataSelectorApp.screen;

import tech.nmhillusion.jParrotDataSelectorApp.loader.DatabaseLoader;
import tech.nmhillusion.jParrotDataSelectorApp.model.DatasourceModel;
import tech.nmhillusion.jParrotDataSelectorApp.model.QueryResultModel;
import tech.nmhillusion.jParrotDataSelectorApp.screen.panel.HeaderPanel;
import tech.nmhillusion.jParrotDataSelectorApp.screen.panel.QueryResultPanel;
import tech.nmhillusion.jParrotDataSelectorApp.screen.panel.SqlEditorPanel;
import tech.nmhillusion.jParrotDataSelectorApp.state.ExecutionState;
import tech.nmhillusion.jParrotDataSelectorApp.state.LoadingStateListener;
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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

import static tech.nmhillusion.n2mix.helper.log.LogHelper.getLogger;

/**
 * created by: nmhillusion
 * <p>
 * created date: 2025-05-10
 */
@Neon
public class MainFrame extends JPanel implements LoadingStateListener {
    private static final EmptyBorderSize EMPTY_BORDER_SIZE = new EmptyBorderSize(2, 5, 2, 5);
    private final ExecutionState executionState;
    private final HeaderPanel headerPanel;
    private final SqlEditorPanel sqlEditorPanel;
    private final QueryResultPanel queryResultPanel;
    private final DatabaseLoader databaseLoader;
    private final JButton btnExec = new JButton("Execute");
    private final JButton btnStop = new JButton("Stop");
    private final JProgressBar progressBar = new JProgressBar();

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

        executionState.addListener(this::updateStateOfActonBox);
    }

    private void updateStateOfActonBox() {
        final DatasourceModel datasourceModel = executionState.getDatasourceModel();
        final SwingWorker<?, ?> currentBackgroundWorker = executionState.getCurrentBackgroundWorker();
        final boolean isLoading = executionState.getIsLoading();

        btnExec.setEnabled(null != datasourceModel && !isLoading);
        btnStop.setEnabled(null != currentBackgroundWorker && isLoading);
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
            headerPanel.setLoadingStateListener(this);
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

            btnExec.setPreferredSize(new Dimension(200, 30));
            btnExec.addActionListener(this::onClickExecSql);
            btnExec.setAlignmentX(Component.RIGHT_ALIGNMENT);
            btnExec.setCursor(
                    Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            );
            btnExecPanel.add(btnExec);

            btnStop.setPreferredSize(new Dimension(80, 30));
            btnStop.addActionListener(this::onClickStopSql);
            btnStop.setAlignmentX(Component.RIGHT_ALIGNMENT);
            btnStop.setCursor(
                    Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            );
            btnExecPanel.add(btnStop);

            add(
                    btnExecPanel
            );
            setHeightForComponent(btnExecPanel, 50);
        }

        {
            add(progressBar);
            setHeightForComponent(progressBar, 20);
            onLoadingStateChange(false);
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
            queryResultPanel.setLoadingStateListener(this);
        }
    }

    private void onClickStopSql(ActionEvent evt) {
        final SwingWorker<?, ?> currentBackgroundWorker = executionState.getCurrentBackgroundWorker();
        if (null == currentBackgroundWorker) {
            JOptionPane.showMessageDialog(
                    evt.getSource() instanceof JButton ? (JButton) evt.getSource() : this
                    , "No background worker to stop"
                    , "Warning"
                    , JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        if (currentBackgroundWorker.cancel(true)) {
            if (currentBackgroundWorker.isCancelled()) {
                executionState.setCurrentBackgroundWorker(null);
            }
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

            final CompletableFuture<List<QueryResultModel>> completableFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return doExecSqlQueries(sqlBlockList, databaseExecutor);
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            });

            completableFuture.whenComplete((value, ex) -> {
                SwingUtilities.invokeLater(() -> {
                    queryResultPanel.showResult(
                            value
                    );
                });
            });
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

    private List<QueryResultModel> doExecSqlQueries(List<String> sqlBlockList, DatabaseExecutor databaseExecutor) throws InterruptedException, ExecutionException {
        final SwingWorker<List<QueryResultModel>, Void> swingWorker = new SwingWorker<>() {
            @Override
            protected List<QueryResultModel> doInBackground() throws Exception {
                final List<QueryResultModel> queryResultList = new ArrayList<>();

                publish();
                try {
                    for (final String sqlText : sqlBlockList) {
                        publish();
                        getLogger(this).info("exec sql: {}", sqlText);

                        final DbExportDataModel dbExportDataModel = databaseExecutor.doReturningWork(conn ->
                                conn.doReturningPreparedStatement(sqlText, preparedStatement_ -> {
                                    final ResultSet resultSet = preparedStatement_.executeQuery();

                                    return ExtractResultToPage.buildDbExportDataModel(resultSet);
                                }));

                        queryResultList.add(new QueryResultModel(sqlText, dbExportDataModel));

                        publish();
                    }

                    {
                        /// Mark: TEST: longer executing time
                        Thread.sleep(10000);
                    }
                } catch (Throwable ex) {
                    getLogger(this).error(ex);
                    throw new SQLException(ex);
                }

                return queryResultList;
            }

            @Override
            protected void process(List<Void> chunks) {
                onLoadingStateChange(true);
            }

            @Override
            protected void done() {
                onLoadingStateChange(false);
            }
        };

        executionState.setCurrentBackgroundWorker(swingWorker);
        swingWorker.execute();

        return swingWorker.get();
    }

    @Override
    public void onLoadingStateChange(boolean isLoading) {
        executionState.setIsLoading(isLoading);
        progressBar.setIndeterminate(isLoading);
        progressBar.setValue(isLoading ? 0 : 100);
        progressBar.setVisible(isLoading);
    }

    record EmptyBorderSize(int top, int left, int bottom, int right) {
    }
}
