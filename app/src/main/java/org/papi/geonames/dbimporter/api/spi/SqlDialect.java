package org.papi.geonames.dbimporter.api.spi;

import lombok.NonNull;
import org.papi.geonames.dbimporter.api.PlaceType;
import org.papi.geonames.dbimporter.api.SQLDialectEnum;

/**
 * @author Plamen Uzunov
 */
public interface SqlDialect {

    SQLDialectEnum getType();

    String getCreateTableQuery(@NonNull PlaceType type, @NonNull Object... args);

    String getInsertTableQuery(@NonNull PlaceType type, Object... args);

    String prepareInsertStart(@NonNull String tableName);
    String prepareInsertEnd(@NonNull String tableName);

}
