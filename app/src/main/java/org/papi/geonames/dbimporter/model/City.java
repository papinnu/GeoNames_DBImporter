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
public class City extends Division {

    public City(@NonNull String code, @NonNull String name) {
        super(DivisionType.CITY, code, name);
    }

    private Division community;
    private Division county;

}
