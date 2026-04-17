package org.papi.geonames.dbimporter.impl;

import java.util.HashMap;
import java.util.Map;

import lombok.NonNull;
import org.papi.geonames.dbimporter.api.DivisionType;
import org.papi.geonames.dbimporter.api.SQLDialectEnum;
import org.papi.geonames.dbimporter.api.spi.SqlDialect;

/**
 * @author Plamen Uzunov
 */
public abstract class AbstractSqlDialect implements SqlDialect {

    private final SQLDialectEnum type;

    public AbstractSqlDialect(SQLDialectEnum type) {
        this.type = type;
    }

    protected final Map<DivisionType, String> createTableQueries = new HashMap<>(2);
    protected final Map<DivisionType, String> insertTableQueries = new HashMap<>(2);

    protected abstract String getDefaultCreateQuery();

    protected abstract String getDefaultInsertQuery();

    @Override
    public String getCreateTableQuery(@NonNull DivisionType type, @NonNull Object... args) {
        return getCreateTableQuery(type).formatted(args);
    }

    @Override
    public String getInsertTableQuery(@NonNull DivisionType type, Object... args) {
        return getInsertTableQuery(type).formatted(args);
    }

    @Override
    public SQLDialectEnum getType() {
        return type;
    }

    @Override
    public String prepareInsertStart(@NonNull String tableName) {
        return "";
    }

    @Override
    public String prepareInsertEnd(@NonNull String tableName) {
        return "";
    }

    protected void addCreateTableQuery(@NonNull DivisionType type, @NonNull String query) {
        createTableQueries.put(type, query);
    }

    protected void addInsertTableQuery(@NonNull DivisionType type, @NonNull String query) {
        insertTableQueries.put(type, query);
    }

    protected String getInsertTableQuery(@NonNull DivisionType type) {
        String query = insertTableQueries.get(type);
        return (query == null) ? getDefaultInsertQuery() : query;
    }

    protected String getCreateTableQuery(@NonNull DivisionType type) {
        String query = createTableQueries.get(type);
        return (query == null) ? getDefaultCreateQuery() : query;
    }
}
