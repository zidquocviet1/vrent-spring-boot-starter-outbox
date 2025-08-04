package vrent.event.outbox.repository;

import java.util.List;
import org.springframework.jdbc.core.simple.JdbcClient;
import vrent.event.outbox.repository.entity.ColumnInfo;
import vrent.event.outbox.repository.mapper.ColumnInfoMapper;

public class InformationSchemaRepository {
  private static final String CHECK_TABLE_SQL =
      "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = ? AND table_schema = CURRENT_SCHEMA()";

  private static final String GET_COLUMNS_TYPE_SQL =
      "SELECT column_name, data_type FROM information_schema.columns WHERE table_name = ? AND table_schema = CURRENT_SCHEMA()";

  private final JdbcClient jdbcClient;

  public InformationSchemaRepository(JdbcClient jdbcClient) {
    this.jdbcClient = jdbcClient;
  }

  public boolean isTableExists(String tableName) {
    return jdbcClient.sql(CHECK_TABLE_SQL).param(tableName).query(Integer.class).single() > 0;
  }

  public List<ColumnInfo> getColumnInfos(String tableName) {
    return jdbcClient
        .sql(GET_COLUMNS_TYPE_SQL)
        .param(tableName)
        .query(new ColumnInfoMapper())
        .list();
  }

  public void createTable(String query) {
    jdbcClient.sql(query).update();
  }
}
