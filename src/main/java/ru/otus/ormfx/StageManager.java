package ru.otus.ormfx;

import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;

public class StageManager {
    private static Stage primaryStage;

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void setPrimaryStage(Stage primaryStage) {
        StageManager.primaryStage = primaryStage;
    }
}
