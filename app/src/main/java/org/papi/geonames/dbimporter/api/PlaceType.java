package org.papi.geonames.dbimporter.api;

/**
 * @author Plamen Uzunov
 */
public enum PlaceType {

    STATE("states", null, "code", "name"),
    COUNTY("counties", "stateId", "code", "name"),
    COMMUNITY("communities", "countyId", "code", "name"),
    CITY("cities", "communityId", "postalCode", "name"),
    ;

    private String tableName;
    private final String parentColumn;
    private final String codeColumn;
    private final String nameColumn;

    PlaceType(String tableName, String parentColumn, String codeColumn, String nameColumn) {
        this.tableName = tableName;
        this.parentColumn = parentColumn;
        this.codeColumn = codeColumn;
        this.nameColumn = nameColumn;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

//    public void setParentColumn(String parentColumn) {
//        this.parentColumn = parentColumn;
//    }

    public String getTableName() {
        return tableName;
    }

    public String getParentColumn() {
        return parentColumn;
    }

    public String getCodeColumn() {
        return codeColumn;
    }

    public String getNameColumn() {
        return nameColumn;
    }
}
