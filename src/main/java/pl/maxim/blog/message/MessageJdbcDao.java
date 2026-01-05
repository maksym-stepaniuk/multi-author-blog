package pl.maxim.blog.message;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import pl.maxim.blog.message.dto.ConversationMessageResponse;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Repository
public class MessageJdbcDao {

    private final JdbcTemplate jdbcTemplate;

    public MessageJdbcDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int deleteById(Long id) {
        return jdbcTemplate.update("delete from message where id = ?", id);
    }

    public long conversationCount(Long u1, Long u2) {
        String sql = """
                select count(*)
                from message m
                where (m.sender_id = ? and m.recipient_id = ?)
                   or (m.sender_id = ? and m.recipient_id = ?)
                """;
        Long v = jdbcTemplate.queryForObject(sql, Long.class, u1, u2, u2, u1);
        return v == null ? 0L : v;
    }

    public List<ConversationMessageResponse> conversation(Long u1, Long u2, int limit, int offset) {
        String sql = """
                select m.id,
                       m.sender_id,
                       s.username as sender_username,
                       m.recipient_id,
                       r.username as recipient_username,
                       m.content,
                       m.created_at
                from message m
                join app_user s on s.id = m.sender_id
                join app_user r on r.id = m.recipient_id
                where (m.sender_id = ? and m.recipient_id = ?)
                   or (m.sender_id = ? and m.recipient_id = ?)
                order by m.created_at desc
                limit ? offset ?
                """;

        RowMapper<ConversationMessageResponse> mapper = (rs, i) -> {
            Timestamp ts = rs.getTimestamp("created_at");
            Instant createdAt = ts == null ? null : ts.toInstant();
            return new ConversationMessageResponse(
                    rs.getLong("id"),
                    rs.getLong("sender_id"),
                    rs.getString("sender_username"),
                    rs.getLong("recipient_id"),
                    rs.getString("recipient_username"),
                    rs.getString("content"),
                    createdAt
            );
        };

        return jdbcTemplate.query(sql, mapper, u1, u2, u2, u1, limit, offset);
    }
}
