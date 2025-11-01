package ru.otus.jdbc.mapper;

import javafx.scene.control.TableColumn;
import ru.otus.jdbc.annotations.Column;
import ru.otus.jdbc.annotations.Entity;
import ru.otus.jdbc.annotations.Id;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class EntityClassMetaDataImpl<T> implements EntityClassMetaData<T> {
    private final Class<T> entityClass;
    private final Constructor<T> constructor;
    private final Field idField;
    private final List<Field> allFields;
    private final List<Field> fieldsWithoutId;
    private final String tableName;
    private final Map<Field, String> fieldToColumnNameMap = new HashMap<>();

    public EntityClassMetaDataImpl(Class<T> entityClass) {
        this.entityClass = Objects.requireNonNull(entityClass, "Entity class cannot be null");
        this.tableName = extractTableName();
        this.allFields = List.of(entityClass.getDeclaredFields());
        initFieldToColumnNameMapping();
        this.constructor = initConstructorName();
        this.idField = initIdField();
        this.fieldsWithoutId = initFieldsWithoutId();
    }

    @Override
    public Map<Field, String> getFieldToColumnNameMap() {
        return fieldToColumnNameMap;
    }

    @Override
    public String getName() {
        return tableName;
    }

    @Override
    public Constructor<T> getConstructor() {
        return constructor;
    }

    @Override
    public Field getIdField() {
        return idField;
    }

    @Override
    public List<Field> getAllFields() {
        return allFields;
    }

    @Override
    public List<Field> getFieldsWithoutId() {
        return fieldsWithoutId;
    }

    @Override
    public Field getFieldForColumn(String columnId) {
        return allFields.stream()
                .filter(field -> isColumnField(field))
                .filter(field -> columnId.equals(getColumnId(field)))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "No field found for column id: " + columnId +
                                " in entity: " + entityClass.getName()
                ));
    }

    @Override
    public void validateColumns(List<TableColumn<T, ?>> columns) {
        for (TableColumn<T, ?> column : columns) {
            var columnId = column.getId();
            if (columnId == null || columnId.trim().isEmpty()) {
                throw new EntityProcessingException("TableColumn must have id attribute in FXML");
            }

            try {
                getFieldForColumn(columnId);
            } catch (RuntimeException e) {
                throw new EntityProcessingException("Column id '" + columnId + "' does not match any field in entity " +
                        entityClass.getName() + ". Available fields: " + getAvailableColumnIds());
            }
        }
    }

    private String getColumnId(Field field) {
        Column columnAnnotation = field.getAnnotation(Column.class);
        if (columnAnnotation != null && !columnAnnotation.name().isEmpty()) {
            return columnAnnotation.name();
        }
        return field.getName();
    }

    private List<String> getAvailableColumnIds() {
        return allFields.stream()
                .filter(field -> isColumnField(field))
                .map(this::getColumnId)
                .collect(Collectors.toList());
    }

    private void initFieldToColumnNameMapping() {
        fieldToColumnNameMap.clear();
        for (Field field : allFields) {
            String columnName = extractColumnName(field);
            fieldToColumnNameMap.put(field, columnName);
        }
    }

    private String extractColumnName(Field field) {
        Column columnAnnotation = field.getAnnotation(Column.class);
        if (columnAnnotation != null) {
            String nameFromAnnotation = columnAnnotation.name();
            if (nameFromAnnotation != null && !nameFromAnnotation.trim().isEmpty()) {
                return nameFromAnnotation.trim();
            }
        }
        return field.getName();
    }

    private String extractTableName() {
        Entity entityAnnotation = entityClass.getAnnotation(Entity.class);
        if (entityAnnotation != null) {
            String tableNameFromAnnotation = entityAnnotation.tableName();
            if (tableNameFromAnnotation != null && !tableNameFromAnnotation.trim().isEmpty()) {
                return tableNameFromAnnotation.trim();
            }
        }
        return entityClass.getSimpleName();
    }

    private List<Field> initFieldsWithoutId() {
        var idField = getIdField();
        return getAllFields().stream().filter(field -> !field.equals(idField)).toList();
    }

    private Field initIdField() {
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Id.class))
                .findFirst()
                .orElseThrow(() -> new EntityProcessingException("Entity class must have a field annotated with @Id"));
    }

    private Constructor<T> initConstructorName() {
        try {
            return entityClass.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new EntityProcessingException("Default constructor not found.");
        }
    }
}
