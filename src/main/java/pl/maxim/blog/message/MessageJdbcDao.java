package pl.maxim.blog.message;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class MessageJdbcDao {

    private final JdbcTemplate jdbcTemplate;

    public MessageJdbcDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int deleteById(Long id) {
        return jdbcTemplate.update("delete from message where id = ?", id);
    }
}
