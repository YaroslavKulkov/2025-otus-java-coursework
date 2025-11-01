package ru.otus.jdbc.mapper;

import ru.otus.jdbc.annotations.Id;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.stream.Collectors;

public class EntitySQLMetaDataImpl implements EntitySQLMetaData {
    private final EntityClassMetaData<?> entityMetaData;
    private final String tableName;
    private final String idFieldName;

    private final String selectAllSql;
    private final String selectByIdSql;
    private final String insertSql;
    private final String updateSql;
    private final String deleteSql;

    public EntitySQLMetaDataImpl(EntityClassMetaData<?> entityMetaData) {
        this.entityMetaData = entityMetaData;
        this.tableName = entityMetaData.getName().toLowerCase();
        idFieldName = findIdFieldName();
        selectAllSql = initSelectAllSql();
        selectByIdSql = initSelectByIdSql();
        insertSql = initInsertSql();
        updateSql = initUpdateSql();
        deleteSql = initDeleteSql();
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

    @Override
    public String getDeleteSql() {
        return deleteSql;
    }

    private String initDeleteSql() {
        return "DELETE FROM " + tableName + " WHERE " + idFieldName + " = ?";
    }

    private String initUpdateSql() {
        return "UPDATE " + tableName + " SET " + generateUpdateClause() + " WHERE " + idFieldName + " = ?";
    }

    private String initInsertSql() {
        return "INSERT INTO " + tableName + "(" + generateListOfFieldsName() + ") VALUES("
                + generatePlaceHolders(entityMetaData.getFieldsWithoutId().size()) + ")";
    }

    private String initSelectByIdSql() {
        return "SELECT " + generateListOfFields() + " FROM " + tableName + " WHERE " + idFieldName + " = ?";
    }

    private String initSelectAllSql() {
        return "SELECT * FROM " + tableName;
    }

    private String findIdFieldName() {
        return entityMetaData.getAllFields().stream()
                .filter(field -> field.isAnnotationPresent(Id.class))
                .map(Field::getName)
                .findFirst()
                .orElseThrow(() -> new EntityProcessingException("Entity class must have a field annotated with @Id"));
    }

    private String generateListOfFieldsName() {
        Map<Field, String> fieldToColumnMap = entityMetaData.getFieldToColumnNameMap();
        return entityMetaData.getFieldsWithoutId().stream()
                .map(fieldToColumnMap::get)
                .collect(Collectors.joining(", "));
    }

    private String generatePlaceHolders(int size) {
        return String.join(", ", "?".repeat(size).split(""));
    }

    private String generateUpdateClause() {
        Map<Field, String> fieldToColumnMap = entityMetaData.getFieldToColumnNameMap();
        return entityMetaData.getFieldsWithoutId().stream()
                .map(field -> fieldToColumnMap.get(field) + " = ?")
                .collect(Collectors.joining(", "));
    }

    private String generateListOfFields() {
        var fields = entityMetaData.getAllFields();
        return fields.stream()
                .map(field -> entityMetaData.getFieldToColumnNameMap().get(field))
                .collect(Collectors.joining(", "));
    }
}
