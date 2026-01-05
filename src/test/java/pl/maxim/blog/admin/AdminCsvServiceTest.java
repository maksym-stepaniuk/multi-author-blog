package pl.maxim.blog.admin;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.maxim.blog.post.PostRepository;
import pl.maxim.blog.testsupport.PostgresTc;
import pl.maxim.blog.testsupport.TcProps;
import pl.maxim.blog.user.AppUserRepository;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
class AdminCsvServiceTest {

    @Autowired
    AdminCsvService csvService;

    @Autowired
    AppUserRepository userRepo;

    @Autowired
    PostRepository postRepo;

    @org.junit.jupiter.api.BeforeEach
    void clean() {
        postRepo.deleteAll();
        userRepo.deleteAll();
    }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        PostgresTc.DB.start();
        TcProps.register(registry);
    }

    @Test
    void import_users_creates_and_updates() {
        String csv = """
                username,email,password,role,enabled
                u1,u1@ex.com,pass,USER,true
                u2,u2@ex.com,,ADMIN,false
                """;
        MockMultipartFile file = new MockMultipartFile("file", "users.csv", "text/csv", csv.getBytes());

        var res1 = csvService.importUsers(file);
        assertThat(res1.created()).isEqualTo(2);
        assertThat(userRepo.findByUsername("u1")).isPresent();
        assertThat(userRepo.findByUsername("u2")).isPresent();

        String csv2 = """
                username,email,password,role,enabled
                u1,u1_new@ex.com,,ADMIN,true
                """;
        MockMultipartFile file2 = new MockMultipartFile("file", "users2.csv", "text/csv", csv2.getBytes());

        var res2 = csvService.importUsers(file2);
        assertThat(res2.updated()).isEqualTo(1);
        assertThat(userRepo.findByUsername("u1").orElseThrow().getEmail()).isEqualTo("u1_new@ex.com");
    }

    @Test
    void import_posts_and_export_posts() {
        String users = """
                username,email,password,role,enabled
                a1,a1@ex.com,pass,USER,true
                a2,a2@ex.com,pass,USER,true
                """;
        csvService.importUsers(new MockMultipartFile("file", "users.csv", "text/csv", users.getBytes()));

        String posts = """
                title,content,authors
                T1,<b>c</b>,a1;a2
                T2,text,a2
                """;
        var res = csvService.importPosts(new MockMultipartFile("file", "posts.csv", "text/csv", posts.getBytes()));
        assertThat(res.created()).isEqualTo(2);
        assertThat(postRepo.findAll()).hasSizeGreaterThanOrEqualTo(2);

        byte[] out = csvService.exportPostsCsv();
        String outStr = new String(out);
        assertThat(outStr).contains("id,title,content,authors");
        assertThat(outStr).contains("T1");
        assertThat(outStr).contains("T2");
    }
}
