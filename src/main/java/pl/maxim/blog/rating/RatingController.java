package pl.maxim.blog.rating;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.maxim.blog.rating.dto.PostRatingResponse;
import pl.maxim.blog.rating.dto.RatePostRequest;

@RestController
@RequestMapping("/api/v1/posts/{postId}/rating")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @PutMapping
    public ResponseEntity<PostRatingResponse> rate(@PathVariable Long postId, @Valid @RequestBody RatePostRequest req) {
        return ResponseEntity.ok(ratingService.rate(postId, req));
    }
}
