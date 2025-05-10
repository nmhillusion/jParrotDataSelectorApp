package tech.nmhillusion.jParrotDataSelectorApp.screen.panel;

import javax.swing.*;
import java.awt.*;

/**
 * created by: nmhillusion
 * <p>
 * created date: 2025-05-10
 */
public class QueryResultPanel extends JPanel {
    public QueryResultPanel() {
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
        resultActionBox.add(
                new JButton("Copy"), BorderLayout.EAST
        );
        add(
                resultActionBox, gbc
        );


        final JTextArea resultTextArea = new JTextArea();
        resultTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        resultTextArea.setText("Result will be shown here");

        gbc.gridy = rowIdx++;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        add(
                new JScrollPane(resultTextArea), gbc
        );
    }

}
