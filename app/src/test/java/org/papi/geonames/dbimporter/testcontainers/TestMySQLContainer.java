package org.papi.geonames.dbimporter.testcontainers;

import org.testcontainers.containers.MySQLContainer;

/**
 * @author Plamen Uzunov
 */
public class TestMySQLContainer extends MySQLContainer<TestMySQLContainer> {
    public static final String IMAGE_VERSION = "mysql";
    public static final int MYSQL_PORT = 33066;
    private static TestMySQLContainer container;

    private TestMySQLContainer(String dockerImageName) {
        super(dockerImageName);
    }

    /**
     * Returns a singleton {@code TestPostgreSqlContainer} instance.
     *
     * @return - singleton instance.
     */
    public static TestMySQLContainer getInstance() {
        if (container == null) {
            container = new TestMySQLContainer(IMAGE_VERSION)
                .withExposedPorts(3306, MYSQL_PORT)
                .withDatabaseName("geonames")
                .withUsername("test")
                .withPassword("1234567890")
                .withEnv("useSSL", "false")
                ;
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
