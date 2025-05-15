package tech.nmhillusion.jParrotDataSelectorApp.screen.dialog;

import javax.swing.*;

/**
 * created by: nmhillusion
 * <p>
 * created date: 2025-05-15
 */
public class OptionDialogPane extends JScrollPane {

    public OptionDialogPane(String message) {
        this(message, 400, 200);
    }

    public OptionDialogPane(String message, int width, int height) {
        super();

        setPreferredSize(
                new java.awt.Dimension(width, height)
        );

        final JTextArea textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false); // Make it read-only
        textArea.setText(message);

        setViewportView(
                textArea
        );
    }
}
