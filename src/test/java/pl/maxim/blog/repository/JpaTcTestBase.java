package pl.maxim.blog.repository;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.maxim.blog.testsupport.PostgresTc;
import pl.maxim.blog.testsupport.TcProps;

@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class JpaTcTestBase {

    @BeforeAll
    static void start() {
        PostgresTc.DB.start();
    }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        TcProps.register(registry);
    }
}
