package ru.otus.jdbc.mapper;

import javafx.scene.control.TableColumn;
import ru.otus.jdbc.annotations.Column;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/** "Разбирает" объект на составные части */
public interface EntityClassMetaData<T> {
    // Имя класса для таблицы
    String getName();

    // Через этот конструктор DataTemplate будет создавать экземпляры
    Constructor<T> getConstructor();

    // Поле Id должно определять по наличию аннотации Id
    // Аннотацию @Id надо сделать самостоятельно
    Field getIdField();

    List<Field> getAllFields();

    List<Field> getFieldsWithoutId();

    Field getFieldForColumn(String columnId);

    // Метод для проверки соответствия колонок полям
    void validateColumns(List<TableColumn<T, ?>> columns);

    Map<Field, String> getFieldToColumnNameMap();

    default boolean isColumnField(Field field) {
        return field.isAnnotationPresent(Column.class);
    }
}
