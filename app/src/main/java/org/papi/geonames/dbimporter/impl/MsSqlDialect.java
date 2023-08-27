package org.papi.geonames.dbimporter.impl;

import lombok.NonNull;
import org.papi.geonames.dbimporter.api.PlaceType;
import org.papi.geonames.dbimporter.api.SQLDialectEnum;
import org.papi.geonames.dbimporter.util.Utils;

/**
 * @author Plamen Uzunov
 */
public class MsSqlDialect extends AbstractSqlDialect {

    private static final String CREATE_STATE = "CREATE TABLE %1$s (ID INT NOT NULL IDENTITY PRIMARY KEY, %2$s VARCHAR(20) NOT NULL, %3$s VARCHAR(100) NOT NULL, CONSTRAINT UNI_%1$s_%2$s UNIQUE (%2$s));";
    private static final String CREATE_DEFAULT = "CREATE TABLE %1$s (ID INT NOT NULL IDENTITY PRIMARY KEY, %2$s INT NOT NULL, %3$s VARCHAR(20) NOT NULL, %4$s VARCHAR(100) NOT NULL, CONSTRAINT UNI_%1$s_%2$s_%3$s UNIQUE (%2$s, %3$s));";
    private static final String CREATE_CITY = "CREATE TABLE %1$s (ID INT NOT NULL IDENTITY PRIMARY KEY, stateId INT NOT NULL, countyId INT NOT NULL, %2$s INT, %3$s VARCHAR(20) NOT NULL, %4$s VARCHAR(100) NOT NULL, CONSTRAINT UNI_%1$s_SC_%3$s UNIQUE (stateId, countyId, %3$s));";

    private static final String INSERT_STATE = "INSERT INTO %1$s (ID, %2$s, %3$s) VALUES (%4$d, '%5$s', '%6$s');";
    private static final String INSERT_DEFAULT = "INSERT INTO %1$s (ID, %2$s, %3$s, %4$s) VALUES (%5$d, %6$d, '%7$s', '%8$s');";
    private static final String INSERT_CITY = "INSERT INTO %1$s (ID, stateId, countyId, %2$s, %3$s, %4$s) VALUES (%5$d, %6$d, %7$d, %8$d, '%9$s', '%10$s');";

    private static final String INSERT_START = "SET IDENTITY_INSERT %1$s ON;";
    private static final String INSERT_END = "SET IDENTITY_INSERT %1$s OFF;";


    public MsSqlDialect() {
        super(SQLDialectEnum.MSSQL);
        addCreateTableQuery(PlaceType.STATE, CREATE_STATE);
        addCreateTableQuery(PlaceType.CITY, CREATE_CITY);

        addInsertTableQuery(PlaceType.STATE, INSERT_STATE);
        addInsertTableQuery(PlaceType.CITY, INSERT_CITY);
    }

    @Override
    public String getInsertTableQuery(@NonNull PlaceType type, Object... args) {
        return Utils.normalizeQuery(getInsertTableQueryInternal(type), args);
    }

    @Override
    protected String getDefaultCreateQuery() {
        return CREATE_DEFAULT;
    }

    @Override
    protected String getDefaultInsertQuery() {
        return INSERT_DEFAULT;
    }

    @Override
    public String prepareInsertStart(@NonNull String tableName) {
        return INSERT_START.formatted(tableName);
    }

    @Override
    public String prepareInsertEnd(@NonNull String tableName) {
        return INSERT_END.formatted(tableName);
    }
}
