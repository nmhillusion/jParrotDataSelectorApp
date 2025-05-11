package tech.nmhillusion.jParrotDataSelectorApp.screen.panel;

import tech.nmhillusion.jParrotDataSelectorApp.loader.DatabaseLoader;
import tech.nmhillusion.jParrotDataSelectorApp.model.DatasourceModel;
import tech.nmhillusion.jParrotDataSelectorApp.state.ExecutionState;

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
    private final ExecutionState executionState;

    public HeaderPanel(ExecutionState executionState) throws IOException {
        this.executionState = executionState;

        setLayout(new GridBagLayout());

        initComponents();
    }

    private JComboBox<DatasourceModel> buildDataSourceSelectionBox() throws IOException {
        final List<DatasourceModel> datasourceModels = new DatabaseLoader().loadDatasourceModels();

        final JComboBox<DatasourceModel> dataSourceSelectionBox = new JComboBox<>();

        for (final DatasourceModel datasourceModel : datasourceModels) {
            dataSourceSelectionBox.addItem(datasourceModel);

            if (null == executionState.getDatasourceModel()) {
                executionState.setDatasourceModel(datasourceModel);
            }
        }

        dataSourceSelectionBox.addItemListener(e -> {
            final Object selectedItem = e.getItem();
            if (selectedItem instanceof DatasourceModel datasourceModel) {
                executionState.setDatasourceModel(datasourceModel);
            }
        });

        dataSourceSelectionBox.setSelectedIndex(0);

        return dataSourceSelectionBox;
    }

    private void initComponents() throws IOException {
        final GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);

        add(
                new JLabel("Data Source:"), gridBagConstraints
        );

        final JComboBox<DatasourceModel> dataSourceSelectionBox = buildDataSourceSelectionBox();

        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;

        add(
                dataSourceSelectionBox, gridBagConstraints
        );
    }

}
