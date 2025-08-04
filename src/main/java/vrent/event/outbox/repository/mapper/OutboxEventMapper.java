package vrent.event.outbox.repository.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.jdbc.core.RowMapper;
import vrent.event.outbox.repository.entity.OutboxEvent;

public class OutboxEventMapper implements RowMapper<OutboxEvent> {
  @Override
  public OutboxEvent mapRow(ResultSet rs, int rowNum) throws SQLException {
    return new OutboxEvent(
        UUID.fromString(rs.getString("id")),
        rs.getString("aggregate_id"),
        rs.getString("aggregate_type"),
        rs.getString("topic"),
        rs.getString("key_class"),
        rs.getString("key"),
        rs.getString("value_class"),
        rs.getString("value"),
        rs.getBoolean("processed"),
        fromDate(rs.getDate("created_at")),
        fromDate(rs.getDate("updated_at")),
        fromDate(rs.getDate("processed_at")),
        rs.getBoolean("is_error"),
        rs.getString("error_message"),
        rs.getInt("retry_attempt"),
        fromDate(rs.getDate("last_retry_at")));
  }

  private static LocalDate fromDate(java.sql.Date date) {
    if (date == null) {
      return null;
    }
    return date.toLocalDate();
  }
}
