package pl.maxim.blog.comment.dto;

import pl.maxim.blog.post.dto.UserSummary;

import java.time.Instant;

public record CommentResponse(
        Long id,
        Long postId,
        UserSummary author,
        String content,
        Instant createdAt
) {}
