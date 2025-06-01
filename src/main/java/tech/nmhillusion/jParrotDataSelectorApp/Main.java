package tech.nmhillusion.jParrotDataSelectorApp;

import tech.nmhillusion.jParrotDataSelectorApp.helper.PathHelper;
import tech.nmhillusion.jParrotDataSelectorApp.helper.ViewHelper;
import tech.nmhillusion.jParrotDataSelectorApp.loader.DatabaseLoader;
import tech.nmhillusion.jParrotDataSelectorApp.screen.MainFrame;
import tech.nmhillusion.jParrotDataSelectorApp.screen.dialog.OptionDialogPane;
import tech.nmhillusion.jParrotDataSelectorApp.state.ExecutionState;
import tech.nmhillusion.n2mix.helper.YamlReader;
import tech.nmhillusion.n2mix.validator.StringValidator;
import tech.nmhillusion.neon_di.NeonEngine;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static tech.nmhillusion.n2mix.helper.log.LogHelper.getLogger;

/**
 * created by: nmhillusion
 * <p>
 * created date: 2024-11-26
 */

public class Main {
    private static final NeonEngine NEON_ENGINE = new NeonEngine();
    private static String APP_DISPLAY_NAME = "jParrotDataSelectorApp";

    public static void main(String[] args) throws IOException {
        getLogger(Main.class).info("Starting jParrotDataSelectorApp");

//        checkSystem();
        fillAppProperty();

        try {
            setLookAndFeelUI();
//            throwIfUnavailableRequiredPaths();
        } catch (Throwable ex) {
            JOptionPane.showMessageDialog(
                    null
                    , new OptionDialogPane("Error when init program [%s]: %s".formatted(ex.getClass().getSimpleName(), ex.getMessage()))
                    , "Error"
                    , JOptionPane.ERROR_MESSAGE
            );
            exitAppOnError(ex);
        }

        SwingUtilities.invokeLater(() -> {
            try {
                NEON_ENGINE.run(Main.class);

                final ExecutionState executionState = NEON_ENGINE.makeSureObtainNeon(ExecutionState.class);
                initForExecutionState(executionState);
                throwIfInvalidDbConfig();
                makeGUI(executionState);
            } catch (Exception ex) {
                exitAppOnError(ex);
            }
        });
    }

    private static <T> T getAppInfoProperty(String configKey, Class<T> class2Cast) throws IOException {
        final Path appInfoPath = PathHelper.getPathOfResource("config/app-info.yml");
        try (final InputStream fis = Files.newInputStream(appInfoPath)) {
            return new YamlReader(fis).getProperty(configKey, class2Cast);
        }
    }

    private static void fillAppProperty() throws IOException {
        final String appName = getAppInfoProperty("info.name", String.class);
        final String appVersion = getAppInfoProperty("info.version", String.class);

        APP_DISPLAY_NAME = appName + (
                StringValidator.isBlank(appVersion) ? "" : " v" + appVersion
        )
        ;
    }

    private static void checkSystem() {
        System.getenv()
                .forEach((k, v) -> getLogger(Main.class).info("env {}: {}", k, v));

        System.getProperties()
                .forEach((k, v) -> getLogger(Main.class).info("prop {}: {}", k, v));
    }

    private static void initForExecutionState(ExecutionState executionState) {
        executionState.setDatasourceModel(null);
    }

    private static void throwIfInvalidDbConfig() throws IOException {
        final DatabaseLoader databaseLoader = NEON_ENGINE.makeSureObtainNeon(DatabaseLoader.class);
        try {
            databaseLoader.loadDatasourceModels();
        } catch (Throwable ex) {
            getLogger(Main.class).error(ex);
            JOptionPane.showMessageDialog(
                    null
                    , new OptionDialogPane(
                            "Error when init program [%s]: %s".formatted(ex.getClass().getSimpleName(), ex.getMessage())
                    )
                    , "Error"
                    , JOptionPane.ERROR_MESSAGE
            );
            throw ex;
        }
    }

    private static void setLookAndFeelUI() {
        try {
            final String lookAndFeelClassName = !ViewHelper.isMacOS()
                    ? UIManager.getSystemLookAndFeelClassName()
                    : "javax.swing.plaf.nimbus.NimbusLookAndFeel";

            UIManager.setLookAndFeel(
                    lookAndFeelClassName
            );
        } catch (Exception e) {
            getLogger(Main.class).error(e);
        }
    }

    private static void makeGUI(ExecutionState executionState) throws IOException {
        final JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 860);
        frame.setTitle(APP_DISPLAY_NAME);
        frame.setLocationByPlatform(true);
        setIconForApp(frame);

        executionState.addListener(() -> {
            if (null == executionState.getDatasourceModel()) {
                frame.setTitle(APP_DISPLAY_NAME);
                return;
            }

            final String dataSourceName = executionState.getDatasourceModel()
                    .getDataSourceName();

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
