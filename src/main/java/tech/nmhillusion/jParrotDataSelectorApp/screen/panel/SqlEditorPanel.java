package tech.nmhillusion.jParrotDataSelectorApp.screen.panel;

import tech.nmhillusion.jParrotDataSelectorApp.helper.PathHelper;
import tech.nmhillusion.jParrotDataSelectorApp.state.ExecutionState;
import tech.nmhillusion.n2mix.helper.YamlReader;
import tech.nmhillusion.neon_di.annotation.Inject;
import tech.nmhillusion.neon_di.annotation.Neon;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * created by: nmhillusion
 * <p>
 * created date: 2025-05-10
 */
@Neon
public class SqlEditorPanel extends JPanel {
    private final ExecutionState executionState;
    private final SqlEditorProperty sqlEditorProperty;
    private JTextArea sqlTextArea;

    public SqlEditorPanel(@Inject ExecutionState executionState) throws IOException {
        this.executionState = executionState;
        sqlEditorProperty = new SqlEditorProperty(
                getAppConfig("testing.enable", boolean.class)
                , getAppConfig("testing.defaultSql", String.class)
        );

        setLayout(new BorderLayout());
        initComponents();
    }

    private <T> T getAppConfig(String configKey, Class<T> class2Cast) throws IOException {
        try (final InputStream configStream = Files.newInputStream(PathHelper.getPathOfResource("config/app.config.yml"))) {
            return new YamlReader(configStream).getProperty(configKey, class2Cast);
        }
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
        //-- Mark: TEST: default SQL to test
        if (sqlEditorProperty.testingEnable()) {
            sqlTextArea.setText(
                    sqlEditorProperty.defaultSql()
            );
        }
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
