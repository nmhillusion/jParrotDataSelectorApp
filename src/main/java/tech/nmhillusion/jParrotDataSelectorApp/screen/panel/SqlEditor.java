package tech.nmhillusion.jParrotDataSelectorApp.screen.panel;

import javax.swing.*;
import java.awt.*;

/**
 * created by: nmhillusion
 * <p>
 * created date: 2025-05-10
 */
public class SqlEditor extends JPanel {

    public SqlEditor() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        final JTextArea sqlTextArea = new JTextArea();
        sqlTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        sqlTextArea.setText("SELECT * FROM dual;");
        sqlTextArea.setLineWrap(false);
        sqlTextArea.setRows(20);

        add(
                new JScrollPane(sqlTextArea), BorderLayout.CENTER
        );
    }


}
