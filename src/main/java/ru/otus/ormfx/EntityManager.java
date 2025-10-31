package ru.otus.ormfx;

import javafx.collections.ObservableList;

public interface EntityManager<T> {
    <T> ObservableList<T> findAll();

    <T> void save(T entity);

    <T> void update(T entity);

    <T> void delete(T entity);

    <T> T findById(Class<T> entityClass, Object id);
}
