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
    private Constructor<T> constructor;
    private Field idField;
    private List<Field> allFields;
    private List<Field> fieldsWithoutId;
    private String className;

    private Map<Field, String> fieldToColumnNameMap = new HashMap<>();

    public EntityClassMetaDataImpl(Class<T> entityClass) {
        this.entityClass = Objects.requireNonNull(entityClass, "Entity class cannot be null");
        initializeFields();
    }

    public Map<Field, String> getFieldToColumnNameMap() {
        return fieldToColumnNameMap;
    }

    @Override
    public String getName() {
        return className;
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

    @Override
    public void validateColumns(List<TableColumn<T, ?>> columns) {
        for (TableColumn<T, ?> column : columns) {
            String columnId = column.getId();
            if (columnId == null || columnId.trim().isEmpty()) {
                throw new RuntimeException("TableColumn must have id attribute in FXML");
            }

            try {
                getFieldForColumn(columnId);
            } catch (RuntimeException e) {
                throw new RuntimeException("Column id '" + columnId + "' does not match any field in entity " +
                        entityClass.getName() + ". Available fields: " + getAvailableColumnIds());
            }
        }
    }

    private void initializeFields() {
        //className = entityClass.getSimpleName();
        className = extractTableName();

        allFields = List.of(entityClass.getDeclaredFields());
        initFieldToColumnNameMapping();

        initConstructorName();
        initIdField();
        initFieldsWithoutId();
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
        // Если аннотации @Column нет или name пустой, используем имя поля
        return field.getName();
    }

    public String getColumnName(Field field) {
        return fieldToColumnNameMap.get(field);
    }

    private String extractTableName() {
        Entity entityAnnotation = entityClass.getAnnotation(Entity.class);
        if (entityAnnotation != null) {
            String tableNameFromAnnotation = entityAnnotation.tableName();
            if (tableNameFromAnnotation != null && !tableNameFromAnnotation.trim().isEmpty()) {
                return tableNameFromAnnotation.trim();
            }
        }
        // Если аннотации нет или tableName пустое, возвращаем имя класса
        return entityClass.getSimpleName();
    }

    private void initFieldsWithoutId() {
        var idField = getIdField();
        this.fieldsWithoutId =
                getAllFields().stream().filter(field -> !field.equals(idField)).toList();
    }

    private void initIdField() {
        idField = Arrays.stream(entityClass.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Id.class))
                .findFirst()
                .orElseThrow(() -> new EntityProcessingException("Entity class must have a field annotated with @Id"));
    }

    private void initConstructorName() {
        try {
            this.constructor = entityClass.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new EntityProcessingException("Default constructor not found.");
        }
    }
}
