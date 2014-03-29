package org.guess880.desktop_lucene;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.LogManager;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class DesktopLucene extends Application {

    public static final ExecutorService SERVICE = Executors.newSingleThreadExecutor();

    @Override
    public void start(final Stage stage) throws IOException {
        try (final InputStream logConfig = getClass().getResourceAsStream("logging.properties")) {
            LogManager.getLogManager().readConfiguration(logConfig);
        }
        final AnchorPane root = (AnchorPane) FXMLLoader.load(getClass().getResource("Main.fxml"), Resources.getResourceBundle());
        final Scene scene = new Scene(root);
//        scene.getStylesheets().add(getClass().getResource("win7glass.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Desktop Lucene");
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(final WindowEvent event) {
                SERVICE.shutdown();
            }
        });
        stage.show();
    }

    public static final void main(final String[] args) {
        launch(args);
    }

}
