package ru.otus.jdbc.mapper;

import ru.otus.core.repository.DataTemplate;
import ru.otus.core.repository.DataTemplateException;
import ru.otus.core.repository.executor.DbExecutor;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Сохратяет объект в базу, читает объект из базы
 */
@SuppressWarnings("java:S1068")
public class DataTemplateJdbc<T> implements DataTemplate<T> {

    private final DbExecutor dbExecutor;
    private final EntitySQLMetaData entitySQLMetaData;
    private final EntityClassMetaData<T> entityClassMetaData;

    public DataTemplateJdbc(
            DbExecutor dbExecutor, EntitySQLMetaData entitySQLMetaData, EntityClassMetaData<T> entityClassMetaData) {
        this.dbExecutor = dbExecutor;
        this.entitySQLMetaData = entitySQLMetaData;
        this.entityClassMetaData = entityClassMetaData;
    }

    @Override
    public Optional<T> findById(Connection connection, long id) {
        return dbExecutor.executeSelect(
                connection, entitySQLMetaData.getSelectByIdSql(), List.of(id), rs -> createListOfEntityFromRS(rs).getFirst());
    }

    @Override
    public List<T> findAll(Connection connection) {
        var resultOfSelect = dbExecutor.executeSelect(
                connection, entitySQLMetaData.getSelectAllSql(), List.of(), this::createListOfEntityFromRS);
        return resultOfSelect.orElse(new ArrayList<>());
    }

    @Override
    public long insert(Connection connection, T entity) {
        var fields = entityClassMetaData.getFieldsWithoutId();
        var listOfParams = new ArrayList<>(fields.size());
        try {
            for (Field field : fields) {
                field.setAccessible(true);
                listOfParams.add(field.get(entity));
            }
        } catch (IllegalAccessException e) {
            throw new DataTemplateException(e);
        }
        return dbExecutor.executeStatement(connection, entitySQLMetaData.getInsertSql(), listOfParams);
    }

    @Override
    public void update(Connection connection, T entity) {
        var fields = entityClassMetaData.getFieldsWithoutId();
        var listOfParams = new ArrayList<>(fields.size() + 1);
        try {
            for (Field field : fields) {
                field.setAccessible(true);
                listOfParams.add(field.get(entity));
            }
            var idField = entityClassMetaData.getIdField();
            idField.setAccessible(true);
            listOfParams.add(idField.get(entity));
        } catch (IllegalAccessException e) {
            throw new DataTemplateException(e);
        }

        dbExecutor.executeStatement(connection, entitySQLMetaData.getUpdateSql(), listOfParams);
    }

    @Override
    public void delete(Connection connection, T entity) {
        var idField = entityClassMetaData.getIdField();
        idField.setAccessible(true);
        try {
            dbExecutor.executeStatement(connection, entitySQLMetaData.getDeleteSql(), List.of(idField.get(entity)));
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error while deleting entity");
        }
    }

/*    private T createEntityFromRS(ResultSet rs) {
        try {
            if (rs.next()) {
                return createEntityFromCurrentRow(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new DataTemplateException(e);
        }
    }*/

    private List<T> createListOfEntityFromRS(ResultSet rs) {
        try {
            List<T> entities = new ArrayList<>();
            while (rs.next()) {
                entities.add(createEntityFromCurrentRow(rs));
            }
            return entities;
        } catch (SQLException e) {
            throw new DataTemplateException(e);
        }
    }

    private T createEntityFromCurrentRow(ResultSet rs) {
        try {
            T entity = entityClassMetaData.getConstructor().newInstance();
            Map<Field, String> fieldToColumnMap = entityClassMetaData.getFieldToColumnNameMap();

            for (Field field : entityClassMetaData.getAllFields()) {
                field.setAccessible(true);
                String columnName = fieldToColumnMap.get(field);
                field.set(entity, rs.getObject(columnName));
            }
            return entity;
        } catch (SQLException | ReflectiveOperationException e) {
            throw new DataTemplateException(e);
        }
    }
}
