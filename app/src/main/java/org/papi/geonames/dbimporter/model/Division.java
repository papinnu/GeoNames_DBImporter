package org.papi.geonames.dbimporter.model;

import java.util.Objects;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.papi.geonames.dbimporter.api.DivisionType;

/**
 * @author Plamen Uzunov
 */
@Getter
@Setter
public class Division {

    private final DivisionType type;
    private final String code;
    private final String name;
    private Division parent;
    private int Id;

    public Division(@NonNull DivisionType type, @NonNull String code, @NonNull String name) {
        if (code.isBlank() || name.isBlank()) {
            throw new IllegalArgumentException(String.format("Code and name place cannot be blank for %s!", type.name()));
        }
        this.code = code;
        this.name = name;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Division place = (Division) o;
        return type == place.type && code.equals(place.code) && name.equals(place.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, code, name);
    }

}
