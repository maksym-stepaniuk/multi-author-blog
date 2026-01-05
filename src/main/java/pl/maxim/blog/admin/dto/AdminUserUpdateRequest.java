package pl.maxim.blog.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pl.maxim.blog.user.Role;

public record AdminUserUpdateRequest(
        @NotNull @Email String email,
        @NotNull Role role,
        @NotNull Boolean enabled,
        @Size(max = 100) String password
) {}
