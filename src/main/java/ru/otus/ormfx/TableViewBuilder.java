package ru.otus.ormfx;

import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.jdbc.mapper.EntityClassMetaData;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public final class TableViewBuilder {
    private static final Logger log = LoggerFactory.getLogger(TableViewBuilder.class);

    private TableViewBuilder() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static <T> void bindTableView(TableView<T> tableView,
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
        TableColumn<T, Object> typedColumn = (TableColumn<T, Object>) column;

        typedColumn.setCellValueFactory(cellData -> {
            try {
                Object value = getFieldValue(cellData.getValue(), field);
                log.debug("Value for column {} is {}", columnId, value);
                return new SimpleObjectProperty<>(value);
            } catch (Exception e) {
                log.error("Error accessing field {} for column {}", field.getName(), columnId, e);
                throw new TableViewCreatingException("Error accessing field " + field.getName() +
                        " for column " + columnId);
            }
        });
    }

    private static Object getFieldValue(Object entity, Field field) throws Exception {
        String getterName = getGetterName(field);
        try {
            Method getter = entity.getClass().getMethod(getterName);
            return getter.invoke(entity);
        } catch (NoSuchMethodException e) {
            boolean accessible = field.canAccess(entity);
            field.setAccessible(true);
            Object value = field.get(entity);
            field.setAccessible(accessible);
            return value;
        }
    }

    private static String getGetterName(Field field) {
        var fieldName = field.getName();
        var prefix = field.getType() == boolean.class ? "is" : "get";
        var getterName = prefix + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        log.debug("Getter name: {}", getterName);
        return getterName;
    }
}
