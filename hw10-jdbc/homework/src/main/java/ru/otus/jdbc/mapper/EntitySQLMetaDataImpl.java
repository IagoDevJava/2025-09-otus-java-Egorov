package ru.otus.jdbc.mapper;

import java.lang.reflect.Field;
import java.util.stream.Collectors;

public class EntitySQLMetaDataImpl implements EntitySQLMetaData {

    private final EntityClassMetaData<?> entityMeta;

    public EntitySQLMetaDataImpl(EntityClassMetaData<?> entityMeta) {
        this.entityMeta = entityMeta;
    }

    @Override
    public String getSelectAllSql() {
        String tableName = entityMeta.getName().toLowerCase();
        return "SELECT * FROM " + tableName;
    }

    @Override
    public String getSelectByIdSql() {
        String tableName = entityMeta.getName().toLowerCase();
        String idColumnName = entityMeta.getIdField().getName().toLowerCase();
        return "SELECT * FROM " + tableName + " WHERE " + idColumnName + " = ?";
    }

    @Override
    public String getInsertSql() {
        String tableName = entityMeta.getName().toLowerCase();
        String columns = entityMeta.getFieldsWithoutId().stream()
                .map(Field::getName)
                .map(String::toLowerCase)
                .collect(Collectors.joining(", "));
        String placeholders =
                entityMeta.getFieldsWithoutId().stream().map(f -> "?").collect(Collectors.joining(", "));
        return "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + placeholders + ")";
    }

    @Override
    public String getUpdateSql() {
        String tableName = entityMeta.getName().toLowerCase();
        String idColumnName = entityMeta.getIdField().getName().toLowerCase();

        String setClause = entityMeta.getFieldsWithoutId().stream()
                .map(Field::getName)
                .map(name -> name.toLowerCase() + " = ?")
                .collect(Collectors.joining(", "));

        return "UPDATE " + tableName + " SET " + setClause + " WHERE " + idColumnName + " = ?";
    }
}
