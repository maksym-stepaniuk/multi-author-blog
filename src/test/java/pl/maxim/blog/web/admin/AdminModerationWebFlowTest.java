package pl.maxim.blog.web.admin;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import pl.maxim.blog.api.ApiIntegrationTestBase;
import pl.maxim.blog.post.Post;
import pl.maxim.blog.post.PostRepository;
import pl.maxim.blog.user.AppUser;
import pl.maxim.blog.user.AppUserRepository;
import pl.maxim.blog.user.Role;

import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class AdminModerationWebFlowTest extends ApiIntegrationTestBase {

    @Autowired MockMvc mvc;
    @Autowired AppUserRepository userRepo;
    @Autowired PostRepository postRepo;

    private AppUser ensureUser(String username, Role role) {
        return userRepo.findByUsername(username).orElseGet(() -> {
            AppUser u = new AppUser();
            u.setUsername(username);
            u.setEmail(username + "@ex.com");
            u.setPassword("x");
            u.setRole(role);
            u.setEnabled(true);
            return userRepo.save(u);
        });
    }

    private Long createPost(AppUser author) {
        Post p = new Post();
        p.setTitle("AdminPost");
        p.setContent("C");
        p.setAuthors(Set.of(author));
        return postRepo.save(p).getId();
    }

    @Test
    @WithMockUser(username = "admin_db", roles = "ADMIN")
    void moderation_users_and_posts_pages_and_updates() throws Exception {
        ensureUser("admin_db", Role.ADMIN);
        AppUser u = ensureUser("u1", Role.USER);
        Long postId = createPost(u);

        mvc.perform(get("/admin/moderation/users")).andExpect(status().isOk());
        mvc.perform(get("/admin/moderation/posts")).andExpect(status().isOk());

        mvc.perform(get("/admin/moderation/users/" + u.getId() + "/edit"))
                .andExpect(status().isOk());

        mvc.perform(post("/admin/moderation/users/" + u.getId())
                        .with(csrf())
                        .param("email", "new@ex.com")
                        .param("role", "ADMIN")
                        .param("enabled", "true")
                        .param("password", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/moderation/users"));

        mvc.perform(get("/admin/moderation/posts/" + postId + "/edit"))
                .andExpect(status().isOk());

        mvc.perform(post("/admin/moderation/posts/" + postId)
                        .with(csrf())
                        .param("title", "AdminPost2")
                        .param("content", "C2")
                        .param("authorIds", u.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/moderation/posts"));

        mvc.perform(post("/admin/moderation/posts/" + postId + "/delete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/moderation/posts"));
    }
}
