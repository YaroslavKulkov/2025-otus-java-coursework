package ru.otus.ormfx;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import ru.otus.jdbc.mapper.EntityClassMetaData;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static org.flywaydb.core.internal.util.ClassUtils.getFieldValue;

public class TableViewBuilder {

    public static <T> void bindTableView(TableView<T> tableView, Class<T> entityClass,
                                         EntityClassMetaData<T> metaData) {
        List<TableColumn<T, ?>> columns = tableView.getColumns();

        metaData.validateColumns(columns);

        for (TableColumn<T, ?> column : columns) {
            configureColumnCellValueFactory(column, metaData);
        }
    }

    private static <T> void configureColumnCellValueFactory(TableColumn<T, ?> column,
                                                            EntityClassMetaData<T> metaData) {
        String columnId = column.getId();
        Field field = metaData.getFieldForColumn(columnId);

        // Приводим к правильному типу
        TableColumn<T, Object> typedColumn = (TableColumn<T, Object>) column;

        typedColumn.setCellValueFactory(cellData -> {
            try {
                Object value = getFieldValue(cellData.getValue(), field);
                return new SimpleObjectProperty<>(value);
            } catch (Exception e) {
                throw new RuntimeException("Error accessing field " + field.getName() +
                        " for column " + columnId, e);
            }
        });

    }

    private static Object getFieldValue(Object entity, Field field) throws Exception {
        // Пытаемся получить значение через геттер
        String getterName = getGetterName(field);
        try {
            Method getter = entity.getClass().getMethod(getterName);
            return getter.invoke(entity);
        } catch (NoSuchMethodException e) {
            // Если геттера нет, получаем значение напрямую через reflection
            boolean accessible = field.canAccess(entity);
            field.setAccessible(true);
            Object value = field.get(entity);
            field.setAccessible(accessible);
            return value;
        }
    }

    private static String getGetterName(Field field) {
        String fieldName = field.getName();
        String prefix = field.getType() == boolean.class ? "is" : "get";
        return prefix + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }
}
