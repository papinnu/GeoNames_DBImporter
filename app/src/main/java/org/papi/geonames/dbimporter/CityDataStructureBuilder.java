package org.papi.geonames.dbimporter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import lombok.NonNull;
import org.papi.geonames.dbimporter.api.PlaceType;
import org.papi.geonames.dbimporter.api.SQLDialectEnum;
import org.papi.geonames.dbimporter.model.City;
import org.papi.geonames.dbimporter.model.Place;
import org.papi.geonames.dbimporter.spi.SqlDialect;

/**
 * @author Plamen Uzunov
 */
public class CityDataStructureBuilder {

    private String fileName;
    private SqlDialect sqlDialect = null;
    private final Map<String, Place> states = new HashMap<>();
    private final Map<String, Place> countries = new HashMap<>();
    private final Map<String, Place> communities = new HashMap<>();
    private final Map<String, City> cities = new HashMap<>();

    public static CityDataStructureBuilder create() {
        return new CityDataStructureBuilder();
    }

    public CityDataStructureBuilder fromFile(@NonNull String fileName) {
        this.fileName = fileName;
        return this;
    }

    public CityDataStructureBuilder withSqlDialect(String sqlDialect) {
        if (sqlDialect != null) {
            SQLDialectEnum sqlDialectEnum = SQLDialectEnum.lookup(sqlDialect);
            Optional<SqlDialect> dialect = SqlDialectFactory.getInstance().lookup(sqlDialectEnum);
            if(dialect.isPresent()) {
                this.sqlDialect = dialect.get();
            } else {
                throw new IllegalArgumentException(String.format("Provided sql dialect '%1$s'' does not exist.", sqlDialect));
            }
        }
        return this;
    }

    public CityDataStructureBuilder build() {
        Path path = Paths.get(fileName);
        try (Stream<String> lines = Files.lines(path)) {
            lines.forEach(this::parse);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    public Path save(@NonNull String sqlFile) {
        Path filePath = Path.of(sqlFile);
        // truncate the file if exists, otherwise creates a new empty file
        try {
            Files.write(filePath, new byte[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            writePlacesTable(PlaceType.STATE, states, writer);
            writePlacesTable(PlaceType.COUNTRY, countries, writer);
            writePlacesTable(PlaceType.COMMUNITY, communities, writer);
            writeCitiesTable(cities, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filePath;
    }

    private void writePlacesTable(PlaceType placeType, Map<String, Place> places, BufferedWriter writer) throws IOException {
        if (!states.isEmpty()) {
            writeCreateTable(placeType, writer);
            AtomicInteger index = new AtomicInteger(1);
            places.values().forEach(place -> writeWithException(index, placeType, place, writer));
        }
    }

    private void writeCitiesTable(Map<String, City> cities, BufferedWriter writer) throws IOException {
        if (!cities.isEmpty()) {
            writeCreateTable(PlaceType.CITY, writer);
            AtomicInteger index = new AtomicInteger(1);
            cities.values().forEach(place -> writeWithException(index, PlaceType.CITY, place, writer));
        }
    }

    private void parse(@NonNull String line) {
        try {
            String[] tokens = line.split("\\t");
            City city = new City(PlaceType.CITY, tokens[1], tokens[2]);
            Place state = states.get(tokens[4]);
            if (state == null) {
                state = new Place(PlaceType.STATE, tokens[4], tokens[3]);
                state.setId(states.size() + 1);
                states.put(state.getCode(), state);
            }
            city.setStateId(state.getId());

            Place country = countries.get(tokens[6]);
            if (country == null) {
                country = new Place(PlaceType.COUNTRY, tokens[6], tokens[5]);
                country.setId(countries.size() + 1);
                country.setParent(state);
                countries.put(country.getCode(), country);
            }
            city.setCountryId(country.getId());

            if (!tokens[8].isBlank() && tokens[7].isBlank()) {
                Place community = communities.get(tokens[8]);
                if (community == null) {
                    try {
                        community = new Place(PlaceType.COMMUNITY, tokens[8], tokens[7]);
                        community.setId(communities.size() + 1);
                        community.setParent(country);
                        communities.put(community.getCode(), community);
                    } catch (IllegalArgumentException ex) {
                        // skip community - can be NULL
                    }
                }
                city.setParent(community);
            }
            city.setId(cities.size() + 1);
            cities.put(city.getCode(), city);
        } catch (IllegalArgumentException ex) {
            // skip the current line
        }
    }

    private void writeCreateTable(@NonNull PlaceType placeType, BufferedWriter writer) throws IOException {
        if (placeType == PlaceType.STATE) {
            writer.write(sqlDialect.getCreateTableQuery(placeType,
                placeType.getTableName(),
                placeType.getCodeColumn(),
                placeType.getNameColumn()));
        } else {
            writer.write(sqlDialect.getCreateTableQuery(placeType,
                placeType.getTableName(),
                placeType.getParentColumn(),
                placeType.getCodeColumn(),
                placeType.getNameColumn()));
        }
        writer.newLine();
    }

    private void writeWithException(AtomicInteger index,
                                    PlaceType placeType,
                                    Place place,
                                    BufferedWriter writer) {
        try {
            writeInsertRow(index, placeType, place, writer);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    private void writeInsertRow(AtomicInteger index,
                                PlaceType placeType,
                                Place place,
                                BufferedWriter writer)
        throws IOException {
        place.setId(index.get());
        switch (placeType) {
            case STATE -> writer.write(sqlDialect.getInsertTableQuery(placeType,
                placeType.getTableName(),
                placeType.getCodeColumn(),
                placeType.getNameColumn(),
                place.getId(),
                place.getCode(),
                place.getName()));
            case CITY -> writer.write(sqlDialect.getInsertTableQuery(placeType,
                placeType.getTableName(),
                placeType.getParentColumn(),
                placeType.getCodeColumn(),
                placeType.getNameColumn(),
                place.getId(),
                ((City) place).getStateId(),
                ((City) place).getCountryId(),
                place.getParent() == null ? null : place.getParent().getId(),
                place.getCode(),
                place.getName()));
            default -> writer.write(sqlDialect.getInsertTableQuery(placeType,
                placeType.getTableName(),
                placeType.getParentColumn(),
                placeType.getCodeColumn(),
                placeType.getNameColumn(),
                place.getId(),
                place.getParent().getId(),
                place.getCode(),
                place.getName()));
        }
        writer.newLine();
        index.incrementAndGet();
    }

}
