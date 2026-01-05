package pl.maxim.blog.web.forms;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pl.maxim.blog.user.Role;

public class AdminUserForm {

    @NotNull
    @Email
    private String email;

    @NotNull
    private Role role;

    @NotNull
    private Boolean enabled;

    @Size(max = 100)
    private String password;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
