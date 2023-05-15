package org.papi.geonames.dbimporter.api;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Plamen Uzunov
 */
public enum SQLDialectEnum {

    MSSQL,
    MYSQL,
    POSTGRESQL,
    ORACLE,
    ;

    /**
     * Case-insensitive lookup for SQL dialect.
     * @param netName the SQL dialect name
     * @return - a corresponding {@code SQLDialect} for the given name, otherwise returns {@code null}.
     */
    public static SQLDialectEnum lookup(final String netName) {
        return INDEX_NAME.get(netName.toUpperCase());
    }

    private static final Map<String, SQLDialectEnum> INDEX_NAME = Stream.of(values())
        .collect(Collectors.toMap(SQLDialectEnum::name, Function.identity()));

}
