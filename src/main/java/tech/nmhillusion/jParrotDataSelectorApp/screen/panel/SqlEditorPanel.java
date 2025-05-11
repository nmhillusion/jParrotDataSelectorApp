package tech.nmhillusion.jParrotDataSelectorApp.screen.panel;

import tech.nmhillusion.jParrotDataSelectorApp.state.ExecutionState;

import javax.swing.*;
import java.awt.*;

/**
 * created by: nmhillusion
 * <p>
 * created date: 2025-05-10
 */
public class SqlEditorPanel extends JPanel {
    private final ExecutionState executionState;
    private JTextArea sqlTextArea;

    public SqlEditorPanel(ExecutionState executionState) {
        this.executionState = executionState;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        this.sqlTextArea = new JTextArea();
        sqlTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        sqlTextArea.setText("SELECT * FROM dual;");
        sqlTextArea.setLineWrap(false);
        sqlTextArea.setRows(20);

        add(
                new JScrollPane(sqlTextArea), BorderLayout.CENTER
        );
    }

    public String getSqlText() {
        final String sqlText = sqlTextArea.getText();
        executionState.setSqlData(sqlText);
        return sqlText;
    }
}
