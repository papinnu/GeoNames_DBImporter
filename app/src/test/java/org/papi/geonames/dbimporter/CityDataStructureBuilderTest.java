package org.papi.geonames.dbimporter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.papi.geonames.dbimporter.testcontainers.TestMySQLContainer;
import org.papi.geonames.dbimporter.testcontainers.TestPostgreSqlContainer;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.MountableFile;

/**
 * @author Plamen Uzunov
 */
class CityDataStructureBuilderTest {
    @Container
    private static final TestMySQLContainer mysqlContainer = TestMySQLContainer.getInstance();

    @Container
    private static final PostgreSQLContainer<TestPostgreSqlContainer> postgresqlContainer =
        TestPostgreSqlContainer.getInstance();

//    @Test
    void testWithMysql() {
        System.setProperty("skipCommunities", "true");
        URL url = this.getClass().getClassLoader().getResource("US.txt");
        assertNotNull(url);
        String inputFile = url.getFile();
        Path outputPath = CityDataStructureBuilder.create()
            .fromFile(inputFile)
            .withSqlDialect("mysql")
            .build()
            .save("GeoNamesMySQL.sql");
        assertTrue(outputPath.toFile().exists());
        MountableFile mFile = MountableFile.forHostPath("GeoNamesMySQL.sql");
        mysqlContainer.withCopyFileToContainer(mFile, "GeoNamesMySQL.sql");
        mysqlContainer.start();

        try {
            String command = "mysql -u%1$s -p%2$s geonames < GeoNamesMySQL.sql"
                .formatted(mysqlContainer.getUsername(), mysqlContainer.getPassword());
            ExecResult res = mysqlContainer.execInContainer("sh", "-c", command);
            assertEquals(0, res.getExitCode());
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(CityDataStructureBuilderTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("Error MySQL test.");
        }

        // Test MySQL imported data
        try (Connection conn = getConnection(mysqlContainer)) {
            PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM states WHERE code=?");
            pstmt.setString(1, "LA");
            ResultSet res = pstmt.executeQuery();
            if (res.next()) {
                assertEquals(1, res.getInt(1));
            } else {
                fail("Error MySQL test: There is no state in the database with code 'LA'.");
            }

            pstmt = conn.prepareStatement("SELECT COUNT(*) FROM cities WHERE postalCode=?");
            pstmt.setString(1, "46105");
            res = pstmt.executeQuery();
            if (res.next()) {
                assertEquals(1, res.getInt(1));
            } else {
                fail("Error MySQL test: There is no city in the database with postal code '46105'.");
            }
        } catch (SQLException ex) {
            Logger.getLogger(CityDataStructureBuilderTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("Error MySQL test.");
        }

        mysqlContainer.stop();
    }

    @Test
    void testWithPostgreSql() {
        System.setProperty("skipCommunities", "true");
        URL url = this.getClass().getClassLoader().getResource("BG.txt");
        assertNotNull(url);
        String inputFile = url.getFile();
        Path outputPath = CityDataStructureBuilder.create()
            .fromFile(inputFile)
            .withSqlDialect("postgresql")
            .build()
            .save("GeoNames_PostgreSQL.sql");
        assertTrue(outputPath.toFile().exists());
        MountableFile mFile = MountableFile.forHostPath("GeoNames_PostgreSQL.sql");
        postgresqlContainer.withCopyFileToContainer(mFile, "GeoNames_PostgreSQL.sql");
        postgresqlContainer.start();

        try {
            String command = String.format("psql -h localhost -d geonames -U %1$s -f GeoNames_PostgreSQL.sql", postgresqlContainer.getUsername());
            ExecResult res = postgresqlContainer.execInContainer("sh", "-c", command);
            assertEquals(0, res.getExitCode());
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(CityDataStructureBuilderTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("Error PostgreSQL test.");
        }

        // Test PostgreSql imported data
        try (Connection conn = getConnection(postgresqlContainer)) {
            PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM states WHERE code=?");
            pstmt.setString(1, "BGS");
            ResultSet res = pstmt.executeQuery();
            if (res.next()) {
                assertEquals(1, res.getInt(1));
            } else {
                fail("Error PostgreSQL test: There is no state in the database with code 'BGS'.");
            }

            pstmt = conn.prepareStatement("SELECT COUNT(*) FROM cities WHERE postalCode=?");
            pstmt.setString(1, "2873");
            res = pstmt.executeQuery();
            if (res.next()) {
                assertEquals(1, res.getInt(1));
            } else {
                fail("Error PostgreSQL test: There is no city in the database with postal code '2873'.");
            }
        } catch (SQLException ex) {
            Logger.getLogger(CityDataStructureBuilderTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("Error PostgreSQL test.");
        }

        postgresqlContainer.stop();
    }

    public Connection getConnection(JdbcDatabaseContainer<?> container) throws SQLException {
        return getDataSource(container).getConnection();
    }

    protected DataSource getDataSource(JdbcDatabaseContainer<?> container) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(container.getJdbcUrl());
        hikariConfig.setUsername(container.getUsername());
        hikariConfig.setPassword(container.getPassword());
        hikariConfig.setDriverClassName(container.getDriverClassName());
        return new HikariDataSource(hikariConfig);
    }

}