package ru.otus.jdbc.mapper;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import ru.otus.core.repository.DataTemplate;
import ru.otus.core.repository.DataTemplateException;
import ru.otus.core.repository.executor.DbExecutor;

@SuppressWarnings("java:S3011")
public class DataTemplateJdbc<T> implements DataTemplate<T> {

    private final DbExecutor dbExecutor;
    private final EntitySQLMetaData sqlMetaData;
    private final EntityClassMetaData<T> entityMeta;

    // Нам нужно entityMeta для инстанцирования объекта из ResultSet
    public DataTemplateJdbc(DbExecutor dbExecutor, EntitySQLMetaData sqlMetaData, EntityClassMetaData<T> entityMeta) {
        this.dbExecutor = dbExecutor;
        this.sqlMetaData = sqlMetaData;
        this.entityMeta = entityMeta;
    }

    @Override
    public Optional<T> findById(Connection connection, long id) {
        return dbExecutor.executeSelect(
                connection, sqlMetaData.getSelectByIdSql(), Collections.singletonList(id), rs -> {
                    try {
                        if (rs.next()) {
                            return createEntityFromResultSet(rs);
                        }
                        return null;
                    } catch (SQLException | ReflectiveOperationException e) {
                        throw new DataTemplateException(e);
                    }
                });
    }

    @Override
    public List<T> findAll(Connection connection) {
        return dbExecutor
                .executeSelect(connection, sqlMetaData.getSelectAllSql(), Collections.emptyList(), rs -> {
                    List<T> list = new ArrayList<>();
                    try {
                        while (rs.next()) {
                            list.add(createEntityFromResultSet(rs));
                        }
                        return list;
                    } catch (SQLException | ReflectiveOperationException e) {
                        throw new DataTemplateException(e);
                    }
                })
                .orElseThrow(() -> new DataTemplateException("Unexpected error in findAll"));
    }

    @Override
    public long insert(Connection connection, T entity) {
        try {
            List<Object> params = extractFieldValues(entity, entityMeta.getFieldsWithoutId());
            return dbExecutor.executeStatement(connection, sqlMetaData.getInsertSql(), params);
        } catch (Exception e) {
            throw new DataTemplateException(e);
        }
    }

    @Override
    public void update(Connection connection, T entity) {
        try {
            List<Object> params = new ArrayList<>();
            params.addAll(extractFieldValues(entity, entityMeta.getFieldsWithoutId()));
            params.add(getIdValue(entity)); // WHERE id = ?
            dbExecutor.executeStatement(connection, sqlMetaData.getUpdateSql(), params);
        } catch (Exception e) {
            throw new DataTemplateException(e);
        }
    }

    // Вспомогательные методы

    private T createEntityFromResultSet(ResultSet rs) throws SQLException, ReflectiveOperationException {
        T obj = entityMeta.getConstructor().newInstance();
        for (Field field : entityMeta.getAllFields()) {
            String columnName = field.getName().toLowerCase();
            Object value = rs.getObject(columnName);
            field.set(obj, value);
        }
        return obj;
    }

    private List<Object> extractFieldValues(T entity, List<Field> fields) throws IllegalAccessException {
        List<Object> values = new ArrayList<>();
        for (Field field : fields) {
            values.add(field.get(entity));
        }
        return values;
    }

    private Object getIdValue(T entity) throws IllegalAccessException {
        return entityMeta.getIdField().get(entity);
    }
}
