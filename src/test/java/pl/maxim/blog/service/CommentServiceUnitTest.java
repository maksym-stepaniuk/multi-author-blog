package pl.maxim.blog.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;
import pl.maxim.blog.comment.Comment;
import pl.maxim.blog.comment.CommentRepository;
import pl.maxim.blog.comment.CommentService;
import pl.maxim.blog.comment.dto.CommentCreateRequest;
import pl.maxim.blog.post.Post;
import pl.maxim.blog.post.PostRepository;
import pl.maxim.blog.security.CurrentUserService;
import pl.maxim.blog.user.AppUser;
import pl.maxim.blog.user.Role;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceUnitTest {

    @Mock CommentRepository commentRepository;
    @Mock PostRepository postRepository;
    @Mock CurrentUserService currentUserService;

    @InjectMocks CommentService service;

    @Test
    void add_requires_post_exists() {
        when(postRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.add(1L, new CommentCreateRequest("x")))
                .isInstanceOf(pl.maxim.blog.common.ResourceNotFoundException.class);
    }

    @Test
    void delete_forbidden_when_not_owner_and_not_admin() {
        AppUser owner = new AppUser();
        ReflectionTestUtils.setField(owner, "id", 1L);
        owner.setUsername("o");
        owner.setEmail("o@ex.com");
        owner.setPassword("p");
        owner.setRole(Role.USER);

        AppUser other = new AppUser();
        ReflectionTestUtils.setField(other, "id", 2L);
        other.setUsername("x");
        other.setEmail("x@ex.com");
        other.setPassword("p");
        other.setRole(Role.USER);

        Post post = new Post();
        ReflectionTestUtils.setField(post, "id", 10L);
        post.setTitle("t");
        post.setContent("c");

        Comment c = new Comment();
        ReflectionTestUtils.setField(c, "id", 100L);
        c.setPost(post);
        c.setAuthor(owner);
        c.setContent("hi");

        when(commentRepository.findById(100L)).thenReturn(Optional.of(c));
        when(currentUserService.requireUser()).thenReturn(other);
        when(currentUserService.isAdmin()).thenReturn(false);

        assertThatThrownBy(() -> service.delete(10L, 100L)).isInstanceOf(AccessDeniedException.class);
        verify(commentRepository, never()).delete(any());
    }
}
