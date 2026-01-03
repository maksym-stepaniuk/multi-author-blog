package pl.maxim.blog.stats.dto;

public record TopPostRow(
        Long postId,
        String title,
        Double averageRating,
        Long commentCount
) {}
