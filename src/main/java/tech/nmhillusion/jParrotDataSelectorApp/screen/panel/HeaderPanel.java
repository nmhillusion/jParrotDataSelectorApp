package tech.nmhillusion.jParrotDataSelectorApp.screen.panel;

import tech.nmhillusion.jParrotDataSelectorApp.loader.DatabaseLoader;
import tech.nmhillusion.jParrotDataSelectorApp.model.DatasourceModel;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;

/**
 * created by: nmhillusion
 * <p>
 * created date: 2025-05-10
 */
public class HeaderPanel extends JPanel {
    public HeaderPanel() throws IOException {
        initComponents();
    }

    private JComboBox<String> buildDataSourceSelectionBox() throws IOException {
        final List<DatasourceModel> datasourceModels = new DatabaseLoader().loadDatasourceModels();

        final JComboBox<String> dataSourceSelectionBox = new JComboBox<>();

        for (DatasourceModel datasourceModel : datasourceModels) {
            dataSourceSelectionBox.addItem(datasourceModel.getDataSourceName());
        }

        return dataSourceSelectionBox;
    }

    private void initComponents() throws IOException {

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
