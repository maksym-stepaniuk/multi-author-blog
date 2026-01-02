package pl.maxim.blog.post.dto;

import java.time.Instant;
import java.util.List;

public record PostResponse(
        Long id,
        String title,
        String content,
        List<UserSummary> authors,
        Double averageRating,
        Instant createdAt,
        Instant updatedAt
) {}
