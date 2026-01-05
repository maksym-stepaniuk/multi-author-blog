package pl.maxim.blog.web;

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
class DashboardWebFlowTest extends ApiIntegrationTestBase {

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
        p.setTitle("Old");
        p.setContent("C");
        p.setAuthors(Set.of(author));
        return postRepo.save(p).getId();
    }

    @Test
    @WithMockUser(username = "alice", roles = "USER")
    void dashboard_get_ok() throws Exception {
        ensureUser("alice", Role.USER);
        mvc.perform(get("/dashboard")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "alice", roles = "USER")
    void create_post_forbidden_when_current_not_in_authors() throws Exception {
        AppUser alice = ensureUser("alice", Role.USER);
        AppUser bob = ensureUser("bob", Role.USER);

        mvc.perform(post("/dashboard/posts")
                        .with(csrf())
                        .param("title", "T1")
                        .param("content", "C1")
                        .param("authorIds", bob.getId().toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin_db", roles = "ADMIN")
    void create_update_delete_happy_path() throws Exception {
        AppUser admin = ensureUser("admin_db", Role.ADMIN);

        mvc.perform(post("/dashboard/posts")
                        .with(csrf())
                        .param("title", "T1")
                        .param("content", "C1")
                        .param("authorIds", admin.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));

        Long postId = postRepo.findAll().stream()
                .filter(p -> "T1".equals(p.getTitle()))
                .map(Post::getId)
                .findFirst().orElseThrow();

        mvc.perform(post("/dashboard/posts/" + postId)
                        .with(csrf())
                        .param("title", "T2")
                        .param("content", "C2")
                        .param("authorIds", admin.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));

        mvc.perform(post("/dashboard/posts/" + postId + "/delete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }
}
