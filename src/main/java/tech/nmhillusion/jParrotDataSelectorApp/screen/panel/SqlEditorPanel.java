package tech.nmhillusion.jParrotDataSelectorApp.screen.panel;

import tech.nmhillusion.jParrotDataSelectorApp.helper.PathHelper;
import tech.nmhillusion.jParrotDataSelectorApp.model.ClickExecuteSqlListener;
import tech.nmhillusion.jParrotDataSelectorApp.state.ExecutionState;
import tech.nmhillusion.n2mix.helper.YamlReader;
import tech.nmhillusion.n2mix.validator.StringValidator;
import tech.nmhillusion.neon_di.annotation.Inject;
import tech.nmhillusion.neon_di.annotation.Neon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import static tech.nmhillusion.n2mix.helper.log.LogHelper.getLogger;

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
    private ClickExecuteSqlListener clickExecuteSqlListener;

    public SqlEditorPanel(@Inject ExecutionState executionState) throws IOException {
        this.executionState = executionState;
        sqlEditorProperty = new SqlEditorProperty(
                getAppConfig("testing.enable", boolean.class)
                , getAppConfig("testing.defaultSql", String.class)
        );

        setLayout(new BorderLayout());
        initComponents();
    }

    public SqlEditorPanel setClickExecuteSqlListener(ClickExecuteSqlListener clickExecuteSqlListener) {
        this.clickExecuteSqlListener = clickExecuteSqlListener;
        return this;
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
        sqlTextArea.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyboardListener(e);
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
        sqlTextArea.setLineWrap(false);
        sqlTextArea.setRows(18);

        final JScrollPane scrollPane = new JScrollPane(sqlTextArea);
        add(
                scrollPane, BorderLayout.CENTER
        );
    }

    private void handleKeyboardListener(KeyEvent keyEvent) {
//        getLogger(this).info("keyEvent: {}", keyEvent);

        if (KeyEvent.VK_ENTER == keyEvent.getKeyCode() && keyEvent.isControlDown()) {
            getLogger(this).info("trigger execute SQL");
            if (null != clickExecuteSqlListener) {
                clickExecuteSqlListener.execute(
                        new ActionEvent(this, 0, "")
                );
            }
        }
    }

    public String getSqlText() {
        final String selectedText = sqlTextArea.getSelectedText();
        final String sqlText = StringValidator.isBlank(selectedText) ? sqlTextArea.getText() : selectedText;

        executionState.setSqlData(sqlText);
        return sqlText;
    }
}
