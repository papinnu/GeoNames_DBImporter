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

    public City(@NonNull PlaceType type, @NonNull String code, @NonNull String name) {
        super(type, code, name);
    }

    private int stateId;
    private int countryId;

}
