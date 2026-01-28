package ru.otus.base;

import org.testcontainers.containers.PostgreSQLContainer;

public class TestContainersConfig {

    public static class CustomPostgreSQLContainer extends PostgreSQLContainer<CustomPostgreSQLContainer> {

        private static final String IMAGE_VERSION = "postgres:16"; // лучше 16, а не 12

        public CustomPostgreSQLContainer() {
            super(IMAGE_VERSION);
            withDatabaseName("demoDB");
            withUsername("usr");
            withPassword("pwd");
        }
    }
}
