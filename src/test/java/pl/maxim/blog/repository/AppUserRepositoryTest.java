package pl.maxim.blog.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import pl.maxim.blog.user.AppUser;
import pl.maxim.blog.user.AppUserRepository;
import pl.maxim.blog.user.Role;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AppUserRepositoryTest extends JpaTcTestBase {

    @Autowired
    AppUserRepository repo;

    @org.junit.jupiter.api.BeforeEach
    void clean() {
        repo.deleteAll();
    }

    @Test
    void save_and_findByUsername() {
        AppUser u = new AppUser();
        u.setUsername("u1");
        u.setEmail("u1@ex.com");
        u.setPassword("x");
        u.setRole(Role.USER);
        repo.save(u);

        Optional<AppUser> found = repo.findByUsername("u1");
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("u1@ex.com");
    }

    @Test
    void findByUsername_missing() {
        assertThat(repo.findByUsername("nope")).isEmpty();
    }

    @Test
    void findByUsernameContainingIgnoreCase() {
        AppUser a = new AppUser();
        a.setUsername("AliceX");
        a.setEmail("a@ex.com");
        a.setPassword("x");
        a.setRole(Role.USER);

        AppUser b = new AppUser();
        b.setUsername("BobY");
        b.setEmail("b@ex.com");
        b.setPassword("x");
        b.setRole(Role.USER);

        repo.save(a);
        repo.save(b);

        var page = repo.findByUsernameContainingIgnoreCase("alicex", org.springframework.data.domain.PageRequest.of(0, 10));
        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getUsername()).isEqualTo("AliceX");
    }
}
