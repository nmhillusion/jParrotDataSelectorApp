package tech.nmhillusion.jParrotDataSelectorApp.screen.panel;

import tech.nmhillusion.jParrotDataSelectorApp.helper.PathHelper;
import tech.nmhillusion.jParrotDataSelectorApp.model.QueryResultModel;
import tech.nmhillusion.jParrotDataSelectorApp.state.ExecutionState;
import tech.nmhillusion.n2mix.helper.YamlReader;
import tech.nmhillusion.n2mix.model.database.DbExportDataModel;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.List;

/**
 * created by: nmhillusion
 * <p>
 * created date: 2025-05-10
 */
public class QueryResultPanel extends JPanel {
    private final ExecutionState executionState;
    private final JEditorPane resultTextArea = new JEditorPane();
    private final int maxRows;

    public QueryResultPanel(ExecutionState executionState) throws IOException {
        this.executionState = executionState;
        maxRows = getConfig("showResult.maxRows", Integer.class);

//        setBackground(Color.green);
        setLayout(new GridBagLayout());

        initComponents();
    }

    private <T> T getConfig(String configKey, Class<T> clazz2Cast) throws IOException {
        final Path configPath = PathHelper.getPathOfResource("config/app.config.yml");
        try (final InputStream fis = Files.newInputStream(configPath)) {
            return new YamlReader(fis).getProperty(configKey, clazz2Cast);
        }
    }

    private void initComponents() {
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
        resultActionBox.setLayout(new BorderLayout());
//        resultActionBox.setBackground(Color.green);
        final JButton btnCopy = new JButton("Copy");
        resultActionBox.add(
                btnCopy, BorderLayout.EAST
        );
        btnCopy.addActionListener(e -> copyResultToClipboard());
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

    private void copyResultToClipboard() {
        resultTextArea.select(0, resultTextArea.getText().length());
        resultTextArea.copy();

        JOptionPane.showMessageDialog(
                null
                , "Result copied to clipboard"
                , "Success"
                , JOptionPane.INFORMATION_MESSAGE
        );
    }

    private StringBuilder showEachResult(QueryResultModel queryResultModel) {
        final StringBuilder sb = new StringBuilder();

        sb.append(
                MessageFormat.format("<pre class='language-sql'><code>{0};</code></pre>", queryResultModel.sqlText())
        );

        final DbExportDataModel dbExportDataModel = queryResultModel.dbExportDataModel();
        final List<List<String>> allRows = dbExportDataModel.getValues();

        if (maxRows < allRows.size()) {
            sb.append("<p><i>(Only show first ").append(maxRows).append(" rows)</i></p>");
        }
        sb.append("<table><thead>");
        final String headerRow = String.join("", dbExportDataModel
                .getHeader()
                .stream()
                .map(it -> "<th style='border: solid 1px #eee;'>" + it + "</th>")
                .toList()
        );

        sb.append(headerRow).append("</thead><tbody>");

        final List<List<String>> shownRows = allRows.subList(0, Math.min(maxRows, allRows.size()));

        for (final java.util.List<String> row : shownRows) {
            sb.append("<tr>");
            sb.append(String.join("", row.stream()
                    .map(it -> "<td style='border: solid 1px #eee;'>" + it + "</td>")
                    .toList())
            );
            sb.append("</tr>");
        }

        sb.append("</tbody></table><br><hr><br>");

        return sb;
    }

    public void showResult(java.util.List<QueryResultModel> queryResultList) {
        final StringBuilder sb = new StringBuilder(
                MessageFormat.format("Query result of {0} queries.<hr><br>", queryResultList.size())
        );

        queryResultList.stream()
                .map(this::showEachResult)
                .forEach(sb::append);

        resultTextArea.setText(sb.toString());
    }
}
