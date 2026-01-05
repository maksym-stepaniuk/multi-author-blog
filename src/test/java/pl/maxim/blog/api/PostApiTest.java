package pl.maxim.blog.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import pl.maxim.blog.user.AppUser;
import pl.maxim.blog.user.AppUserRepository;
import pl.maxim.blog.user.Role;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class PostApiTest extends ApiIntegrationTestBase {

    @Autowired
    MockMvc mvc;

    @Autowired
    AppUserRepository userRepo;

    private Long ensureUser(String username) {
        return userRepo.findByUsername(username).map(AppUser::getId).orElseGet(() -> {
            AppUser u = new AppUser();
            u.setUsername(username);
            u.setEmail(username + "@ex.com");
            u.setPassword("x");
            u.setRole(Role.USER);
            userRepo.save(u);
            return u.getId();
        });
    }

    @Test
    void public_list_posts_ok() throws Exception {
        mvc.perform(get("/api/v1/posts"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "alice", roles = "USER")
    void create_post_ok() throws Exception {
        Long aliceId = ensureUser("alice");

        String body = """
                {"title":"T1","content":"<b>c</b>","authorIds":[%d]}
                """.formatted(aliceId);

        mvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("T1"));
    }

    @Test
    @WithMockUser(username = "bob", roles = "USER")
    void create_post_validation_400() throws Exception {
        Long bobId = ensureUser("bob");

        String body = """
                {"title":"","content":"","authorIds":[%d]}
                """.formatted(bobId);

        mvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void secure_endpoint_requires_auth() throws Exception {
        mvc.perform(get("/api/v1/messages/inbox"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin_db", roles = "ADMIN")
    void admin_users_list_ok() throws Exception {
        mvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isOk());
    }
}
