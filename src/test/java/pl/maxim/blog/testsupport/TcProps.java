package pl.maxim.blog.testsupport;

import org.springframework.test.context.DynamicPropertyRegistry;

public class TcProps {

    public static void register(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> PostgresTc.DB.getJdbcUrl());
        registry.add("spring.datasource.username", () -> PostgresTc.DB.getUsername());
        registry.add("spring.datasource.password", () -> PostgresTc.DB.getPassword());
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> "true");
    }
}
