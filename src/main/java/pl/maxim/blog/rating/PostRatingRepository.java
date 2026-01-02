package pl.maxim.blog.rating;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostRatingRepository extends JpaRepository<PostRating, Long> {
    Optional<PostRating> findByPostIdAndUserId(Long postId, Long userId);
}
