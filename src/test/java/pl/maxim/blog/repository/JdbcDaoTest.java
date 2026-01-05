package pl.maxim.blog.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import pl.maxim.blog.message.MessageJdbcDao;
import pl.maxim.blog.post.Post;
import pl.maxim.blog.post.PostRepository;
import pl.maxim.blog.stats.PostStatsDao;
import pl.maxim.blog.user.AppUser;
import pl.maxim.blog.user.AppUserRepository;
import pl.maxim.blog.user.Role;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({pl.maxim.blog.stats.PostStatsDao.class, MessageJdbcDao.class})
class JdbcDaoTest extends JpaTcTestBase {

    @Autowired
    PostStatsDao statsDao;

    @Autowired
    MessageJdbcDao messageJdbcDao;

    @Autowired
    PostRepository postRepo;

    @Autowired
    AppUserRepository userRepo;

    @Test
    void topPosts_returns_rows() {
        AppUser a = new AppUser();
        a.setUsername("s1");
        a.setEmail("s1@ex.com");
        a.setPassword("x");
        a.setRole(Role.USER);
        userRepo.save(a);

        Post p = new Post();
        p.setTitle("Top");
        p.setContent("x");
        p.getAuthors().add(a);
        postRepo.save(p);

        var rows = statsDao.topPosts(10);
        assertThat(rows).isNotEmpty();
        assertThat(rows.get(0).title()).isNotBlank();
    }

    @Test
    void conversationCount_zero_for_new_users() {
        AppUser a = new AppUser();
        a.setUsername("m1");
        a.setEmail("m1@ex.com");
        a.setPassword("x");
        a.setRole(Role.USER);

        AppUser b = new AppUser();
        b.setUsername("m2");
        b.setEmail("m2@ex.com");
        b.setPassword("x");
        b.setRole(Role.USER);

        userRepo.save(a);
        userRepo.save(b);

        long c = messageJdbcDao.conversationCount(a.getId(), b.getId());
        assertThat(c).isEqualTo(0);
    }
}
