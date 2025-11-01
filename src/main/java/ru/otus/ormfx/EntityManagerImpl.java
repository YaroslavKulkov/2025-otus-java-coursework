package ru.otus.ormfx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.core.repository.DataTemplate;
import ru.otus.core.repository.executor.DbExecutor;
import ru.otus.core.sessionmanager.TransactionRunner;
import ru.otus.crm.model.Client;
import ru.otus.crm.model.Manager;
import ru.otus.crm.model.Student;
import ru.otus.crm.service.DbServiceClientImpl;
import ru.otus.jdbc.annotations.Entity;
import ru.otus.jdbc.mapper.DataTemplateJdbc;
import ru.otus.jdbc.mapper.EntityClassMetaData;
import ru.otus.jdbc.mapper.EntityProcessingException;
import ru.otus.jdbc.mapper.EntitySQLMetaData;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class EntityManagerImpl<T>  {
    private final TransactionRunner transactionRunner;
    private final DbExecutor dbExecutor;
    private final EntityClassMetaData<T> entityMetaData;
    private final EntitySQLMetaData entitySQLMetaData;
    private final DataTemplate<T> dataTemplate;

    public EntityManagerImpl(TransactionRunner transactionRunner, DbExecutor dbExecutor, EntityClassMetaData<T> entityMetaData, EntitySQLMetaData entitySQLMetaData, DataTemplate<T> dataTemplate) {
        this.transactionRunner = transactionRunner;
        this.dbExecutor = dbExecutor;
        this.entityMetaData = entityMetaData;
        this.entitySQLMetaData = entitySQLMetaData;
        this.dataTemplate = dataTemplate;
    }

    public <T> ObservableList<T> findAll(Class<T> entityClass) {
        DataTemplate<T> dataTemplate = new DataTemplateJdbc<T>(dbExecutor, entitySQLMetaData, (EntityClassMetaData<T>) entityMetaData);;

        ObservableList<T> resultList = FXCollections.observableArrayList();

        // Загружаем данные из БД
        List<T> data = transactionRunner.doInTransaction(connection ->
                dataTemplate.findAll(connection)
        );
        resultList.addAll(data);
        return resultList;
    }

    public <T> ObservableList<T> createBoundList(Class<T> entityClass, TableView<T> tableView) {
        ObservableList<T> observableList = findAll(entityClass);

        if (tableView != null) {
            tableView.setItems(observableList);
        }

        return observableList;
    }

    public T save(T entity) {
        return transactionRunner.doInTransaction(connection -> {
            try {
                Field idField = entityMetaData.getIdField();
                idField.setAccessible(true);
                Object id = idField.get(entity);

                if (id == null) {
                    long newId = dataTemplate.insert(connection, entity);
                    idField.set(entity, newId);
                    return entity;
                } else {
                    dataTemplate.update(connection, entity);
                    return entity;
                }
            } catch (IllegalAccessException e) {
                throw new EntityProcessingException("Failed to access ID field");
            }
        });
    }

    public void delete(T entity) {
        transactionRunner.doInTransaction(connection -> {
            try {
                Field idField = entityMetaData.getIdField();
                idField.setAccessible(true);
                Object id = idField.get(entity);

                if (id != null) {
                    //dataTemplate.delete(connection, (T) id);
                    dataTemplate.delete(connection, entity);
                } else {
                    throw new RuntimeException("Cannot delete entity with null ID");
                }
                return null;
            } catch (IllegalAccessException e) {
                throw new EntityProcessingException("Failed to access ID field");
            }
        });
    }



}
