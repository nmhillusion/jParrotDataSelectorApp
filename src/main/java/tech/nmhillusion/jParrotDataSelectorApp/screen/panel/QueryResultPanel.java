package tech.nmhillusion.jParrotDataSelectorApp.screen.panel;

import org.apache.poi.ss.usermodel.*;
import tech.nmhillusion.jParrotDataSelectorApp.helper.HtmlSanitizer;
import tech.nmhillusion.jParrotDataSelectorApp.helper.PathHelper;
import tech.nmhillusion.jParrotDataSelectorApp.helper.ViewHelper;
import tech.nmhillusion.jParrotDataSelectorApp.model.QueryResultModel;
import tech.nmhillusion.jParrotDataSelectorApp.model.SqlResultModel;
import tech.nmhillusion.jParrotDataSelectorApp.model.UpdateResultModel;
import tech.nmhillusion.jParrotDataSelectorApp.screen.dialog.OptionDialogPane;
import tech.nmhillusion.jParrotDataSelectorApp.state.ExecuteMode;
import tech.nmhillusion.jParrotDataSelectorApp.state.ExecutionState;
import tech.nmhillusion.jParrotDataSelectorApp.state.LoadingStateListener;
import tech.nmhillusion.n2mix.helper.YamlReader;
import tech.nmhillusion.n2mix.helper.office.excel.writer.ExcelDataSheet;
import tech.nmhillusion.n2mix.helper.office.excel.writer.ExcelWriteHelper;
import tech.nmhillusion.n2mix.helper.office.excel.writer.model.BasicExcelDataModel;
import tech.nmhillusion.n2mix.helper.storage.FileHelper;
import tech.nmhillusion.n2mix.model.database.DbExportDataModel;
import tech.nmhillusion.n2mix.util.CastUtil;
import tech.nmhillusion.n2mix.util.DateUtil;
import tech.nmhillusion.neon_di.annotation.Inject;
import tech.nmhillusion.neon_di.annotation.Neon;

import javax.swing.*;
import java.awt.*;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import static tech.nmhillusion.n2mix.helper.log.LogHelper.getLogger;

/**
 * created by: nmhillusion
 * <p>
 * created date: 2025-05-10
 */
@Neon
public class QueryResultPanel extends JPanel {
    private final ExecutionState executionState;
    private final JEditorPane resultTextArea = new JEditorPane();
    private final JButton btnCopy = new JButton("Copy");
    private final JButton btnExport = new JButton("Export Excels");
    private final ShowResultProperty showResultProperty;
    private final Path outputPath = PathHelper.getPathOfResource("output");
    private final int MAX_ROWS_OF_QUERY;
    private final String COLUMN_MORE_SIGN = "...";
    private java.util.List<QueryResultModel> cachedQueryResultList;
    private LoadingStateListener loadingStateListener;

    public QueryResultPanel(@Inject ExecutionState executionState) throws IOException {
        this.executionState = executionState;

        showResultProperty = new ShowResultProperty(
                getConfig("showResult.maxRows", Integer.class)
                , getConfig("showResult.maxColumns", Integer.class)
        );

        if (showResultProperty.maxRows() <= 0) {
            throw new IllegalArgumentException("showResult.maxRows must be greater than 0");
        }

        if (showResultProperty.maxColumns() <= 1) {
            throw new IllegalArgumentException("showResult.maxColumns must be greater than 1");
        }

        MAX_ROWS_OF_QUERY = getConfig("query.maxRows", Integer.class);

        if (MAX_ROWS_OF_QUERY <= 0) {
            throw new IllegalArgumentException("query.maxRows must be greater than 0");
        }

//        setBackground(Color.green);
        setLayout(new GridBagLayout());

        initComponents();

        executionState.addListener(this::updateStateOfActionButtons);
    }

    private <T> T getConfig(String configKey, Class<T> clazz2Cast) throws IOException {
        final Path configPath = PathHelper.getPathOfResource("config/app.config.yml");
        try (final InputStream fis = Files.newInputStream(configPath)) {
            return new YamlReader(fis).getProperty(configKey, clazz2Cast);
        }
    }

    private void buildActionBoxButtons(JPanel resultActionBox) throws IOException {
        btnCopy.setIcon(new ImageIcon(
                ViewHelper.getIconForButton("icon/copy-icon.png", 15, 15)
        ));
        btnCopy.setIconTextGap(5);
        btnCopy.setEnabled(false);
        btnCopy.addActionListener(this::copyResultToClipboard);
        btnCopy.setAlignmentX(Component.RIGHT_ALIGNMENT);
        btnCopy.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        btnExport.setIcon(new ImageIcon(
                ViewHelper.getIconForButton("icon/export-excel-icon.png", 15, 15)
        ));
        btnExport.setIconTextGap(5);
        btnExport.setEnabled(false);
        btnExport.addActionListener(this::exportResultToExcel);
        btnExport.setAlignmentX(Component.RIGHT_ALIGNMENT);
        btnExport.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        resultActionBox.add(Box.createHorizontalGlue()); // Push subsequent components to the right
        resultActionBox.add(btnCopy);
        resultActionBox.add(btnExport);
    }

    private void initComponents() throws IOException {
        int rowIdx = 0;
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = rowIdx++;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.BOTH;

        final JPanel resultActionBox = new JPanel();
        resultActionBox.setLayout(new BoxLayout(resultActionBox, BoxLayout.LINE_AXIS));
//        resultActionBox.setAlignmentX(Component.RIGHT_ALIGNMENT);
//        resultActionBox.setBackground(Color.green);
        buildActionBoxButtons(resultActionBox);
        add(
                resultActionBox, gbc
        );

        gbc.gridy = rowIdx++;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;

        add(
                new JLabel("Result:"), gbc
        );

        resultTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        resultTextArea.setEditable(false);
        resultTextArea.setContentType("text/html");

        gbc.gridy = rowIdx++;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        add(
                new JScrollPane(resultTextArea), gbc
        );
    }

    private void copyResultToClipboard(ActionEvent evt) {
        if (null == cachedQueryResultList || cachedQueryResultList.isEmpty()) {
            JOptionPane.showMessageDialog(
                    evt.getSource() instanceof JButton ? (JButton) evt.getSource() : this
                    , "No result to copy"
                    , "Warning"
                    , JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        resultTextArea.requestFocusInWindow(FocusEvent.Cause.ACTIVATION);
        resultTextArea.selectAll();
        resultTextArea.copy();

        JOptionPane.showMessageDialog(
                evt.getSource() instanceof JButton ? (JButton) evt.getSource() : this
                , "Result copied to clipboard"
                , "Success"
                , JOptionPane.INFORMATION_MESSAGE
        );
    }

    private List<String> minimalizeColumnsToDisplay(List<String> columns) {
        if (null == columns || columns.isEmpty()) {
            throw new IllegalArgumentException("columns cannot be null or empty");
        }

        if (showResultProperty.maxColumns() >= columns.size()) {
            return columns;
        }

        final List<String> minimalizedColumns = new ArrayList<>(
                columns.subList(0, showResultProperty.maxColumns() - 1)
        );
        minimalizedColumns.add(COLUMN_MORE_SIGN);

        return minimalizedColumns;
    }

    private StringBuilder showEachQueryResult(QueryResultModel queryResultModel) {
        final StringBuilder sb = new StringBuilder();

        sb.append(
                MessageFormat.format(
                        "<pre class='language-sql'><code>{0}</code></pre>"
                        , queryResultModel.sqlText()
                                .replace('\n', ' ')
                )
        );

        final DbExportDataModel dbExportDataModel = queryResultModel.dbExportDataModel();
        final List<List<String>> allRowsInResultModel = dbExportDataModel.getValues();

        sb.append("<p> Total rows: ").append(queryResultModel.totalRows()).append(". ");
        if (MAX_ROWS_OF_QUERY < queryResultModel.totalRows()) {
            sb.append("<i>(Only query first ").append(MAX_ROWS_OF_QUERY).append(" rows)</i>");
        }
        if (showResultProperty.maxRows() < allRowsInResultModel.size()) {
            sb.append("<i>(Only show first ").append(showResultProperty.maxRows()).append(" rows)</i>");
        }
        sb.append("</p>");

        sb.append("<table><thead>");
        final List<String> minimalizeHeaderColumnsToDisplay = minimalizeColumnsToDisplay(dbExportDataModel.getHeader());
        final String headerRow = String.join("",
                minimalizeHeaderColumnsToDisplay
                        .stream()
                        .map(it -> "<th style='border: solid 1px #eee;'>" + it + "</th>")
                        .toList()
        );

        sb.append(headerRow).append("</thead><tbody>");

        final List<List<String>> shownRows = new ArrayList<>(
                allRowsInResultModel.subList(0, Math.min(showResultProperty.maxRows(), allRowsInResultModel.size()))
        );

        for (final java.util.List<String> row : shownRows) {
            sb.append("<tr>");
            sb.append(String.join("",
                            minimalizeColumnsToDisplay(row)
                                    .stream()
                                    .map(it -> normalizeCellContentToDisplay(it))
                                    .map(it -> "<td style='border: solid 1px #eee;'>" + it + "</td>")
                                    .toList()
                    )
            );
            sb.append("</tr>");
        }

        if (allRowsInResultModel.size() > showResultProperty.maxRows()) {
            sb.append("<tr>");
            sb.append(String.join("",
                            minimalizeHeaderColumnsToDisplay
                                    .stream()
                                    .map(it -> "<td style='border: solid 1px #eee;'>...</td>")
                                    .toList()
                    )
            );
            sb.append("</tr>");
        }

        sb.append("</tbody></table><br><hr><br>");

        return sb;
    }

    private StringBuilder showEachUpdateResult(UpdateResultModel updateResultModel) {
        final StringBuilder sb = new StringBuilder();

        sb.append(
                MessageFormat.format(
                        "<pre class='language-sql'><code>{0}</code></pre>"
                        , updateResultModel.sqlText()
                                .replace('\n', ' ')
                )
        );

        sb.append(
                MessageFormat.format(
                        "<i style='color: green; font-weight: bold;font-style: italic;'> -- {0} rows affected </i>"
                        , updateResultModel.affectedRows()
                )
        );

        sb.append(
                "<br><hr><br>"
        );

        return sb;
    }

    private String normalizeCellContentToDisplay(String cellContent) {
        return HtmlSanitizer.escapeHtml(cellContent);
    }

    public void showResult(java.util.List<? extends SqlResultModel> sqlResultList_) {
        if (null == sqlResultList_ || sqlResultList_.isEmpty()) {
            resultTextArea.setText("No result");
            return;
        }

        switch (executionState.getExecuteMode()) {
            case SELECT -> showResultQuery(sqlResultList_
                    .stream()
                    .map(it -> CastUtil.safeCast(it, QueryResultModel.class))
                    .toList()
            );
            case UPDATE -> showResultUpdate(sqlResultList_
                    .stream()
                    .map(it -> CastUtil.safeCast(it, UpdateResultModel.class))
                    .toList()
            );
        }
    }

    private void showResultQuery(java.util.List<QueryResultModel> sqlResultList_) {
        final java.util.List<QueryResultModel> queryResultList = sqlResultList_
                .stream()
                .map(it -> {
                    final String sqlText = it.sqlText();

                    return new QueryResultModel(
                            MessageFormat.format("{0};", sqlText)
                            , it.totalRows()
                            , it.dbExportDataModel()
                    );
                })
                .toList();

        cachedQueryResultList = new ArrayList<>(queryResultList);

        final StringBuilder sb = new StringBuilder(
                1 == queryResultList.size()
                        ? "Query result:<hr><br>"
                        : MessageFormat.format("Query result of {0} queries:<hr><br>", queryResultList.size())
        );

        queryResultList.stream()
                .map(this::showEachQueryResult)
                .forEach(sb::append);

        resultTextArea.setText(sb.toString());
    }

    private void showResultUpdate(List<UpdateResultModel> resultList) {
        cachedQueryResultList = Collections.emptyList();

        final StringBuilder sb = new StringBuilder(
                1 == resultList.size()
                        ? "Update result:<hr><br>"
                        : MessageFormat.format("Update result of {0} SQLs:<hr><br>", resultList.size())
        );

        resultList.stream()
                .map(this::showEachUpdateResult)
                .forEach(sb::append);

        resultTextArea.setText(
                sb.toString()
        );
    }

    private void updateStateOfActionButtons() {
        final boolean resultAvailable = ExecuteMode.SELECT == executionState.getExecuteMode()
                && null != cachedQueryResultList
                && !cachedQueryResultList.isEmpty();

        final boolean isLoading = executionState.getIsLoading();

        btnCopy.setEnabled(!isLoading && resultAvailable);
        btnExport.setEnabled(!isLoading && resultAvailable);
    }

    private void prepareOutputFolder() throws IOException {
        if (!Files.exists(outputPath)) {
            Files.createDirectories(outputPath);
        }

        try (final Stream<Path> filelist = Files.list(outputPath)) {
            filelist.forEach(it -> {
                FileHelper.recursiveDeleteFolder(it.toFile());
            });
        }
    }

    private void exportResultToExcel(ActionEvent evt) {
        try {
            loadingStateListener.onLoadingStateChange(true);
            final SwingWorker<Path, Throwable> swingWorker = doExportResultToExcel(evt);

            executionState.setCurrentBackgroundWorker(swingWorker);
            swingWorker.execute();
        } catch (Throwable ex) {
            getLogger(this).error(ex);
            JOptionPane.showMessageDialog(
                    btnExport
                    , new OptionDialogPane("Error when export result: " + ex.getMessage())
                    , "Error"
                    , JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private SwingWorker<Path, Throwable> doExportResultToExcel(ActionEvent evt) {
        return new SwingWorker<>() {
            @Override
            protected Path doInBackground() throws Exception {
                try {
                    if (null == cachedQueryResultList || cachedQueryResultList.isEmpty()) {
                        throw new IllegalStateException("No result to export");
                    }

                    prepareOutputFolder();

                    final int queryListSize = cachedQueryResultList.size();
                    final String queryId = DateUtil.format(Date.from(Instant.now()), "yyyy-MM-dd_HH-mm");

                    for (int queryIdx = 0; queryIdx < queryListSize; queryIdx++) {
                        final QueryResultModel queryResultModel = cachedQueryResultList.get(queryIdx);
                        final DbExportDataModel dbExportDataModel = queryResultModel.dbExportDataModel();

                        final StringBuilder queryResultMetaData = new StringBuilder("Total rows: ").append(queryResultModel.totalRows());
                        if (MAX_ROWS_OF_QUERY < queryResultModel.totalRows()) {
                            queryResultMetaData.append(" (Only query first ").append(MAX_ROWS_OF_QUERY).append(" rows)");
                        }

                        final List<List<String>> exportDataContent = new ArrayList<>(dbExportDataModel.getValues());
                        if (MAX_ROWS_OF_QUERY < queryResultModel.totalRows()) {
                            exportDataContent.add(
                                    Collections.nCopies(exportDataContent
                                                    .getFirst().size()
                                            , "..."
                                    )
                            );
                        }

                        final byte[] queryData = new ExcelWriteHelper()
                                .addSheetData(
                                        new BasicExcelDataModel()
                                                .setHeaders(
                                                        List.of(
                                                                Collections.singletonList(
                                                                        queryResultMetaData.toString()
                                                                )
                                                                , dbExportDataModel.getHeader()
                                                        )
                                                )
                                                .setBodyData(
                                                        exportDataContent
                                                )
                                                .setSheetName("Data")
                                )
                                .addSheetData(
                                        new BasicExcelDataModel()
                                                .setHeaders(
                                                        List.of(
                                                                List.of("SQL")
                                                        )
                                                )
                                                .setBodyData(
                                                        List.of(
                                                                List.of(queryResultModel.sqlText())
                                                        )
                                                )
                                                .setSheetName("SQL")
                                        , (self, dataSheet, workbookRef, sheetRef) -> {
                                            formatForSqlSheet(
                                                    queryResultModel.sqlText()
                                                    , self
                                                    , dataSheet
                                                    , workbookRef
                                                    , sheetRef
                                            );
                                        }
                                )
                                .build();

                        final Path savePath = saveQueryData(queryId, queryIdx, queryData);
                        getLogger(this).info(
                                "saved path: {}", savePath
                        );
                    }

                    return outputPath;
                } catch (Exception ex) {
                    getLogger(this).error(ex);
                    throw ex;
                }
            }

            @Override
            protected void process(List<Throwable> chunks) {
                if (null != chunks && !chunks.isEmpty()) {
                    JOptionPane.showMessageDialog(
                            btnExport
                            , new OptionDialogPane("Error when export result: " + chunks.getFirst().getMessage())
                            , "Error"
                            , JOptionPane.ERROR_MESSAGE
                    );
                }
            }

            @Override
            protected void done() {
                try {
                    loadingStateListener.onLoadingStateChange(false);
                    ViewHelper.openFileExplorer(outputPath);
                } catch (IOException ex) {
                    getLogger(this).error(ex);
                    JOptionPane.showMessageDialog(
                            btnExport
                            , new OptionDialogPane("Error when open folder: " + ex.getMessage())
                            , "Error"
                            , JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };
    }

    private void formatForSqlSheet(String sqlText, ExcelWriteHelper self, ExcelDataSheet dataSheet, Workbook workbookRef, Sheet sheetRef) {
        final int columnIdxToFormat = 0;

        final Row row = sheetRef.getRow(1);
        if (null == row) {
            throw new IllegalStateException("Row not found");
        }

        final Cell sqlQueryCell = row.getCell(columnIdxToFormat);
        if (null == sqlQueryCell) {
            throw new IllegalStateException("Cell not found");
        }

        final int lineCount = sqlText.split("\n").length + 1;
        row.setHeightInPoints(sheetRef.getDefaultRowHeightInPoints() * lineCount);

        final CellStyle style = workbookRef.createCellStyle();
        style.setWrapText(true);
        sqlQueryCell.setCellStyle(style);
        sheetRef.autoSizeColumn(columnIdxToFormat);
    }

    private Path saveQueryData(String queryId, int queryIdx, byte[] queryData) throws IOException {
        final Path fileOutputPath = Path.of(String.valueOf(outputPath), "query-" + queryId + "--" + (queryIdx + 1) + ".xlsx");

        if (Files.exists(fileOutputPath)) {
            Files.delete(fileOutputPath);
        }

        try (final OutputStream fos = Files.newOutputStream(fileOutputPath)) {
            fos.write(queryData);
            fos.flush();
            return fileOutputPath;
        }
    }

    public void setLoadingStateListener(LoadingStateListener loadingStateListener) {
        this.loadingStateListener = loadingStateListener;
    }
}
