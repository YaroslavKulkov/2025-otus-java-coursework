package ru.otus.crm.repository;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import ru.otus.core.repository.DataTemplate;
import ru.otus.core.repository.executor.DbExecutor;
import ru.otus.core.sessionmanager.TransactionRunner;
import ru.otus.jdbc.mapper.DataTemplateJdbc;
import ru.otus.jdbc.mapper.EntityClassMetaData;
import ru.otus.jdbc.mapper.EntityProcessingException;
import ru.otus.jdbc.mapper.EntitySQLMetaData;

public class EntityManagerImpl<T>  implements EntityManager<T> {
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

    public ObservableList<T> findAll() {
        var dataTemplate = new DataTemplateJdbc<T>(dbExecutor, entitySQLMetaData, entityMetaData);

        ObservableList<T> resultList = FXCollections.observableArrayList();

        var data = transactionRunner.doInTransaction(dataTemplate::findAll);
        resultList.addAll(data);
        return resultList;
    }

    public ObservableList<T> createBoundList(TableView<T> tableView) {
        var observableList = findAll();
        if (tableView != null) {
            tableView.setItems(observableList);
        }
        return observableList;
    }

    public T save(T entity) {
        return transactionRunner.doInTransaction(connection -> {
            try {
                var idField = entityMetaData.getIdField();
                idField.setAccessible(true);
                Object id = idField.get(entity);

                if (id == null) {
                    var newId = dataTemplate.insert(connection, entity);
                    idField.set(entity, newId);
                } else {
                    dataTemplate.update(connection, entity);
                }
                return entity;
            } catch (IllegalAccessException e) {
                throw new EntityProcessingException("Failed to access ID field");
            }
        });
    }

    public void delete(T entity) {
        transactionRunner.doInTransaction(connection -> {
            try {
                var idField = entityMetaData.getIdField();
                idField.setAccessible(true);
                Object id = idField.get(entity);

                if (id != null) {
                    dataTemplate.delete(connection, entity);
                } else {
                    throw new EntityProcessingException("Cannot delete entity with null ID");
                }
                return null;
            } catch (IllegalAccessException e) {
                throw new EntityProcessingException("Failed to access ID field");
            }
        });
    }
}
