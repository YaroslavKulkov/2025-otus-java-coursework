package ru.otus.ormfx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.core.repository.DataTemplate;
import ru.otus.core.sessionmanager.TransactionRunner;
import ru.otus.crm.model.Client;
import ru.otus.crm.model.Manager;
import ru.otus.crm.model.Student;
import ru.otus.crm.service.DbServiceClientImpl;
import ru.otus.jdbc.annotations.Entity;
import ru.otus.jdbc.mapper.EntityClassMetaData;

import java.util.List;

public class EntityManagerImpl<T> implements EntityManager<T> {
    private static final Logger log = LoggerFactory.getLogger(EntityManagerImpl.class);

    private final DataTemplate<T> dataTemplate;
    private final TransactionRunner transactionRunner;
    private final EntityClassMetaData<T> entityClassMetaData;

    public EntityManagerImpl(TransactionRunner transactionRunner, DataTemplate<T> dataTemplate, EntityClassMetaData<T> entityClassMetaData) {
        this.transactionRunner = transactionRunner;
        this.dataTemplate = dataTemplate;
        this.entityClassMetaData = entityClassMetaData;
    }

    @Override
    public <T> ObservableList<T> findAll() {
        List<T> list = (List<T>) transactionRunner.doInTransaction(connection -> {
            var clientList = dataTemplate.findAll(connection);
            log.info("clientList:{}", clientList);
            return clientList;
        });
        return FXCollections.observableArrayList(list);
    }

    @Override
    public <T> T save(T entity) {
        return transactionRunner.doInTransaction(connection -> {
            if (entity.getId() == null) {
                var clientId = dataTemplate.insert(connection, entity);
                var createdClient = new Client(clientId, entity.getName());
                log.info("created client: {}", createdClient);
                return createdClient;
            }
            dataTemplate.update(connection, entity);
            log.info("updated client: {}", entity);
            return entity;
        });
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
