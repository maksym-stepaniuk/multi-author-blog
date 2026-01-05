package pl.maxim.blog.message.dto;

import java.time.Instant;

public record ConversationMessageResponse(
        Long id,
        Long senderId,
        String senderUsername,
        Long recipientId,
        String recipientUsername,
        String content,
        Instant createdAt
) {}
