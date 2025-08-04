package vrent.event.outbox.repository.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import vrent.event.outbox.repository.entity.ColumnInfo;

public class ColumnInfoMapper implements RowMapper<ColumnInfo> {
  @Override
  public ColumnInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
    return new ColumnInfo(rs.getString("column_name"), rs.getString("data_type"));
  }
}
