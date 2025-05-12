package tech.nmhillusion.jParrotDataSelectorApp.screen.panel;

import tech.nmhillusion.jParrotDataSelectorApp.loader.DatabaseLoader;
import tech.nmhillusion.jParrotDataSelectorApp.model.DatasourceModel;
import tech.nmhillusion.jParrotDataSelectorApp.state.ExecutionState;
import tech.nmhillusion.neon_di.annotation.Inject;
import tech.nmhillusion.neon_di.annotation.Neon;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.List;

/**
 * created by: nmhillusion
 * <p>
 * created date: 2025-05-10
 */
@Neon
public class HeaderPanel extends JPanel {
    private final ExecutionState executionState;
    private final DatabaseLoader databaseLoader;

    public HeaderPanel(@Inject ExecutionState executionState, @Inject DatabaseLoader databaseLoader) throws IOException {
        this.executionState = executionState;
        this.databaseLoader = databaseLoader;

        setLayout(new GridBagLayout());

        initComponents();
    }

    private JComboBox<DatasourceModel> buildDataSourceSelectionBox() throws IOException {
        final List<DatasourceModel> datasourceModels = databaseLoader.loadDatasourceModels();

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
