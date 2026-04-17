package org.papi.geonames.dbimporter.impl;

import org.papi.geonames.dbimporter.api.DivisionType;
import org.papi.geonames.dbimporter.api.SQLDialectEnum;

/**
 * @author Plamen Uzunov
 */
public class MysqlSqlDialect extends AbstractSqlDialect {

    private static final String CREATE_STATE = "CREATE TABLE IF NOT EXISTS %1$s (ID INT NOT NULL AUTO_INCREMENT, %2$s VARCHAR(20) NOT NULL, %3$s VARCHAR(100) NOT NULL, PRIMARY KEY (ID), UNIQUE (%2$s));";
    private static final String CREATE_DEFAULT = "CREATE TABLE IF NOT EXISTS %1$s (ID INT NOT NULL AUTO_INCREMENT, %2$s INT NOT NULL, %3$s VARCHAR(20) NOT NULL, %4$s VARCHAR(100) NOT NULL, PRIMARY KEY (ID), UNIQUE (%2$s, %3$s));";
    private static final String CREATE_CITY = "CREATE TABLE IF NOT EXISTS %1$s (ID INT NOT NULL AUTO_INCREMENT, %2$s INT NOT NULL, countyId INT, communityId INT, %3$s VARCHAR(20) NOT NULL, %4$s VARCHAR(100) NOT NULL, PRIMARY KEY (ID));";
    private static final String CREATE_COUNTRY = "CREATE TABLE IF NOT EXISTS %1$s (ID INT NOT NULL AUTO_INCREMENT, code VARCHAR(2) NOT NULL, name VARCHAR(50) NOT NULL, isoCode3 VARCHAR(3) NOT NULL, originalName VARCHAR(50), PRIMARY KEY (ID));";

    private static final String INSERT_STATE = "INSERT INTO %1$s (ID, %2$s, %3$s) VALUES (%4$d, \"%5$s\", \"%6$s\");";
    private static final String INSERT_DEFAULT = "INSERT INTO %1$s (ID, %2$s, %3$s, %4$s) VALUES (%5$d, %6$d, \"%7$s\", \"%8$s\");";
    private static final String INSERT_CITY = "INSERT INTO %1$s (ID, %2$s, countyId, communityId, %3$s, %4$s) VALUES (%5$d, %6$d, %7$d, %8$d, \"%9$s\", \"%10$s\");";
    private static final String INSERT_COUNTRY = "INSERT INTO %1$s (ID, code, name, isoCode3, originalName) VALUES (%2$d, \"%3$s\", \"%4$s\", \"%5$s\", \"%6$s\");";

    public MysqlSqlDialect() {
        super(SQLDialectEnum.MYSQL);
        addCreateTableQuery(DivisionType.STATE, CREATE_COUNTRY);
        addCreateTableQuery(DivisionType.CITY, CREATE_CITY);

        addInsertTableQuery(DivisionType.COUNTRY, INSERT_COUNTRY);
        addInsertTableQuery(DivisionType.CITY, INSERT_CITY);
    }

    @Override
    protected String getDefaultCreateQuery() {
        return CREATE_DEFAULT;
    }

    @Override
    protected String getDefaultInsertQuery() {
        return INSERT_DEFAULT;
    }

}
