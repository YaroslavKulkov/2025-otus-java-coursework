package ru.otus.jdbc.mapper;

import ru.otus.jdbc.annotations.Id;

import java.lang.reflect.Field;
import java.util.stream.Collectors;

public class EntitySQLMetaDataImpl implements EntitySQLMetaData {
    private final EntityClassMetaData<?> entityMetaData;
    private final String tableName;
    private final String idFieldName;

    private String selectAllSql;
    private String selectByIdSql;
    private String insertSql;
    private String updateSql;

    public EntitySQLMetaDataImpl(EntityClassMetaData<?> entityMetaData) {
        this.entityMetaData = entityMetaData;
        this.tableName = entityMetaData.getName().toLowerCase();
        idFieldName = findIdFieldName();
        initializeSQLQuery();
    }

    @Override
    public String getSelectAllSql() {
        return selectAllSql;
    }

    @Override
    public String getSelectByIdSql() {
        return selectByIdSql;
    }

    @Override
    public String getInsertSql() {
        return insertSql;
    }

    @Override
    public String getUpdateSql() {
        return updateSql;
    }

    private void initializeSQLQuery() {
        initSelectAllSql();
        initSelectByIdSql();
        initInsertSql();
        initUpdateSql();
    }

    private void initUpdateSql() {
        updateSql = "UPDATE " + tableName + " SET " + generateUpdateClause() + " WHERE " + idFieldName + " = ?";
    }

    private void initInsertSql() {
        insertSql = "INSERT INTO " + tableName + "(" + generateListOfFieldsName() + ") VALUES("
                + generatePlaceHolders(entityMetaData.getFieldsWithoutId().size()) + ")";
    }

    private void initSelectByIdSql() {
        selectByIdSql = "SELECT " + generateListOfFields() + " FROM " + tableName + " WHERE " + idFieldName + " = ?";
    }

    private void initSelectAllSql() {
        selectAllSql = "SELECT * FROM " + tableName;
    }

    private String findIdFieldName() {
        return entityMetaData.getAllFields().stream()
                .filter(field -> field.isAnnotationPresent(Id.class))
                .map(Field::getName)
                .findFirst()
                .orElseThrow(() -> new EntityProcessingException("Entity class must have a field annotated with @Id"));
    }

    private String generateListOfFieldsName() {
        return entityMetaData.getFieldsWithoutId().stream().map(Field::getName).collect(Collectors.joining(", "));
    }

    private String generatePlaceHolders(int size) {
        return String.join(", ", "?".repeat(size).split(""));
    }

    private String generateUpdateClause() {
        return entityMetaData.getFieldsWithoutId().stream()
                .map(field -> field.getName() + " = ?")
                .collect(Collectors.joining(", "));
    }

    private String generateListOfFields() {
        var fields = entityMetaData.getAllFields();
        return fields.stream().map(Field::getName).collect(Collectors.joining(", "));
    }
}
