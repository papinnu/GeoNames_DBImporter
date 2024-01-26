package org.papi.geonames.dbimporter.impl;

import java.util.HashMap;
import java.util.Map;

import lombok.NonNull;
import org.papi.geonames.dbimporter.api.PlaceType;
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

    protected final Map<PlaceType, String> createTableQueries = new HashMap<>(2);
    protected final Map<PlaceType, String> insertTableQueries = new HashMap<>(2);

    protected abstract String getDefaultCreateQuery();

    protected abstract String getDefaultInsertQuery();

    @Override
    public String getCreateTableQuery(@NonNull PlaceType type, @NonNull Object... args) {
        String skip = System.getProperty("skipCommunities");
        return (PlaceType.CITY == type && skip != null && !skip.isBlank())
            ? getDefaultCreateQuery().formatted(args)
            : getCreateTableQuery(type).formatted(args);
    }

    @Override
    public String getInsertTableQuery(@NonNull PlaceType type, Object... args) {
        return getInsertTableQueryInternal(type).formatted(args);
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

    protected String getInsertTableQueryInternal(@NonNull PlaceType type) {
        String skip = System.getProperty("skipCommunities");
        return (PlaceType.CITY == type && skip != null && !skip.isBlank())
            ? getDefaultInsertQuery()
            : getInsertTableQuery(type);
    }

    protected void addCreateTableQuery(@NonNull PlaceType type, @NonNull String query) {
        createTableQueries.put(type, query);
    }

    protected void addInsertTableQuery(@NonNull PlaceType type, @NonNull String query) {
        insertTableQueries.put(type, query);
    }

    protected String getInsertTableQuery(@NonNull PlaceType type) {
        String query = insertTableQueries.get(type);
        if (query == null) {
            query = getDefaultInsertQuery();
        }
        return query;
    }

    protected String getCreateTableQuery(@NonNull PlaceType type) {
        String query = createTableQueries.get(type);
        if (query == null) {
            query = getDefaultCreateQuery();
        }
        return query;
    }
}
