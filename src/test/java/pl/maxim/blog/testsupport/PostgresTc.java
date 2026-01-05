package pl.maxim.blog.testsupport;

import org.testcontainers.containers.PostgreSQLContainer;

public class PostgresTc {
    public static final PostgreSQLContainer<?> DB = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
}
