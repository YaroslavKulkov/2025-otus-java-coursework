package ru.otus.ormfx;

import javafx.scene.control.TableView;

public class TableViewBuilder {
    public static <T> TableView<T> createTableView(Class<T> entityClass) {
        // Автоматически:
        // 1. Создает TableView<T>
        // 2. Создает TableColumn для каждого поля с @Column
        // 3. Настраивает cellValueFactory на основе имен полей
        // 4. Возвращает готовый TableView
        return null;
    }
}
