package pl.maxim.blog.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pl.maxim.blog.post.Post;
import pl.maxim.blog.post.PostRepository;
import pl.maxim.blog.rating.PostRating;
import pl.maxim.blog.rating.PostRatingRepository;
import pl.maxim.blog.rating.RatingService;
import pl.maxim.blog.rating.dto.RatePostRequest;
import pl.maxim.blog.security.CurrentUserService;
import pl.maxim.blog.user.AppUser;
import pl.maxim.blog.user.Role;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RatingServiceUnitTest {

    @Mock PostRepository postRepository;
    @Mock PostRatingRepository ratingRepository;
    @Mock CurrentUserService currentUserService;

    @InjectMocks RatingService service;

    @Test
    void rate_requires_post_exists() {
        when(postRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.rate(1L, new RatePostRequest(5)))
                .isInstanceOf(pl.maxim.blog.common.ResourceNotFoundException.class);
    }

    @Test
    void rate_upserts_rating() {
        AppUser u = new AppUser();
        ReflectionTestUtils.setField(u, "id", 1L);
        u.setUsername("u");
        u.setEmail("u@ex.com");
        u.setPassword("p");
        u.setRole(Role.USER);

        Post p = new Post();
        ReflectionTestUtils.setField(p, "id", 10L);
        p.setTitle("t");
        p.setContent("c");

        when(postRepository.findById(10L)).thenReturn(Optional.of(p));
        when(currentUserService.requireUser()).thenReturn(u);
        when(ratingRepository.findByPostIdAndUserId(10L, 1L)).thenReturn(Optional.empty());
        when(postRepository.findAverageRating(10L)).thenReturn(4.0);

        service.rate(10L, new RatePostRequest(5));

        verify(ratingRepository).save(any(PostRating.class));
    }
}
