package pl.maxim.blog.stats;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.maxim.blog.stats.dto.TopPostRow;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stats")
public class StatsController {

    private final PostStatsDao postStatsDao;

    public StatsController(PostStatsDao postStatsDao) {
        this.postStatsDao = postStatsDao;
    }

    @GetMapping("/top-posts")
    public ResponseEntity<List<TopPostRow>> topPosts(@RequestParam(defaultValue = "10") int limit) {
        int safe = Math.max(1, Math.min(limit, 100));
        return ResponseEntity.ok(postStatsDao.topPosts(safe));
    }
}
