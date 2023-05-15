package org.papi.geonames.dbimporter.impl;

import org.papi.geonames.dbimporter.api.PlaceType;
import org.papi.geonames.dbimporter.api.SQLDialectEnum;

/**
 * @author Plamen Uzunov
 */
public class MysqlSqlDialect extends AbstractSqlDialect {

    private static final String CREATE_STATE = "CREATE TABLE %1$s (ID INT NOT NULL AUTO_INCREMENT, %2$s VARCHAR(20) NOT NULL, %3$s VARCHAR(100) NOT NULL, PRIMARY KEY (ID), UNIQUE (%2$s));";
    private static final String CREATE_DEFAULT = "CREATE TABLE %1$s (ID INT NOT NULL AUTO_INCREMENT, %2$s INT NOT NULL, %3$s VARCHAR(20) NOT NULL, %4$s VARCHAR(100) NOT NULL, PRIMARY KEY (ID), UNIQUE (%2$s, %3$s));";
    private static final String CREATE_CITY = "CREATE TABLE %1$s (ID INT NOT NULL AUTO_INCREMENT, stateId INT NOT NULL, countryId INT NOT NULL, %2$s INT, %3$s VARCHAR(20) NOT NULL, %4$s VARCHAR(100) NOT NULL, PRIMARY KEY (ID), UNIQUE (stateId, countryId, %3$s));";

    private static final String INSERT_STATE = "INSERT INTO %1$s (ID, %2$s, %3$s) VALUES (%4$d, \"%5$s\", \"%6$s\");";
    private static final String INSERT_DEFAULT = "INSERT INTO %1$s (ID, %2$s, %3$s, %4$s) VALUES (%5$d, %6$d, \"%7$s\", \"%8$s\");";
    private static final String INSERT_CITY = "INSERT INTO %1$s (ID, stateId, countryId, %2$s, %3$s, %4$s) VALUES (%5$d, %6$d, %7$d, %8$d, \"%9$s\", \"%10$s\");";

    public MysqlSqlDialect() {
        super(SQLDialectEnum.MYSQL);
        addCreateTableQuery(PlaceType.STATE, CREATE_STATE);
        addCreateTableQuery(PlaceType.CITY, CREATE_CITY);

        addInsertTableQuery(PlaceType.STATE, INSERT_STATE);
        addInsertTableQuery(PlaceType.CITY, INSERT_CITY);
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
