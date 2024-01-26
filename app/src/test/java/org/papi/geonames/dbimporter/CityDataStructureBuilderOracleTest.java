package org.papi.geonames.dbimporter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import oracle.jdbc.datasource.impl.OracleDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.MountableFile;

/**
 * @author Plamen Uzunov
 */
public class CityDataStructureBuilderOracleTest {

    public static final String IMAGE_VERSION = "gvenzl/oracle-xe:21-slim-faststart";
    public static final int ORACLE_PORT = 11521;

    @Container
    private static OracleContainer container;

    @BeforeAll
    public static void setUpClass() {
        System.setProperty("skipCommunities", "true");
        container = new OracleContainer(IMAGE_VERSION)
            .withEnv("ORACLE_PASSWORD", "oracle")
            .withExposedPorts(1521, ORACLE_PORT)
            .withDatabaseName("geonames")
            .withUsername("geonames")
            .withPassword("oracle")
        ;
    }

//    @Test
    void testWithOracle() {
        URL url = this.getClass().getClassLoader().getResource("US.txt");
        assertNotNull(url);
        assertTrue(Paths.get(url.getFile()).toFile().exists());
        String inputFile = url.getFile();
        Path outputPath = CityDataStructureBuilder.create()
            .fromFile(inputFile)
            .withSqlDialect("oracle")
            .build()
            .save("GeoNamesOracle.sql");
        assertTrue(outputPath.toFile().exists());
        MountableFile mFile = MountableFile.forHostPath("GeoNamesOracle.sql");
        container.withCopyFileToContainer(mFile, "GeoNamesOracle.sql");
        container.start();
        container.waitingFor(Wait.forLogMessage("'.DATABASE IS READY TO USE!.\\s'", 1));

        try {
            String command = String.format("sqlplus /nolog <<EOF \n CONNECT %1$s/%2$s@XEPDB1 \n@/GeoNamesOracle.sql \ncommit;\nexit;\nEOF", container.getUsername(), container.getPassword());
            ExecResult res = container.execInContainer("sh", "-c", command);
            assertEquals(0, res.getExitCode());
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(CityDataStructureBuilderTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("Error Oracle test.");
        }

        // Test Oracle imported data
        try (Connection conn = getConnection(container)) {
            Statement stmt = conn.createStatement();
            ResultSet res = stmt.executeQuery("select sys_context('USERENV', 'CURRENT_SCHEMA') from dual");
            if (res.next()) {
                assertTrue("geonames".equalsIgnoreCase(res.getString(1)));
            }
            res.close();
            stmt.close();

            PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM states WHERE code=?");
            pstmt.setString(1, "LA");
            res = pstmt.executeQuery();
            if (res.next()) {
                assertEquals(1, res.getInt(1));
            } else {
                fail("Error Oracle test.");
            }
            res.close();
            pstmt.close();

            pstmt = conn.prepareStatement("SELECT COUNT(*) FROM cities WHERE postalCode=?");
            pstmt.setString(1, "46105");
            res = pstmt.executeQuery();
            if (res.next()) {
                assertEquals(1, res.getInt(1));
            } else {
                fail("Error Oracle test: There is no city in the database with postal code '46105'.");
            }
        } catch (SQLException | ClassNotFoundException ex) {
            Logger.getLogger(CityDataStructureBuilderTest.class.getName()).log(Level.SEVERE, null, ex);
            fail("Error Oracle test.");
        }

        container.stop();
    }

    protected Connection getConnection(JdbcDatabaseContainer<?> container) throws ClassNotFoundException, SQLException {
        OracleDataSource dataSource = new OracleDataSource();
        dataSource.setServerName("localhost");
        dataSource.setServiceName("XEPDB1");
        dataSource.setUser(container.getUsername());
        dataSource.setPassword(container.getPassword());
        dataSource.setDatabaseName("geonames");
        dataSource.setPortNumber(container.getFirstMappedPort());
        dataSource.setDriverType("thin");
        return dataSource.getConnection();
    }

}
