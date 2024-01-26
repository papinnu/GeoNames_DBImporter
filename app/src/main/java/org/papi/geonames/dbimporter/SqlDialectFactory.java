package org.papi.geonames.dbimporter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;

import org.papi.geonames.dbimporter.api.SQLDialectEnum;
import org.papi.geonames.dbimporter.api.spi.SqlDialect;

/**
 * @author Plamen Uzunov
 */
public class SqlDialectFactory {

    private static final SqlDialectFactory INSTANCE = new SqlDialectFactory();

    private final Map<SQLDialectEnum, SqlDialect> sqlDialects = new HashMap<>();

    public static SqlDialectFactory getInstance() {
        return INSTANCE;
    }

    public Optional<SqlDialect> lookup(SQLDialectEnum type) {
        return Optional.of(sqlDialects.get(type));
    }

    private SqlDialectFactory() {
        ServiceLoader<SqlDialect> loader = ServiceLoader.load(SqlDialect.class);
        loader.spliterator().forEachRemaining(sqlDialect-> sqlDialects.put(sqlDialect.getType(), sqlDialect));
    }
}
