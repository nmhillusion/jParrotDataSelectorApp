package tech.nmhillusion.jParrotDataSelectorApp.screen.panel;

import tech.nmhillusion.jParrotDataSelectorApp.helper.PathHelper;
import tech.nmhillusion.jParrotDataSelectorApp.model.ClickExecuteSqlListener;
import tech.nmhillusion.jParrotDataSelectorApp.state.ExecutionState;
import tech.nmhillusion.jParrotDataSelectorApp.state.TextAreaLineChangeListener;
import tech.nmhillusion.n2mix.helper.YamlReader;
import tech.nmhillusion.n2mix.helper.log.LogHelper;
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    private int previousLineCount = 0;
    private JTextArea lineNumberGutter;

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

    private void initComponents() {
        final JLabel labelComp = new JLabel("SQL:");
        add(
                labelComp, BorderLayout.NORTH
        );

        final JPanel editorContainer = createEditorContainer();
        final JScrollPane scrollPane = new JScrollPane(editorContainer);
        add(
                scrollPane, BorderLayout.CENTER
        );

        handleLineCountEvent();
    }

    private JPanel createEditorContainer() {
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
        sqlTextArea.getDocument().addDocumentListener(new TextAreaLineChangeListener(this::handleLineCountEvent));

        final JPanel editorContainer = new JPanel();
        editorContainer.setLayout(new BorderLayout());
        editorContainer.add(
                sqlTextArea, BorderLayout.CENTER
        );

        lineNumberGutter = new JTextArea("1\n2\n");
        lineNumberGutter.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        lineNumberGutter.setEditable(false);
        lineNumberGutter.setLineWrap(false);
        lineNumberGutter.setBackground(Color.decode("#eeeeee"));
        lineNumberGutter.setForeground(Color.decode("#666666"));
        editorContainer.add(
                lineNumberGutter, BorderLayout.WEST
        );

        return editorContainer;
    }

    private void handleLineCountEvent() {
        int currentLineCount = sqlTextArea.getLineCount();
        if (currentLineCount != previousLineCount) {
            LogHelper.getLogger(this).info("Line count changed from {} to {}", previousLineCount, currentLineCount);
            previousLineCount = currentLineCount;

            lineNumberGutter.setText(
                    IntStream.range(1, currentLineCount + 1)
                            .mapToObj(String::valueOf)
                            .collect(Collectors.joining("\n"))
            );
        }
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
