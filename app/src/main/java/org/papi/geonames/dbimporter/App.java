package org.papi.geonames.dbimporter;

import org.papi.geonames.dbimporter.api.PlaceType;

public class App {

    public static void main(String[] args) {
        if (args == null) {
            System.out.println("No arguments are passed.");
            System.exit(0);
        }
        if (args.length < 1) {
            System.out.println("Input file is missing!");
            System.exit(0);
        }

        String file = args[0];
        String sqlDialect = null; //TODO: implement default dialect to generate compatible SQL'99
        if (args.length > 1) {
            sqlDialect = args[1];
        }
        if(args.length > 2) {
            PlaceType.CITY.setTableName(args[2]);
        }
        if(args.length > 3) {
            PlaceType.STATE.setTableName(args[3]);
        }
        if(args.length > 4) {
            PlaceType.COUNTRY.setTableName(args[4]);
        }
        if(args.length > 5) {
            PlaceType.COMMUNITY.setTableName(args[5]);
        }

        CityDataStructureBuilder.create()
            .fromFile(file)
            .withSqlDialect(sqlDialect)
            .build()
            .save("GeoNames.sql");
    }

}
