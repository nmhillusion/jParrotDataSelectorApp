package tech.nmhillusion.jParrotDataSelectorApp.screen;

import tech.nmhillusion.jParrotDataSelectorApp.screen.panel.HeaderPanel;
import tech.nmhillusion.jParrotDataSelectorApp.screen.panel.QueryResultPanel;
import tech.nmhillusion.jParrotDataSelectorApp.screen.panel.SqlEditor;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * created by: nmhillusion
 * <p>
 * created date: 2025-05-10
 */
public class MainFrame extends JPanel {
    public MainFrame() throws IOException {
        setLayout(new GridBagLayout());
//        setBackground(Color.CYAN);

        initComponents();
    }

    private void initComponents() throws IOException {
        int rowIdx = 0;

        final Insets defaultInsets = new Insets(2, 5, 2, 5);

        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = rowIdx++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 1;
        gbc.weighty = 0;
        gbc.insets = defaultInsets;

        add(
                new HeaderPanel(), gbc
        );

        gbc.gridy = rowIdx++;
        add(
                new JSeparator(), gbc
        );

        gbc.gridy = rowIdx++;
        add(
                new SqlEditor(), gbc
        );

        gbc.gridy = rowIdx++;
        add(
                new JSeparator(), gbc
        );

        gbc.gridy = rowIdx++;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        final JButton btnExec = new JButton("Execute");
        btnExec.setPreferredSize(new Dimension(200, 30));
        add(
                btnExec, gbc
        );

        gbc.gridy = rowIdx++;
        add(
                new JSeparator(), gbc
        );

        gbc.gridy = rowIdx++;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        add(
                new QueryResultPanel(), gbc
        );
    }
}
