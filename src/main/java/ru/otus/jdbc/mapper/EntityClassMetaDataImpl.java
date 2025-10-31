package ru.otus.jdbc.mapper;

import ru.otus.jdbc.annotations.Id;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class EntityClassMetaDataImpl<T> implements EntityClassMetaData<T> {
    private final Class<T> entityClass;
    private Constructor<T> constructor;
    private Field idField;
    private List<Field> allFields;
    private List<Field> fieldsWithoutId;
    private String className;

    public EntityClassMetaDataImpl(Class<T> entityClass) {
        this.entityClass = Objects.requireNonNull(entityClass, "Entity class cannot be null");
        initializeFields();
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

    private void initializeFields() {
        className = entityClass.getSimpleName();
        allFields = List.of(entityClass.getDeclaredFields());
        initConstructorName();
        initIdField();
        initFieldsWithoutId();
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
