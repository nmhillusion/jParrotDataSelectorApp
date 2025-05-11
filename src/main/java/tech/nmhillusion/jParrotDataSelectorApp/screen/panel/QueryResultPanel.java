package tech.nmhillusion.jParrotDataSelectorApp.screen.panel;

import tech.nmhillusion.jParrotDataSelectorApp.model.QueryResultModel;
import tech.nmhillusion.jParrotDataSelectorApp.state.ExecutionState;
import tech.nmhillusion.n2mix.model.database.DbExportDataModel;

import javax.swing.*;
import java.awt.*;
import java.text.MessageFormat;

/**
 * created by: nmhillusion
 * <p>
 * created date: 2025-05-10
 */
public class QueryResultPanel extends JPanel {
    private final ExecutionState executionState;
    private final JEditorPane resultTextArea = new JEditorPane();

    public QueryResultPanel(ExecutionState executionState) {
        this.executionState = executionState;

//        setBackground(Color.green);
        setLayout(new GridBagLayout());

        initComponents();
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
                MessageFormat.format("<pre class='language-sql'><code>{0}</code></pre>", queryResultModel.sqlText())
        );

        final DbExportDataModel dbExportDataModel = queryResultModel.dbExportDataModel();

        sb.append("<table><thead>");
        final String headerRow = String.join("", dbExportDataModel
                .getHeader()
                .stream()
                .map(it -> "<th border='1'>" + it + "</th>")
                .toList()
        );

        sb.append(headerRow).append("</thead><tbody>");

        for (final java.util.List<String> row : dbExportDataModel.getValues()) {
            sb.append("<tr>");
            sb.append(String.join("", row.stream()
                    .map(it -> "<td border='1'>" + it + "</td>")
                    .toList())
            );
            sb.append("</tr>");
        }

        sb.append("</tbody></table><br><hr><br>");

        return sb;
    }

    public void showResult(java.util.List<QueryResultModel> queryResultList) {

        final StringBuilder sb = new StringBuilder();

        queryResultList.stream()
                .map(this::showEachResult)
                .forEach(sb::append);

        resultTextArea.setText(sb.toString());
    }
}
