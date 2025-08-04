package vrent.event.outbox.validator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vrent.event.outbox.repository.InformationSchemaRepository;
import vrent.event.outbox.repository.entity.ColumnInfo;

public class OutboxTableValidator {
  private static final Logger logger = LoggerFactory.getLogger(OutboxTableValidator.class);

  private static final String CREATE_OUTBOX_TABLE_SQL =
      """
          CREATE TABLE %s
          (
              id             UUID                        NOT NULL PRIMARY KEY,
              aggregate_id   VARCHAR(255)                NOT NULL,
              aggregate_type VARCHAR(255)                NOT NULL,
              topic          VARCHAR(255)                NOT NULL,
              key_class      VARCHAR(255),
              key            VARCHAR(10000),
              value_class    VARCHAR(255)                NOT NULL,
              value          VARCHAR(10000)              NOT NULL,
              processed      BOOLEAN DEFAULT FALSE       NOT NULL,
              is_error       BOOLEAN DEFAULT FALSE       NOT NULL,
              retry_attempt  INTEGER DEFAULT 0           NOT NULL,
              last_retry_at  TIMESTAMP(6) WITH TIME ZONE,
              created_at     TIMESTAMP(6) WITH TIME ZONE NOT NULL,
              processed_at   TIMESTAMP(6) WITH TIME ZONE,
              updated_at     TIMESTAMP(6) WITH TIME ZONE,
              error_message  VARCHAR(1000)
          );
      """;

  private static final Map<String, String> MANDATORY_COLUMN_TYPES = new HashMap<>();

  static {
    MANDATORY_COLUMN_TYPES.put("id", "uuid");
    MANDATORY_COLUMN_TYPES.put("is_error", "boolean");
    MANDATORY_COLUMN_TYPES.put("retry_attempt", "integer");
    MANDATORY_COLUMN_TYPES.put("last_retry_at", "timestamp with time zone");
    MANDATORY_COLUMN_TYPES.put("created_at", "timestamp with time zone");
    MANDATORY_COLUMN_TYPES.put("processed_at", "timestamp with time zone");
    MANDATORY_COLUMN_TYPES.put("updated_at", "timestamp with time zone");
    MANDATORY_COLUMN_TYPES.put("key", "character varying");
    MANDATORY_COLUMN_TYPES.put("value", "character varying");
    MANDATORY_COLUMN_TYPES.put("processed", "boolean");
    MANDATORY_COLUMN_TYPES.put("aggregate_id", "character varying");
    MANDATORY_COLUMN_TYPES.put("aggregate_type", "character varying");
    MANDATORY_COLUMN_TYPES.put("topic", "character varying");
    MANDATORY_COLUMN_TYPES.put("key_class", "character varying");
    MANDATORY_COLUMN_TYPES.put("error_message", "character varying");
    MANDATORY_COLUMN_TYPES.put("value_class", "character varying");
  }

  private final InformationSchemaRepository informationSchemaRepository;
  private final String tableName;

  public OutboxTableValidator(
      final InformationSchemaRepository informationSchemaRepository, final String tableName) {
    this.informationSchemaRepository = informationSchemaRepository;
    this.tableName = tableName;
  }

  public void validate() {
    final boolean isTableExists = informationSchemaRepository.isTableExists(tableName);
    if (isTableExists) {
      if (logger.isDebugEnabled()) {
        logger.debug(
            "Outbox table '{}' already exists, validate all required columns for checking the compatibility",
            tableName);
      }
      ensureMandatoryColumnsExists();
    } else {
      if (logger.isDebugEnabled()) {
        logger.debug(
            "Outbox table '{}' does not exist, try to create with query: {}",
            tableName,
            CREATE_OUTBOX_TABLE_SQL);
      }
      informationSchemaRepository.createTable(String.format(CREATE_OUTBOX_TABLE_SQL, tableName));
    }
  }

  public void ensureMandatoryColumnsExists() {
    final List<ColumnInfo> columns = informationSchemaRepository.getColumnInfos(tableName);

    if (columns.size() < MANDATORY_COLUMN_TYPES.size()) {
      throw new IllegalStateException(
          "Table '"
              + tableName
              + "' does not have all required columns. Expected columns: "
              + MANDATORY_COLUMN_TYPES.keySet()
              + ", actual columns: "
              + columns.stream().map(ColumnInfo::columnName).toList());
    }

    for (ColumnInfo columnInfo : columns) {
      final String requiredDataType = MANDATORY_COLUMN_TYPES.get(columnInfo.columnName());
      final boolean matchType = requiredDataType.equals(columnInfo.dataType());

      if (!matchType) {
        throw new IllegalStateException(
            "Column '"
                + columnInfo.columnName()
                + "' has wrong data type. Expected: "
                + requiredDataType
                + ", actual: "
                + columnInfo.dataType());
      }
    }
  }
}
