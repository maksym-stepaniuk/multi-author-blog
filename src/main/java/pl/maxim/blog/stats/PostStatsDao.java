package pl.maxim.blog.stats;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import pl.maxim.blog.stats.dto.TopPostRow;

import java.util.List;

@Repository
public class PostStatsDao {

    private final JdbcTemplate jdbcTemplate;

    public PostStatsDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<TopPostRow> topPosts(int limit) {
        String sql = """
                select p.id as post_id,
                       p.title as title,
                       coalesce(r.avg_rating, 0) as avg_rating,
                       coalesce(c.comment_count, 0) as comment_count
                from post p
                left join (
                    select post_id, avg(value) as avg_rating
                    from post_rating
                    group by post_id
                ) r on r.post_id = p.id
                left join (
                    select post_id, count(*) as comment_count
                    from comment
                    group by post_id
                ) c on c.post_id = p.id
                order by avg_rating desc, comment_count desc, p.created_at desc
                limit ?
                """;

        RowMapper<TopPostRow> mapper = (rs, i) -> new TopPostRow(
                rs.getLong("post_id"),
                rs.getString("title"),
                rs.getDouble("avg_rating"),
                rs.getLong("comment_count")
        );

        return jdbcTemplate.query(sql, mapper, limit);
    }
}
