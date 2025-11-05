package ru.otus.ui.manager;

import javafx.stage.Stage;

/** Хранит ссылку на главное окно приложения. */
public class StageManager {
    private static Stage primaryStage;

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void setPrimaryStage(Stage primaryStage) {
        StageManager.primaryStage = primaryStage;
    }
}
