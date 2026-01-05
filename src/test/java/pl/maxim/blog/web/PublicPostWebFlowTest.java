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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class PublicPostWebFlowTest extends ApiIntegrationTestBase {

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
        p.setTitle("T");
        p.setContent("<b>HTML</b>");
        p.setAuthors(Set.of(author));
        return postRepo.save(p).getId();
    }

    @Test
    @WithMockUser(username = "alice", roles = "USER")
    void add_comment_invalid_returns_ok_page() throws Exception {
        AppUser alice = ensureUser("alice", Role.USER);
        Long postId = createPost(alice);

        mvc.perform(post("/posts/" + postId + "/comments")
                        .with(csrf())
                        .param("content", ""))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "alice", roles = "USER")
    void add_comment_valid_redirects() throws Exception {
        AppUser alice = ensureUser("alice", Role.USER);
        Long postId = createPost(alice);

        mvc.perform(post("/posts/" + postId + "/comments")
                        .with(csrf())
                        .param("content", "hello"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/" + postId));
    }

    @Test
    @WithMockUser(username = "alice", roles = "USER")
    void rate_invalid_returns_ok_page() throws Exception {
        AppUser alice = ensureUser("alice", Role.USER);
        Long postId = createPost(alice);

        mvc.perform(post("/posts/" + postId + "/rating")
                        .with(csrf())
                        .param("value", ""))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "alice", roles = "USER")
    void rate_valid_redirects() throws Exception {
        AppUser alice = ensureUser("alice", Role.USER);
        Long postId = createPost(alice);

        mvc.perform(post("/posts/" + postId + "/rating")
                        .with(csrf())
                        .param("value", "5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/" + postId));
    }

    @Test
    @WithMockUser(username = "alice", roles = "USER")
    void message_author_redirects() throws Exception {
        AppUser alice = ensureUser("alice", Role.USER);
        AppUser bob = ensureUser("bob", Role.USER);
        Long postId = createPost(alice);

        mvc.perform(post("/posts/" + postId + "/message")
                        .with(csrf())
                        .param("recipientId", bob.getId().toString())
                        .param("content", "hey"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/" + postId));
    }
}
