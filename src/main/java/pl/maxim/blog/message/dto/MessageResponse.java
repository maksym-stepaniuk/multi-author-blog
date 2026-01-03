package pl.maxim.blog.message.dto;

import pl.maxim.blog.post.dto.UserSummary;

import java.time.Instant;

public record MessageResponse(
        Long id,
        UserSummary sender,
        UserSummary recipient,
        String content,
        Instant createdAt
) {}
