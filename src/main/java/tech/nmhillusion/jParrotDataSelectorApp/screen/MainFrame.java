package tech.nmhillusion.jParrotDataSelectorApp.screen;

import tech.nmhillusion.jParrotDataSelectorApp.helper.ViewHelper;
import tech.nmhillusion.jParrotDataSelectorApp.loader.DatabaseLoader;
import tech.nmhillusion.jParrotDataSelectorApp.loader.SqlPreprocessor;
import tech.nmhillusion.jParrotDataSelectorApp.model.DatasourceModel;
import tech.nmhillusion.jParrotDataSelectorApp.model.QueryResultModel;
import tech.nmhillusion.jParrotDataSelectorApp.model.SqlResultModel;
import tech.nmhillusion.jParrotDataSelectorApp.model.UpdateResultModel;
import tech.nmhillusion.jParrotDataSelectorApp.screen.dialog.OptionDialogPane;
import tech.nmhillusion.jParrotDataSelectorApp.screen.panel.HeaderPanel;
import tech.nmhillusion.jParrotDataSelectorApp.screen.panel.QueryResultPanel;
import tech.nmhillusion.jParrotDataSelectorApp.screen.panel.SqlEditorPanel;
import tech.nmhillusion.jParrotDataSelectorApp.state.ExecutionState;
import tech.nmhillusion.jParrotDataSelectorApp.state.LoadingStateListener;
import tech.nmhillusion.n2mix.helper.database.query.DatabaseExecutor;
import tech.nmhillusion.n2mix.validator.StringValidator;
import tech.nmhillusion.neon_di.annotation.Inject;
import tech.nmhillusion.neon_di.annotation.Neon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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

    private void initComponents() throws IOException {
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
            sqlEditorPanel.setClickExecuteSqlListener(this::onClickExecSql);
        }

        {
            final JPanel btnExecPanel = new JPanel(new FlowLayout());

            btnExec.setIcon(new ImageIcon(
                    ViewHelper.getIconForButton("icon/exec-icon.png", 15, 15)
            ));
            btnExec.setIconTextGap(5);
            btnExec.setPreferredSize(new Dimension(200, 30));
            btnExec.addActionListener(this::onClickExecSql);
            btnExec.setAlignmentX(Component.RIGHT_ALIGNMENT);
            btnExec.setCursor(
                    Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
            );
            btnExecPanel.add(btnExec);

            btnStop.setIcon(new ImageIcon(
                    ViewHelper.getIconForButton("icon/stop-icon.png", 15, 15)
            ));
            btnStop.setIconTextGap(5);
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

            final List<String> sqlBlockList = SqlPreprocessor
                    .of(executionState.getDatasourceModel().getDbNameType())
                    .parseSqlStatements(sqlTextAll);

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
                        , new OptionDialogPane("Error when connecting to database: " + ex.getMessage())
                        , "Error"
                        , JOptionPane.ERROR_MESSAGE
                );
            });

            final CompletableFuture<List<? extends SqlResultModel>> completableFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return doExecSqlQueries(datasourceModel, databaseExecutor, sqlBlockList);
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            });

            completableFuture.whenComplete((resultList, ex) -> {
                SwingUtilities.invokeLater(() -> {
                    queryResultPanel.showResult(
                            resultList
                    );
                });
            });
        } catch (Throwable ex) {
            getLogger(this).error(ex);
            JOptionPane.showMessageDialog(
                    evt.getSource() instanceof JButton ? (JButton) evt.getSource() : this
                    , new OptionDialogPane("Error when executing sql: " + ex.getMessage())
                    , "Error"
                    , JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private List<? extends SqlResultModel> doExecSqlQueries(DatasourceModel datasourceModel, DatabaseExecutor databaseExecutor, List<String> sqlBlockList) throws InterruptedException, ExecutionException {
        final SwingWorker<List<? extends SqlResultModel>, Throwable> swingWorker = new SwingWorker<>() {
            @Override
            protected List<? extends SqlResultModel> doInBackground() throws Exception {
                final List<QueryResultModel> queryResultList = new ArrayList<>();
                final List<UpdateResultModel> updatedResultList = new ArrayList<>();

                publish();
                try {
                    for (final String sqlText : sqlBlockList) {
                        publish();
                        getLogger(this).info("exec sql: {}", sqlText);

                        switch (executionState.getExecuteMode()) {
                            case SELECT -> queryResultList.add(
                                    databaseLoader.execSqlQuery(
                                            datasourceModel
                                            , databaseExecutor
                                            , sqlText
                                    )
                            );
                            case UPDATE -> updatedResultList.add(
                                    databaseLoader.execSqlUpdate(
                                            datasourceModel
                                            , databaseExecutor
                                            , sqlText
                                    )
                            );
                        }

                        publish();
                    }

//                    {
//                        /// Mark: TEST: longer executing time
//                        Thread.sleep(10000);
//                    }
                } catch (Throwable ex) {
                    getLogger(this).error(ex);
                    publish(new SQLException(ex));
                }

                return switch (executionState.getExecuteMode()) {
                    case SELECT -> queryResultList;
                    case UPDATE -> updatedResultList;
                };
            }

            @Override
            protected void process(List<Throwable> chunks) {
                if (null != chunks && !chunks.isEmpty()) {
                    onLoadingStateChange(false);
                    JOptionPane.showMessageDialog(
                            btnExec
                            , new OptionDialogPane("Error when executing sql: " + chunks.getFirst().getMessage())
                            , "Error"
                            , JOptionPane.ERROR_MESSAGE
                    );
                } else {
                    onLoadingStateChange(true);
                }
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
        progressBar.setVisible(isLoading);
    }

    record EmptyBorderSize(int top, int left, int bottom, int right) {
    }
}
