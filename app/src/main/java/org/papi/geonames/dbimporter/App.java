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
        if(args.length > 2 && "skipCommunities".equalsIgnoreCase(args[2])) {
            System.setProperty("skipCommunities", "true");
        }
        if(args.length > 3) {
            PlaceType.CITY.setTableName(args[3]);
        }
        if(args.length > 4) {
            PlaceType.STATE.setTableName(args[4]);
        }
        if(args.length > 5) {
            PlaceType.COUNTY.setTableName(args[5]);
        }
        if(args.length > 6) {
            PlaceType.COMMUNITY.setTableName(args[6]);
        }

        CityDataStructureBuilder.create()
            .fromFile(file)
            .withSqlDialect(sqlDialect)
            .build()
            .save("GeoNames.sql");
    }

}
