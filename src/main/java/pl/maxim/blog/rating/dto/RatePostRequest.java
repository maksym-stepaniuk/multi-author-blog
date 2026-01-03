package pl.maxim.blog.rating.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RatePostRequest(
        @NotNull @Min(1) @Max(5) Integer value
) {}
