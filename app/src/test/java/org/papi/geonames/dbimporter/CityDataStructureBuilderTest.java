package org.papi.geonames.dbimporter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;
import org.papi.geonames.dbimporter.testcontainers.TestMySQLContainer;
import org.papi.geonames.dbimporter.testcontainers.TestPostgreSqlContainer;
import org.testcontainers.containers.Container.ExecResult;
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

    @Test
    void testWithMysql() {
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
        try (Connection conn = getMySqlConnection()) {
            PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM states WHERE code=?");
            pstmt.setString(1, "LA");
            ResultSet res = pstmt.executeQuery();
            if (res.next()) {
                assertEquals(1, res.getInt(1));
            } else {
                fail("Error MySQL test.");
            }

            pstmt = conn.prepareStatement("SELECT COUNT(*) FROM cities WHERE postalCode=?");
            pstmt.setString(1, "46105");
            res = pstmt.executeQuery();
            if (res.next()) {
                assertEquals(1, res.getInt(1));
            } else {
                fail("Error MySQL test.");
            }
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(CityDataStructureBuilderTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("Error MySQL test.");
            fail("Error MySQL test.");
        }

        mysqlContainer.stop();
    }

    @Test
    void testWithPostgreSql() {
        URL url = this.getClass().getClassLoader().getResource("BG.txt");
        assertNotNull(url);
        String inputFile = url.getFile();
        Path outputPath = CityDataStructureBuilder.create()
            .fromFile(inputFile)
            .withSqlDialect("postgresql")
            .build()
            .save("GeoNamesPostgre.sql");
        assertTrue(outputPath.toFile().exists());
        MountableFile mFile = MountableFile.forHostPath("GeoNamesPostgre.sql");
        postgresqlContainer.withCopyFileToContainer(mFile, "GeoNamesPostgre.sql");
        postgresqlContainer.start();

        try {
            String command = String.format("psql -h localhost -d geonames -U %1$s -f GeoNamesPostgre.sql", postgresqlContainer.getUsername());
            ExecResult res = postgresqlContainer.execInContainer("sh", "-c", command);
            assertEquals(0, res.getExitCode());
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(CityDataStructureBuilderTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("Error PostgreSQL test.");
        }

        // Test PostgreSql imported data
        try (Connection conn = getPostgreSqlConnection()) {
            PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM states WHERE code=?");
            pstmt.setString(1, "BGS");
            ResultSet res = pstmt.executeQuery();
            if (res.next()) {
                assertEquals(1, res.getInt(1));
            } else {
                fail("Error PostgreSQL test.");
            }

            pstmt = conn.prepareStatement("SELECT COUNT(*) FROM cities WHERE postalCode=?");
            pstmt.setString(1, "2873");
            res = pstmt.executeQuery();
            if (res.next()) {
                assertEquals(1, res.getInt(1));
            } else {
                fail("Error PostgreSQL test.");
            }
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(CityDataStructureBuilderTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("Error PostgreSQL test.");
            fail("Error PostgreSQL test.");
        }

        postgresqlContainer.stop();
    }

    private Connection getMySqlConnection() throws ClassNotFoundException, SQLException {
        String DATABASE_DRIVER = "com.mysql.cj.jdbc.Driver";
        String DATABASE_URL = "jdbc:mysql://localhost:%1$d/geonames".formatted(mysqlContainer.getFirstMappedPort());
        Properties properties = new Properties();
        properties.setProperty("user", mysqlContainer.getUsername());
        properties.setProperty("password", mysqlContainer.getPassword());
        Class.forName(DATABASE_DRIVER);
        return DriverManager.getConnection(DATABASE_URL, properties);
    }

    private Connection getPostgreSqlConnection() throws ClassNotFoundException, SQLException {
        String DATABASE_DRIVER = "org.postgresql.Driver";
        String DATABASE_URL = "jdbc:postgresql://localhost:%1$d/geonames".formatted(postgresqlContainer.getFirstMappedPort());
        Properties properties = new Properties();
        properties.setProperty("user", postgresqlContainer.getUsername());
        properties.setProperty("password", postgresqlContainer.getPassword());
        Class.forName(DATABASE_DRIVER);
        return DriverManager.getConnection(DATABASE_URL, properties);
    }

}