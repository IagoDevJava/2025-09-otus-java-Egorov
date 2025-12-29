package ru.otus.jdbc.mapper;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EntitySQLMetaDataImpl implements EntitySQLMetaData {

  private final String selectAllSql;
  private final String selectByIdSql;
  private final String insertSql;
  private final String updateSql;

  public EntitySQLMetaDataImpl(EntityClassMetaData<?> entityMeta) {
    String tableName = entityMeta.getName().toLowerCase();
    Field idField = entityMeta.getIdField();
    String idColumnName = idField.getName().toLowerCase();

    String allColumns = Stream.concat(
            Stream.of(idField),
            entityMeta.getFieldsWithoutId().stream()
        )
        .map(Field::getName)
        .map(String::toLowerCase)
        .collect(Collectors.joining(", "));
    this.selectAllSql = "SELECT " + allColumns + " FROM " + tableName;

    this.selectByIdSql =
        "SELECT " + allColumns + " FROM " + tableName + " WHERE " + idColumnName + " = ?";

    var nonIdFields = entityMeta.getFieldsWithoutId();
    String insertColumns = nonIdFields.stream()
        .map(Field::getName)
        .map(String::toLowerCase)
        .collect(Collectors.joining(", "));
    String placeholders = String.join(", ", Collections.nCopies(nonIdFields.size(), "?"));
    this.insertSql =
        "INSERT INTO " + tableName + " (" + insertColumns + ") VALUES (" + placeholders + ")";

    String setClause = nonIdFields.stream()
        .map(Field::getName)
        .map(name -> name.toLowerCase() + " = ?")
        .collect(Collectors.joining(", "));
    this.updateSql =
        "UPDATE " + tableName + " SET " + setClause + " WHERE " + idColumnName + " = ?";
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
}