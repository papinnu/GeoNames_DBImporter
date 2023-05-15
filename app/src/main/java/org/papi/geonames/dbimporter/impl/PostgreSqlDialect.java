package org.papi.geonames.dbimporter.impl;

import lombok.NonNull;
import org.papi.geonames.dbimporter.api.PlaceType;
import org.papi.geonames.dbimporter.api.SQLDialectEnum;

/**
 * @author Plamen Uzunov
 */
public class PostgreSqlDialect extends AbstractSqlDialect {

    private static final String CREATE_STATE = "CREATE TABLE %1$s (ID SERIAL PRIMARY KEY, %2$s VARCHAR(20) NOT NULL, %3$s VARCHAR(100) NOT NULL, CONSTRAINT constraint_%2$s UNIQUE (%2$s));";
    private static final String CREATE_DEFAULT = "CREATE TABLE %1$s (ID SERIAL PRIMARY KEY, %2$s INT NOT NULL, %3$s VARCHAR(20) NOT NULL, %4$s VARCHAR(100) NOT NULL, CONSTRAINT constraint_%2$s_%3$s UNIQUE (%2$s, %3$s));";
    private static final String CREATE_CITY = "CREATE TABLE %1$s (ID SERIAL PRIMARY KEY, stateId INT NOT NULL, countyId INT NOT NULL, %2$s INT, %3$s VARCHAR(20) NOT NULL, %4$s VARCHAR(100) NOT NULL, CONSTRAINT constraint_mapping UNIQUE (stateId, countyId, %3$s));";

    private static final String INSERT_STATE = "INSERT INTO %1$s (ID, %2$s, %3$s) VALUES (%4$d, '%5$s', '%6$s');";
    private static final String INSERT_DEFAULT = "INSERT INTO %1$s (ID, %2$s, %3$s, %4$s) VALUES (%5$d, %6$d, '%7$s', '%8$s');";
    private static final String INSERT_CITY = "INSERT INTO %1$s (ID, stateId, countyId, %2$s, %3$s, %4$s) VALUES (%5$d, %6$d, %7$d, %8$d, '%9$s', '%10$s');";

    public PostgreSqlDialect() {
        super(SQLDialectEnum.POSTGRESQL);
        addCreateTableQuery(PlaceType.STATE, CREATE_STATE);
        addCreateTableQuery(PlaceType.CITY, CREATE_CITY);

        addInsertTableQuery(PlaceType.STATE, INSERT_STATE);
        addInsertTableQuery(PlaceType.CITY, INSERT_CITY);
    }

    @Override
    public String getInsertTableQuery(@NonNull PlaceType type, Object... args) {
        String query = getInsertTableQuery(type);
        for (int i = 0; i < args.length; i++) {
            if(args[i] instanceof String) {
                args[i] = ((String)args[i]).replace("'", "''");
            }
        }
        return query.formatted(args);
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
