package pl.maxim.blog.admin.dto;

import java.util.List;

public record ImportResult(
        int created,
        int updated,
        int failed,
        List<String> errors
) {}
