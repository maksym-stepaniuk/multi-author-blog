package pl.maxim.blog.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;
import pl.maxim.blog.post.PostService;
import pl.maxim.blog.post.Post;
import pl.maxim.blog.post.PostRepository;
import pl.maxim.blog.post.dto.PostCreateRequest;
import pl.maxim.blog.security.CurrentUserService;
import pl.maxim.blog.user.AppUser;
import pl.maxim.blog.user.AppUserRepository;
import pl.maxim.blog.user.Role;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServiceUnitTest {

    @Mock
    PostRepository postRepository;

    @Mock
    AppUserRepository userRepository;

    @Mock
    CurrentUserService currentUserService;

    @InjectMocks
    PostService postService;

    @Test
    void create_requires_current_user_in_authors_when_not_admin() {
        AppUser current = new AppUser();
        current.setUsername("u");
        current.setEmail("u@ex.com");
        current.setPassword("x");
        current.setRole(Role.USER);
        ReflectionTestUtils.setField(current, "id", 1L);

        when(currentUserService.requireUser()).thenReturn(current);
        when(currentUserService.isAdmin()).thenReturn(false);

        PostCreateRequest req = new PostCreateRequest("t", "c", List.of(999L));

        assertThatThrownBy(() -> postService.create(req))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void create_saves_post_when_valid() {
        AppUser current = new AppUser();
        current.setUsername("u");
        current.setEmail("u@ex.com");
        current.setPassword("x");
        current.setRole(Role.USER);
        ReflectionTestUtils.setField(current, "id", 2L);

        AppUser a1 = new AppUser();
        a1.setUsername("a1");
        a1.setEmail("a1@ex.com");
        a1.setPassword("x");
        a1.setRole(Role.USER);
        ReflectionTestUtils.setField(a1, "id", 1L);

        when(currentUserService.requireUser()).thenReturn(current);
        when(currentUserService.isAdmin()).thenReturn(true);
        when(userRepository.findAllById(List.of(1L))).thenReturn(List.of(a1));

        Post saved = new Post();
        saved.setTitle("t");
        saved.setContent("c");
        saved.setAuthors(Set.of(a1));
        ReflectionTestUtils.setField(saved, "id", 10L);

        when(postRepository.save(any(Post.class))).thenReturn(saved);
        when(postRepository.findAverageRating(anyLong())).thenReturn(null);

        var res = postService.create(new PostCreateRequest("t", "c", List.of(1L)));
        assertThat(res.title()).isEqualTo("t");
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void get_throws_when_missing() {
        when(postRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> postService.get(1L)).isInstanceOf(pl.maxim.blog.common.ResourceNotFoundException.class);
    }
}
