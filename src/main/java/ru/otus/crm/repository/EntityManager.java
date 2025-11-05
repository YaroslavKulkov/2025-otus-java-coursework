package ru.otus.crm.repository;

import javafx.collections.ObservableList;
import javafx.scene.control.TableView;

/** Репозиторий для работы с сущностями.
 * Создает ObservableList<T> для компонента TableView */
public interface EntityManager<T> {
    ObservableList<T> createBoundList(TableView<T> tableView);

    //Map<Field, String> getFieldToColumnNameMap();

    ObservableList<T> findAll();

    T save(T entity);

    void delete(T entity);
}
