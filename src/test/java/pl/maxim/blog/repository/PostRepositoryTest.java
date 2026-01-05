package pl.maxim.blog.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import jakarta.persistence.EntityManager;
import pl.maxim.blog.post.Post;
import pl.maxim.blog.post.PostRepository;
import pl.maxim.blog.rating.PostRating;
import pl.maxim.blog.rating.PostRatingRepository;
import pl.maxim.blog.user.AppUser;
import pl.maxim.blog.user.AppUserRepository;
import pl.maxim.blog.user.Role;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PostRepositoryTest extends JpaTcTestBase {

    @Autowired
    PostRepository postRepo;

    @Autowired
    AppUserRepository userRepo;

    @Autowired
    PostRatingRepository ratingRepo;

    @Autowired
    EntityManager em;

    @org.junit.jupiter.api.BeforeEach
    void clean() {
        ratingRepo.deleteAll();
        postRepo.deleteAll();
        userRepo.deleteAll();
        ratingRepo.flush();
        postRepo.flush();
        userRepo.flush();
    }

    @Test
    void save_post_with_authors_and_findDistinctByAuthorsId() {
        AppUser a = new AppUser();
        a.setUsername("a1");
        a.setEmail("a1@ex.com");
        a.setPassword("x");
        a.setRole(Role.USER);
        userRepo.saveAndFlush(a);

        Post p = new Post();
        p.setTitle("t1");
        p.setContent("c1");
        p.getAuthors().add(a);
        postRepo.saveAndFlush(p);

        var page = postRepo.findDistinctByAuthorsId(a.getId(), org.springframework.data.domain.PageRequest.of(0, 10));
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getTitle()).isEqualTo("t1");
    }

    @Test
    void average_rating_query() {
        AppUser a = new AppUser();
        a.setUsername("a2");
        a.setEmail("a2@ex.com");
        a.setPassword("x");
        a.setRole(Role.USER);
        userRepo.saveAndFlush(a);

        Post p = new Post();
        p.setTitle("t2");
        p.setContent("c2");
        p.getAuthors().add(a);
        postRepo.saveAndFlush(p);

        PostRating r1 = new PostRating();
        r1.setPost(p);
        r1.setUser(a);
        r1.setValue((short) 4);
        ratingRepo.saveAndFlush(r1);

        Double avg = postRepo.findAverageRating(p.getId());
        assertThat(avg).isNotNull();
        assertThat(avg).isEqualTo(4.0);
    }

    @Test
    void delete_post_cascades_ratings() {
        AppUser a = new AppUser();
        a.setUsername("a3");
        a.setEmail("a3@ex.com");
        a.setPassword("x");
        a.setRole(Role.USER);
        userRepo.saveAndFlush(a);

        Post p = new Post();
        p.setTitle("t3");
        p.setContent("c3");
        p.getAuthors().add(a);
        postRepo.saveAndFlush(p);

        Post managedPost = postRepo.findById(p.getId()).orElseThrow();

        PostRating r = new PostRating();
        r.setPost(managedPost);
        r.setUser(userRepo.findById(a.getId()).orElseThrow());
        r.setValue((short) 5);
        ratingRepo.saveAndFlush(r);

        em.clear();

        postRepo.deleteById(managedPost.getId());
        postRepo.flush();

        assertThat(ratingRepo.count()).isZero();
    }
}
