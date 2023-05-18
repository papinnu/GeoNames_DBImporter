package org.papi.geonames.dbimporter.util;

import lombok.NonNull;

/**
 * @author Plamen Uzunov
 */
public class Utils {

    public static String normalizeQuery(@NonNull String query, Object... args) {
        for (int i = 0; i < args.length; i++) {
            if(args[i] instanceof String) {
                args[i] = ((String)args[i]).replace("'", "''");
            }
        }
        return query.formatted(args);
    }
}
