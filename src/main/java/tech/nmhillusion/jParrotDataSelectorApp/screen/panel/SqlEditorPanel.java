package tech.nmhillusion.jParrotDataSelectorApp.screen.panel;

import tech.nmhillusion.jParrotDataSelectorApp.state.ExecutionState;
import tech.nmhillusion.neon_di.annotation.Inject;
import tech.nmhillusion.neon_di.annotation.Neon;

import javax.swing.*;
import java.awt.*;

/**
 * created by: nmhillusion
 * <p>
 * created date: 2025-05-10
 */
@Neon
public class SqlEditorPanel extends JPanel {
    private final ExecutionState executionState;
    private JTextArea sqlTextArea;

    public SqlEditorPanel(@Inject ExecutionState executionState) {
        this.executionState = executionState;
        setLayout(new BorderLayout());

        initComponents();
    }

    private void setHeightForComponent(Component comp, int height) {
        final Dimension preferredSize = new Dimension(Short.MAX_VALUE, height);
        comp.setPreferredSize(preferredSize);
        comp.setMaximumSize(preferredSize);
        comp.setMinimumSize(preferredSize);
    }

    private void initComponents() {
        final JLabel labelComp = new JLabel("SQL:");
        add(
                labelComp, BorderLayout.NORTH
        );

        this.sqlTextArea = new JTextArea();
        sqlTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        sqlTextArea.setText("SELECT * FROM t_user;\nSELECT * FROM t_user tu \nwhere tu.enabled = 1\norder by tu.username\n;");
        sqlTextArea.setLineWrap(false);
        sqlTextArea.setRows(18);

        final JScrollPane scrollPane = new JScrollPane(sqlTextArea);
        add(
                scrollPane, BorderLayout.CENTER
        );
    }

    public String getSqlText() {
        final String sqlText = sqlTextArea.getText();
        executionState.setSqlData(sqlText);
        return sqlText;
    }
}
