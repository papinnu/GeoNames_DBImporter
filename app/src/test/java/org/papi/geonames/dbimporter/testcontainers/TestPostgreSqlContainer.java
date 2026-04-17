package org.papi.geonames.dbimporter.testcontainers;

import org.testcontainers.containers.PostgreSQLContainer;

//@Testcontainers
public class TestPostgreSqlContainer extends PostgreSQLContainer<TestPostgreSqlContainer> {
    public static final String IMAGE_VERSION = "postgres";
    public static final int POSTGRES_PORT = 54322;
    private static TestPostgreSqlContainer container;

    private TestPostgreSqlContainer(String dockerImageName) {
        super(dockerImageName);
    }

    /**
     * Returns a singleton {@code TestPostgreSqlContainer} instance.
     *
     * @return - singleton instance.
     */
    public static TestPostgreSqlContainer getInstance() {
        if (container == null) {
            container = new TestPostgreSqlContainer(IMAGE_VERSION)
                .withDatabaseName("geonames")
                .withUsername("test")
                .withPassword("1234567890")
                .withEnv("PGPASSWORD", "1234567890")
            ;
            container.addFixedExposedPort(POSTGRES_PORT, 5432);
        }

        return container;
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void stop() {
    }
}

