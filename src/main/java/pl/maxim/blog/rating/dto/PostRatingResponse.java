package pl.maxim.blog.rating.dto;

public record PostRatingResponse(
        Long postId,
        Long userId,
        int value,
        Double average
) {}
