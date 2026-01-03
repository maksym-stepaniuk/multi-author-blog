package pl.maxim.blog.rating;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.maxim.blog.common.ResourceNotFoundException;
import pl.maxim.blog.post.Post;
import pl.maxim.blog.post.PostRepository;
import pl.maxim.blog.rating.dto.PostRatingResponse;
import pl.maxim.blog.rating.dto.RatePostRequest;
import pl.maxim.blog.security.CurrentUserService;

@Service
public class RatingService {

    private final PostRepository postRepository;
    private final PostRatingRepository ratingRepository;
    private final CurrentUserService currentUserService;

    public RatingService(PostRepository postRepository, PostRatingRepository ratingRepository, CurrentUserService currentUserService) {
        this.postRepository = postRepository;
        this.ratingRepository = ratingRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public PostRatingResponse rate(Long postId, RatePostRequest req) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found: " + postId));

        var user = currentUserService.requireUser();

        PostRating rating = ratingRepository.findByPostIdAndUserId(postId, user.getId())
                .orElseGet(PostRating::new);

        rating.setPost(post);
        rating.setUser(user);
        rating.setValue(req.value().shortValue());

        ratingRepository.save(rating);

        Double avg = postRepository.findAverageRating(postId);
        return new PostRatingResponse(postId, user.getId(), req.value(), avg);
    }
}
