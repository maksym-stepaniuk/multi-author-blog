package pl.maxim.blog.api;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.maxim.blog.testsupport.PostgresTc;
import pl.maxim.blog.testsupport.TcProps;

@Testcontainers
@SpringBootTest
public abstract class ApiIntegrationTestBase {

    @BeforeAll
    static void start() {
        PostgresTc.DB.start();
    }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        TcProps.register(registry);
    }
}
