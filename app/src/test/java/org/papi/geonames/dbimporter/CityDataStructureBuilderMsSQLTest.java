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

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

/**
 * @author Plamen Uzunov
 */
public class CityDataStructureBuilderMsSQLTest {

//    public static final String IMAGE_NAME = "mssql/server:2017-CU12"; //DockerImageName MSSQL_SERVER_IMAGE = DockerImageName.parse("mcr.microsoft.com/mssql/server:2017-CU12");
//    public static final int MSSQL_PORT = 11433;
//    @Container
//    private static MSSQLServerContainer container;
//
//    @BeforeAll
//    public static void setUpClass() {
//        System.setProperty("skipCommunities", "true");
//        DockerImageName MSSQL_SERVER_IMAGE = DockerImageName.parse("mcr.microsoft.com/mssql/server:2017-CU12");
//        container = new MSSQLServerContainer<>(MSSQL_SERVER_IMAGE)
//            .acceptLicense()
//            .withEnv("MSSQL_SA_PASSWORD", "1234567890")
////            .withUsername("mssql")
////            .withDatabaseName("geonames")
//            .withExposedPorts(1433, MSSQL_PORT)
//        ;
//    }
//
////    @Test
//    void testWith_MS_SQL() {
//
//        URL url = this.getClass().getClassLoader().getResource("US.txt");
//        assertNotNull(url);
//        assertTrue(Paths.get(url.getFile()).toFile().exists());
//        String inputFile = url.getFile();
//        Path outputPath = CityDataStructureBuilder.create()
//            .fromFile(inputFile)
//            .withSqlDialect("mssql")
//            .build()
//            .save("GeoNamesMsSQL.sql");
//        assertTrue(outputPath.toFile().exists());
//        MountableFile mFile = MountableFile.forHostPath("GeoNamesMsSQL.sql");
//        container.withCopyFileToContainer(mFile, "GeoNamesMsSQL.sql");
//        container.start();
////        container.waitingFor(Wait.forLogMessage("'.DATABASE IS READY TO USE!.\\s'", 1));
//
//        try {
//            //sqlcmd -S SERVERNAME\INSTANCE_NAME -i C:\path\mysqlfile.sql -o C:\path\output_file.txt
//            //sqlcmd -S SERVERNAME -d MYDATABASE -U USERNAME -P PASSWORD -i C:\path\mysqlfile.sql -o C:\path\results.txt
//            //sqlcmd -S "[SERVER NAME]" -d [DATABASE NAME] -i .\[SCRIPT].sql
//            //sqlcmd -S "[SERVER NAME]" -d geonames -i .\[SCRIPT].sql
//
//            String command = String.format("sqlcmd -S localhost -d geonames -U %1$s -P %2$s -i /GeoNamesOracle.sql -o /import_result.txt", container.getUsername(), container.getPassword());
//            org.testcontainers.containers.Container.ExecResult res = container.execInContainer("sh", "-c", command);
//            assertEquals(0, res.getExitCode());
//        } catch (IOException | InterruptedException ex) {
//            Logger.getLogger(CityDataStructureBuilderTest.class.getName()).log(Level.SEVERE, null, ex);
//            fail("Error Oracle test.");
//        }
//
//        // Test Oracle imported data
//        try (Connection conn = getConnection(container)) {
//            Statement stmt = conn.createStatement();
//            ResultSet res = stmt.executeQuery("select sys_context('USERENV', 'CURRENT_SCHEMA') from dual");
//            if (res.next()) {
//                assertTrue("geonames".equalsIgnoreCase(res.getString(1)));
//            }
//            res.close();
//            stmt.close();
//
//            PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) FROM states WHERE code=?");
//            pstmt.setString(1, "LA");
//            res = pstmt.executeQuery();
//            if (res.next()) {
//                assertEquals(1, res.getInt(1));
//            } else {
//                fail("Error Oracle test.");
//            }
//            res.close();
//            pstmt.close();
//
//            pstmt = conn.prepareStatement("SELECT COUNT(*) FROM cities WHERE postalCode=?");
//            pstmt.setString(1, "46105");
//            res = pstmt.executeQuery();
//            if (res.next()) {
//                assertEquals(1, res.getInt(1));
//            } else {
//                fail("Error Oracle test: There is no city in the database with postal code '46105'.");
//            }
//        } catch (SQLException | ClassNotFoundException ex) {
//            Logger.getLogger(CityDataStructureBuilderTest.class.getName()).log(Level.SEVERE, null, ex);
//            fail("Error Oracle test.");
//        }
//
//        container.stop();
//    }
//
//    protected Connection getConnection(JdbcDatabaseContainer<?> container) throws ClassNotFoundException, SQLException {
//
////        datasource:
////        url: "jdbc:sqlserver://localhost:33333"
////        username: SA
////        password: A_Str0ng_Required_Password
////        driver-class-name: com.microsoft.sqlserver.jdbc.SQLServerDriver
//
////        "jdbc:tc:sqlserver:2017-CU12://hostname:hostport;databaseName=databasename",
//
//        HikariConfig hikariConfig = new HikariConfig();
//        hikariConfig.setJdbcUrl(container.getJdbcUrl());
//        hikariConfig.setUsername(container.getUsername());
//        hikariConfig.setPassword(container.getPassword());
//        hikariConfig.setDriverClassName(container.getDriverClassName());
//        return new HikariDataSource(hikariConfig).getConnection();
//
//    }

}
