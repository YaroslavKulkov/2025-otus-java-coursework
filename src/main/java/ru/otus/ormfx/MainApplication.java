package ru.otus.ormfx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.crm.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

public class MainApplication extends Application {
    private static final Logger log = LoggerFactory.getLogger(MainApplication.class);

    private static DriverManagerDataSource dataSource;

    public static DriverManagerDataSource getDataSource() {
        return dataSource;
    }

    @Override
    public void init() throws Exception {
        super.init();
        try (InputStream input = this.getClass().getClassLoader()
                .getResourceAsStream("database.properties")) {
            Properties props = new Properties();
            props.load(input);
            log.info("Properties loaded. Connecting to database {}", props.getProperty("db.url"));
            this.dataSource = new DriverManagerDataSource(props.getProperty("db.url"), props.getProperty("db.username"), props.getProperty("db.password"));

        } catch (IOException e) {
            log.error("Error while loading properties", e);
            Platform.exit();
        }
        flywayMigrations(dataSource);
    }

    @Override
    public void start(Stage stage) throws IOException {
        StageManager.setPrimaryStage(stage);
        ResourceBundle bundle = ResourceBundle.getBundle("main", Locale.getDefault());
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("main-view.fxml"), bundle);
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        stage.setTitle("Demo Application");
        stage.setScene(scene);
        log.info("Main application started with locale {}", Locale.getDefault().toLanguageTag());
        stage.show();
    }

    private void flywayMigrations(DataSource dataSource) {
        log.info("db migration started...");
        var flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:/migration")
                .load();
        flyway.migrate();
        log.info("db migration finished.");
        log.info("***");
    }
}
