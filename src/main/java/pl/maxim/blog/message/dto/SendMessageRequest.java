package pl.maxim.blog.message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SendMessageRequest(
        @NotNull Long recipientId,
        @NotBlank @Size(max = 5000) String content
) {}
