package org.papi.geonames.dbimporter.api.spi;

import lombok.NonNull;
import org.papi.geonames.dbimporter.api.DivisionType;
import org.papi.geonames.dbimporter.api.SQLDialectEnum;

/**
 * @author Plamen Uzunov
 */
public interface SqlDialect {

    SQLDialectEnum getType();

    String getCreateTableQuery(@NonNull DivisionType type, @NonNull Object... args);

    String getInsertTableQuery(@NonNull DivisionType type, Object... args);

    String prepareInsertStart(@NonNull String tableName);
    String prepareInsertEnd(@NonNull String tableName);

}
