package tech.nmhillusion.jParrotDataSelectorApp.state;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * created by: nmhillusion
 * <p>
 * created date: 2025-12-28
 */
public class TextAreaLineChangeListener implements DocumentListener {
    private final Runnable handleLineCountEventFunc;

    public TextAreaLineChangeListener(Runnable handleLineCountEventFunc) {
        this.handleLineCountEventFunc = handleLineCountEventFunc;
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        SwingUtilities.invokeLater(this.handleLineCountEventFunc);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        SwingUtilities.invokeLater(this.handleLineCountEventFunc);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        SwingUtilities.invokeLater(this.handleLineCountEventFunc);
    }
}
