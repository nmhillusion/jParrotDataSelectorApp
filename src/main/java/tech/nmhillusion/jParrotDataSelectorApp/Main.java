package tech.nmhillusion.jParrotDataSelectorApp;

import tech.nmhillusion.jParrotDataSelectorApp.helper.PathHelper;
import tech.nmhillusion.jParrotDataSelectorApp.screen.MainFrame;
import tech.nmhillusion.jParrotDataSelectorApp.state.ExecutionState;
import tech.nmhillusion.neon_di.NeonEngine;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;

import static tech.nmhillusion.n2mix.helper.log.LogHelper.getLogger;

/**
 * created by: nmhillusion
 * <p>
 * created date: 2024-11-26
 */

public class Main {
    private static final String APP_NAME = "jParrotDataSelectorApp";
    private static final NeonEngine NEON_ENGINE = new NeonEngine();

    public static void main(String[] args) throws URISyntaxException {
        getLogger(Main.class).info("Starting jParrotDataSelectorApp");

        try {
            setLookAndFeelUI();
//            throwIfUnavailableRequiredPaths();
        } catch (Throwable ex) {
            JOptionPane.showMessageDialog(
                    null
                    , "Error when init program [%s]: %s".formatted(ex.getClass().getSimpleName(), ex.getMessage())
                    , "Error"
                    , JOptionPane.ERROR_MESSAGE
            );
            exitAppOnError(ex);
        }

        SwingUtilities.invokeLater(() -> {
            try {
                NEON_ENGINE.run(Main.class);

                final ExecutionState executionState = NEON_ENGINE.makeSureObtainNeon(ExecutionState.class);
                makeGUI(executionState);
            } catch (Exception ex) {
                exitAppOnError(ex);
            }
        });
    }

    private static void setLookAndFeelUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            getLogger(Main.class).error(e);
        }
    }

    private static void makeGUI(ExecutionState executionState) throws IOException {
        final JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 860);
        frame.setTitle(APP_NAME);
        frame.setLocationByPlatform(true);
        setIconForApp(frame);

        executionState.addListener(() -> {
            if (null == executionState.getDatasourceModel()) {
                frame.setTitle(APP_NAME);
                return;
            }

            final String dataSourceName = executionState.getDatasourceModel()
                    .getDataSourceName();
            frame.setTitle(dataSourceName + " - " + APP_NAME);
            frame.revalidate();
            frame.repaint();
        });

        frame.setContentPane(
                NEON_ENGINE.makeSureObtainNeon(MainFrame.class)
        );

//        frame.pack();
        frame.revalidate();
        frame.setFocusable(true);
        frame.requestFocusInWindow();
        frame.setVisible(true);
    }

    private static void setIconForApp(JFrame frame) throws IOException {
        try (final InputStream icStream = Files.newInputStream(PathHelper.getPathOfResource("icon/app-icon.png"))) {
            if (null == icStream) {
                throw new IOException("App icon not found");
            }

            frame.setIconImage(
                    ImageIO.read(icStream)
            );
        }
    }

    private static void exitAppOnError(Throwable ex) {
        getLogger(Main.class).error(ex);
        System.exit(-1);
    }
}
