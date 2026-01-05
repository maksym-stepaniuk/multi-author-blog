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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class WebPagesSmokeTest extends ApiIntegrationTestBase {

    @Autowired
    MockMvc mvc;

    @Autowired
    AppUserRepository userRepo;

    @Autowired
    PostRepository postRepo;

    private Long ensureUser(String username) {
        return userRepo.findByUsername(username).map(AppUser::getId).orElseGet(() -> {
            AppUser u = new AppUser();
            u.setUsername(username);
            u.setEmail(username + "@ex.com");
            u.setPassword("x");
            u.setRole(Role.USER);
            return userRepo.save(u).getId();
        });
    }

    private Long ensurePostWithAuthor(String username) {
        ensureUser(username);
        AppUser u = userRepo.findByUsername(username).orElseThrow();
        Post p = new Post();
        p.setTitle("web-post");
        p.setContent("<b>x</b>");
        p.setAuthors(Set.of(u));
        return postRepo.save(p).getId();
    }

    @Test
    void public_wall_ok() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk());
    }

    @Test
    void public_post_page_ok() throws Exception {
        Long id = ensurePostWithAuthor("alice");
        mvc.perform(get("/posts/" + id)).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "alice", roles = "USER")
    void dashboard_ok() throws Exception {
        ensureUser("alice");
        mvc.perform(get("/dashboard")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin_db", roles = "ADMIN")
    void admin_moderation_pages_ok() throws Exception {
        mvc.perform(get("/admin/moderation/users")).andExpect(status().isOk());
        mvc.perform(get("/admin/moderation/posts")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "alice", roles = "USER")
    void messages_inbox_ok() throws Exception {
        ensureUser("alice");
        mvc.perform(get("/messages/inbox")).andExpect(status().isOk());
    }
}
