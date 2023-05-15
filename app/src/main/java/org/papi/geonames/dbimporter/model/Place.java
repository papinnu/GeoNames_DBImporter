package org.papi.geonames.dbimporter.model;

import java.util.Objects;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.papi.geonames.dbimporter.api.PlaceType;

/**
 * @author Plamen Uzunov
 */
@Getter
@Setter
public class Place {

    private final PlaceType type;
    private final String code;
    private final String name;
    private Place parent;
    private int Id;

    public Place(@NonNull PlaceType type, @NonNull String code, @NonNull String name) {
        if (code.isBlank() || name.isBlank()) {
            throw new IllegalArgumentException("Code and name place cannot be blank!");
        }
        this.code = code;
        this.name = name;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Place place = (Place) o;
        return type == place.type && code.equals(place.code) && name.equals(place.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, code, name);
    }

}
