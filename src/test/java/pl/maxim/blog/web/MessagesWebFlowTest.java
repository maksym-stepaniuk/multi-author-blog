package pl.maxim.blog.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import pl.maxim.blog.api.ApiIntegrationTestBase;
import pl.maxim.blog.user.AppUser;
import pl.maxim.blog.user.AppUserRepository;
import pl.maxim.blog.user.Role;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class MessagesWebFlowTest extends ApiIntegrationTestBase {

    @Autowired MockMvc mvc;
    @Autowired AppUserRepository userRepo;

    private AppUser ensureUser(String username) {
        return userRepo.findByUsername(username).orElseGet(() -> {
            AppUser u = new AppUser();
            u.setUsername(username);
            u.setEmail(username + "@ex.com");
            u.setPassword("x");
            u.setRole(Role.USER);
            u.setEnabled(true);
            return userRepo.save(u);
        });
    }

    @Test
    @WithMockUser(username = "alice", roles = "USER")
    void inbox_sent_and_send_message() throws Exception {
        AppUser alice = ensureUser("alice");
        AppUser bob = ensureUser("bob");

        mvc.perform(get("/messages/inbox")).andExpect(status().isOk());
        mvc.perform(get("/messages/sent")).andExpect(status().isOk());

        mvc.perform(post("/messages")
                        .with(csrf())
                        .param("recipientId", bob.getId().toString())
                        .param("content", "hi"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/messages/sent"));

        mvc.perform(get("/messages/with/" + bob.getId()))
                .andExpect(status().isOk());

        mvc.perform(post("/messages/with/" + bob.getId())
                        .with(csrf())
                        .param("recipientId", bob.getId().toString())
                        .param("content", "next"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/messages/with/" + bob.getId()));
    }
}
