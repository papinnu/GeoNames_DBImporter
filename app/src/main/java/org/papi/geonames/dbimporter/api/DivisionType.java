package org.papi.geonames.dbimporter.api;

import lombok.Getter;

/**
 * @author Plamen Uzunov
 */
@Getter
public enum DivisionType {

    COUNTRY("countries", null, "code", "name"),
    STATE("states", "countryId", "code", "name"),
    COUNTY("counties", "stateId", "code", "name"),
    COMMUNITY("communities", "countyId", "code", "name"),
    CITY("cities", "stateId", "postalCode", "name"),
    ;

    private final String tableName;
    private final String parentColumn;
    private final String codeColumn;
    private final String nameColumn;

    DivisionType(String tableName, String parentColumn, String codeColumn, String nameColumn) {
        this.tableName = tableName;
        this.parentColumn = parentColumn;
        this.codeColumn = codeColumn;
        this.nameColumn = nameColumn;
    }

}
