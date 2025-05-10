package tech.nmhillusion.jParrotDataSelectorApp;

import org.hibernate.SessionFactory;
import tech.nmhillusion.jParrotDataSelectorApp.loader.DatabaseLoader;
import tech.nmhillusion.jParrotDataSelectorApp.screen.MainFrame;
import tech.nmhillusion.n2mix.helper.database.query.DatabaseExecutor;
import tech.nmhillusion.n2mix.helper.database.query.DatabaseHelper;
import tech.nmhillusion.n2mix.helper.log.LogHelper;
import tech.nmhillusion.n2mix.type.ChainMap;

import javax.sql.DataSource;
import javax.swing.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.ResultSet;
import java.sql.Timestamp;

import static tech.nmhillusion.n2mix.helper.log.LogHelper.getLogger;

/**
 * created by: nmhillusion
 * <p>
 * created date: 2024-11-26
 */

public class Main {
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
                makeGUI();

//                testDb();
            } catch (IOException ex) {
                exitAppOnError(ex);
            }
        });
    }

    private static void testDb() {
        DatabaseLoader dbc = new DatabaseLoader();
        try (final SessionFactory sessionFactory = dbc.generateDbSession()) {
            final DataSource dataSource = dbc.generateDataSource();
            final DatabaseHelper databaseHelper = new DatabaseHelper(dataSource, sessionFactory);
            final DatabaseExecutor executor = databaseHelper.getExecutor();

            executor.doWork(conn -> {
                conn.doPreparedStatement(" select * from t_user ", preparedStatement_ -> {
                    final ResultSet resultSet = preparedStatement_.executeQuery();

                    while (resultSet.next()) {
                        final String username = resultSet.getString("username");
                        final String fullName = resultSet.getString("full_name");
                        final boolean enabled = resultSet.getBoolean("enabled");
                        final Timestamp createdDate = resultSet.getTimestamp("created_date");
                        LogHelper.getLogger(Main.class).info(
                                new ChainMap<String, Object>()
                                        .chainPut("username", username)
                                        .chainPut("fullName", fullName)
                                        .chainPut("enabled", enabled)
                                        .chainPut("createdDate", createdDate)
                        );
                    }
                });
            });
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static void setLookAndFeelUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            getLogger(Main.class).error(e);
        }
    }

    private static void makeGUI() throws IOException {
        final JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 860);
        frame.setTitle("jParrotDataSelectorApp");
        frame.setLocationByPlatform(true);
//        setIconForApp(frame);

        frame.setContentPane(
                new MainFrame()
        );

//        frame.pack();
        frame.revalidate();
        frame.setFocusable(true);
        frame.requestFocusInWindow();
        frame.setVisible(true);
    }

//    private static void setIconForApp(JFrame frame) throws IOException {
//        try (final InputStream icStream = Files.newInputStream(PathHelper.getPathOfResource("icon/app-icon.png"))) {
//            if (null == icStream) {
//                throw new IOException("App icon not found");
//            }
//
//            frame.setIconImage(
//                    ImageIO.read(icStream)
//            );
//        }
//    }

    private static void exitAppOnError(Throwable ex) {
        getLogger(Main.class).error(ex);
        System.exit(-1);
    }
}
