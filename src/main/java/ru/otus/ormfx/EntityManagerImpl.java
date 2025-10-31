package ru.otus.ormfx;

import javafx.collections.ObservableList;
import ru.otus.core.repository.DataTemplate;
import ru.otus.core.sessionmanager.TransactionRunner;
import ru.otus.crm.model.Manager;
import ru.otus.crm.model.Student;
import ru.otus.jdbc.annotations.Entity;

public class EntityManagerImpl<T> implements EntityManager<T> {

    public EntityManagerImpl(TransactionRunner transactionRunner, DataTemplate<T> dataTemplateTemplate) {
    }

    @Override
    public <T> ObservableList<T> findAll() {
        return null;
    }

    @Override
    public <T> void save(T entity) {

    }

    @Override
    public <T> void update(T entity) {

    }

    @Override
    public <T> void delete(T entity) {

    }

    @Override
    public <T> T findById(Class<T> entityClass, Object id) {
        return null;
    }
}
