package org.papi.geonames.dbimporter.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.papi.geonames.dbimporter.api.PlaceType;

/**
 * @author Plamen Uzunov
 */
@Getter
@Setter
public class City extends Place {

    public City(@NonNull String code, @NonNull String name) {
        super(PlaceType.CITY, code, name);
    }

}
