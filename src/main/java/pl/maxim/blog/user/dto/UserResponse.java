package pl.maxim.blog.user.dto;

import pl.maxim.blog.user.Role;

public record UserResponse(Long id, String username, String email, Role role, boolean enabled) {}
