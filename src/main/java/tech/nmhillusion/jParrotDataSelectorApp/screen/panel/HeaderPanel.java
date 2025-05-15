package tech.nmhillusion.jParrotDataSelectorApp.screen.panel;

import tech.nmhillusion.jParrotDataSelectorApp.helper.ViewHelper;
import tech.nmhillusion.jParrotDataSelectorApp.loader.DatabaseLoader;
import tech.nmhillusion.jParrotDataSelectorApp.model.DatasourceModel;
import tech.nmhillusion.jParrotDataSelectorApp.model.DoubleClickMouseListener;
import tech.nmhillusion.jParrotDataSelectorApp.state.ExecutionState;
import tech.nmhillusion.jParrotDataSelectorApp.state.LoadingStateListener;
import tech.nmhillusion.neon_di.annotation.Inject;
import tech.nmhillusion.neon_di.annotation.Neon;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
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
    private final JButton btnChangeDatasource = new JButton("???");
    private LoadingStateListener loadingStateListener;

    public HeaderPanel(@Inject ExecutionState executionState, @Inject DatabaseLoader databaseLoader) throws IOException {
        this.executionState = executionState;
        this.databaseLoader = databaseLoader;

        setLayout(new GridBagLayout());

        initComponents();

        executionState.addListener(this::onApplyDataSource);
    }

    public HeaderPanel setLoadingStateListener(LoadingStateListener loadingStateListener) {
        this.loadingStateListener = loadingStateListener;
        return this;
    }

    private void initComponents() throws IOException {
        final GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);

        add(
                new JLabel("Data Source:"), gridBagConstraints
        );

        btnChangeDatasource.setIcon(new ImageIcon(
                ViewHelper.getIconForButton("icon/database-icon.png", 12, 12)
        ));
        btnChangeDatasource.setIconTextGap(5);
        btnChangeDatasource.setBorder(
                BorderFactory.createEtchedBorder(
                        EtchedBorder.LOWERED
                )
        );
        btnChangeDatasource.setPreferredSize(
                new Dimension(200, 25)
        );
        btnChangeDatasource.addActionListener(this::onClickChangeDatasource);
        btnChangeDatasource.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        add(
                btnChangeDatasource
                , gridBagConstraints
        );
    }

    private void setFixedSizeOfButton(JButton btn, int parentWidth) {
        final Dimension dimension = new Dimension(parentWidth, 20);
        btn.setPreferredSize(dimension);
        btn.setMaximumSize(dimension);
        btn.setMinimumSize(dimension);
    }

    private void onApplyDataSource() {
        final DatasourceModel datasourceModel = executionState.getDatasourceModel();
        if (null == datasourceModel) {
            btnChangeDatasource.setText("???");
        } else {
            btnChangeDatasource.setText(datasourceModel.getDataSourceName());
        }
    }

    private void onClickChangeDatasource(ActionEvent evt) {
        try {
            var parentFrame = ViewHelper.getFrameAncestor(this);
            final JDialog jDialog = new JDialog(
                    parentFrame
                    , "Select data source"
                    , true
            );
            jDialog.setSize(300, 450);
            jDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

            final JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

            final JScrollPane scrollPane = new JScrollPane(
                    contentPanel
            );
            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

            databaseLoader.loadDatasourceModels()
                    .stream()
                    .map(it -> {
                        final JButton btn = new JButton(it.getDataSourceName());
                        setFixedSizeOfButton(btn, jDialog.getWidth());
                        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                        btn.addMouseListener(new DoubleClickMouseListener() {
                            @Override
                            public void onDoubleClick(MouseEvent e) {
                                throwIfFailConnectionDS(it);

                                executionState.setDatasourceModel(it);
                                jDialog.dispose();
                            }
                        });
                        return btn;
                    })
                    .forEach(contentPanel::add);


            final Container contentPane = jDialog.getContentPane();
            contentPane.setLayout(
                    new CardLayout()
            );

            final JPanel comp = new JPanel();
            comp.setBorder(
                    BorderFactory.createEmptyBorder(
                            5, 5, 5, 5
                    )
            );
            comp.add(
                    scrollPane
            );
            contentPane.add(
                    comp
            );

            jDialog.revalidate();
            jDialog.setLocationRelativeTo(parentFrame);
            jDialog.setResizable(false);
            jDialog.setVisible(true);
        } catch (Throwable ex) {
            JOptionPane.showMessageDialog(
                    evt.getSource() instanceof JButton ? (JButton) evt.getSource() : this
                    , "Error when selecting data source: " + ex.getMessage()
                    , "Error"
                    , JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void throwIfFailConnectionDS(DatasourceModel it) {
        loadingStateListener.onLoadingStateChange(true);

        final SwingWorker<Boolean, Throwable> swingWorker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                databaseLoader.checkConnection(it, ex -> {
                    executionState.setDatasourceModel(null);
                    publish(ex);
                });

                return true;
            }

            @Override
            protected void process(List<Throwable> chunks) {
                if (null != chunks && !chunks.isEmpty()) {
                    JOptionPane.showMessageDialog(
                            btnChangeDatasource
                            , chunks.getFirst()
                            , "Error"
                            , JOptionPane.ERROR_MESSAGE
                    );
                }
            }

            @Override
            protected void done() {
                loadingStateListener.onLoadingStateChange(false);
            }
        };

        executionState.setCurrentBackgroundWorker(swingWorker);
        swingWorker.execute();
    }

}
