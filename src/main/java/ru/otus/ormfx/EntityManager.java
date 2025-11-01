package ru.otus.ormfx;

import javafx.collections.ObservableList;
import javafx.scene.control.TableView;

public interface EntityManager<T> {
    ObservableList<T> createBoundList(TableView<T> tableView);

    //Map<Field, String> getFieldToColumnNameMap();

    ObservableList<T> findAll();

    T save(T entity);

    void delete(T entity);
}
