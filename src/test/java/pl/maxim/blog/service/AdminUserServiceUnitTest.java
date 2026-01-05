package pl.maxim.blog.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import pl.maxim.blog.admin.AdminUserService;
import pl.maxim.blog.admin.dto.AdminUserUpdateRequest;
import pl.maxim.blog.post.Post;
import pl.maxim.blog.post.PostRepository;
import pl.maxim.blog.user.AppUser;
import pl.maxim.blog.user.AppUserRepository;
import pl.maxim.blog.user.Role;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceUnitTest {

    @Mock
    AppUserRepository userRepository;

    @Mock
    PostRepository postRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    AdminUserService service;

    @Test
    void update_sets_fields_and_encodes_password_when_present() {
        AppUser u = new AppUser();
        ReflectionTestUtils.setField(u, "id", 1L);
        u.setUsername("u1");
        u.setEmail("old@ex.com");
        u.setPassword("old");
        u.setRole(Role.USER);
        u.setEnabled(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        when(passwordEncoder.encode("newpass")).thenReturn("ENC");
        when(userRepository.save(any(AppUser.class))).thenAnswer(inv -> inv.getArgument(0));

        var res = service.update(1L, new AdminUserUpdateRequest("new@ex.com", Role.ADMIN, false, "newpass"));

        assertThat(res.email()).isEqualTo("new@ex.com");
        assertThat(res.role()).isEqualTo(Role.ADMIN);
        assertThat(res.enabled()).isFalse();
        assertThat(u.getPassword()).isEqualTo("ENC");
        verify(userRepository).save(u);
    }

    @Test
    void delete_removes_user_from_posts_and_deletes_empty_posts() {
        AppUser u = new AppUser();
        ReflectionTestUtils.setField(u, "id", 10L);
        u.setUsername("x");
        u.setEmail("x@ex.com");
        u.setPassword("p");
        u.setRole(Role.USER);

        AppUser other = new AppUser();
        ReflectionTestUtils.setField(other, "id", 11L);
        other.setUsername("y");
        other.setEmail("y@ex.com");
        other.setPassword("p");
        other.setRole(Role.USER);

        Post p1 = new Post();
        ReflectionTestUtils.setField(p1, "id", 1L);
        p1.setTitle("t1");
        p1.setContent("c1");
        p1.setAuthors(Set.of(u));

        Post p2 = new Post();
        ReflectionTestUtils.setField(p2, "id", 2L);
        p2.setTitle("t2");
        p2.setContent("c2");
        p2.setAuthors(Set.of(u, other));

        when(userRepository.findById(10L)).thenReturn(Optional.of(u));
        when(postRepository.findDistinctByAuthorsId(10L)).thenReturn(List.of(p1, p2));

        service.delete(10L);

        verify(postRepository).delete(p1);
        verify(postRepository).save(argThat((ArgumentMatcher<Post>) p -> p.getId().equals(2L) && p.getAuthors().size() == 1));
        verify(userRepository).delete(u);
    }
}
