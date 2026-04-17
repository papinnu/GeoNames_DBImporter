package org.papi.geonames.dbimporter.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.papi.geonames.dbimporter.api.DivisionType;

/**
 * @author Plamen Uzunov
 */
@Getter
@Setter
public class Country extends Division {

    private String code3;
    private String originalName;

    public Country(@NonNull String code, @NonNull String name) {
        super(DivisionType.COUNTRY, code, name);
    }

}
