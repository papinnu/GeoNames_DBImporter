package org.papi.geonames.dbimporter;

import java.util.Scanner;

import org.papi.geonames.dbimporter.api.DivisionType;

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

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the country ID");
        int countryId = Integer.parseInt(scanner.nextLine());

        // Prompt the user to use custom table name
        System.out.println("Do you want to customize table names? N/y");
        String customize = scanner.nextLine();
        boolean toCustomize = false;
        if (customize != null) {
            String trimed = customize.trim();
            toCustomize = trimed.length() == 1 && "Y".equalsIgnoreCase(trimed);
        }

        if (toCustomize) {
            System.out.println("Enter the cities table name:");
            DivisionType.CITY.setTableName(scanner.nextLine());

            System.out.println("Enter the counties table name:");
            DivisionType.COUNTY.setTableName(scanner.nextLine());

            System.out.println("Enter the communities table name:");
            DivisionType.COMMUNITY.setTableName(scanner.nextLine());

            System.out.println("Enter the states table name:");
            DivisionType.STATE.setTableName(scanner.nextLine());
        }

        System.out.println("Do you want to customize the starting sequence of table indexes? N/y");
        String customizeSequenceIndexes = scanner.nextLine();
        boolean customSequence = false;
        if (customizeSequenceIndexes != null) {
            String trimed = customizeSequenceIndexes.trim();
            customSequence = trimed.length() == 1 && "Y".equalsIgnoreCase(trimed);
        }
        int citiesStartIndex = 1;
        int communitiesStartIndex = 1;
        int countiesStartIndex = 1;
        int statesStartIndex = 1;
        if (customSequence) {
            System.out.printf("Enter the starting index sequence for the %1$s table:%n", DivisionType.CITY.getTableName());
            citiesStartIndex = scanner.nextInt();

            System.out.printf("Enter the starting index sequence for the %1$s table:%n", DivisionType.COMMUNITY.getTableName());
            communitiesStartIndex = scanner.nextInt();

            System.out.printf("Enter the starting index sequence for the %1$s table:%n", DivisionType.COUNTY.getTableName());
            countiesStartIndex = scanner.nextInt();

            System.out.printf("Enter the starting index sequence for the %1$s table:%n", DivisionType.STATE.getTableName());
            statesStartIndex = scanner.nextInt();
        }

        DivisionsDataStructureBuilder.create()
            .fromFile(file)
            .withSqlDialect(sqlDialect)
            .withCountryId(countryId)
            .withCitiesIndex(citiesStartIndex)
            .withCommunitiesIndex(communitiesStartIndex)
            .withCountiesIndex(countiesStartIndex)
            .withStatesIndex(statesStartIndex)
            .buildStructure()
            .save(sqlDialect + ".sql");
    }

}
