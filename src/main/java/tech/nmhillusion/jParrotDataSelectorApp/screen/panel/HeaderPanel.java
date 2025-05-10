package tech.nmhillusion.jParrotDataSelectorApp.screen.panel;

import javax.swing.*;
import java.awt.*;

/**
 * created by: nmhillusion
 * <p>
 * created date: 2025-05-10
 */
public class HeaderPanel extends JPanel {
    public HeaderPanel() {
        initComponents();
    }

    private JComboBox<String> buildDataSourceSelectionBox() {
        final JComboBox<String> dataSourceSelectionBox = new JComboBox<>();
        dataSourceSelectionBox.addItem("AIS");
        dataSourceSelectionBox.addItem("AMS");
        dataSourceSelectionBox.addItem("ACEVNPRD2");

        return dataSourceSelectionBox;
    }

    private void initComponents() {

        final JComboBox<String> dataSourceSelectionBox = buildDataSourceSelectionBox();
        setLayout(new GridBagLayout());

        final GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);

        add(
                new JLabel("Data Source:"), gridBagConstraints
        );

        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;

        add(
                dataSourceSelectionBox, gridBagConstraints
        );
    }

}
