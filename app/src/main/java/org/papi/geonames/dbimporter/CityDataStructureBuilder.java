package org.papi.geonames.dbimporter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
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
    private final Map<String, Place> counties = new HashMap<>();
    private final Map<String, Place> communities = new HashMap<>();
    private final Map<String, City> cities = new HashMap<>();

    private boolean withCommunities = true;

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
            if (dialect.isPresent()) {
                this.sqlDialect = dialect.get();
            } else {
                throw new IllegalArgumentException(String.format("Provided sql dialect '%1$s'' does not exist.", sqlDialect));
            }
        }
        return this;
    }

    public CityDataStructureBuilder build() {
        String skip = System.getProperty("skipCommunities");
        if (skip != null && !skip.isBlank()) {
            withCommunities = false;
        }

        Path path = Paths.get(fileName);
        try (Stream<String> lines = Files.lines(path)) {
            lines.forEach(this::parse);
        } catch (IOException ex) {
            Logger.getLogger(CityDataStructureBuilder.class.getName()).throwing(CityDataStructureBuilder.class.getName(), "build", ex);
        }
        return this;
    }

    public Path save(@NonNull String sqlFile) {
        Path filePath = Path.of(sqlFile);
        // truncate the file if exists, otherwise creates a new empty file
        try {
            Files.write(filePath, new byte[0]);
        } catch (IOException ex) {
            Logger.getLogger(CityDataStructureBuilder.class.getName()).throwing(CityDataStructureBuilder.class.getName(), "save", ex);
        }
        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            writePlacesTable(PlaceType.STATE, states, writer);
            writePlacesTable(PlaceType.COUNTY, counties, writer);
            if (withCommunities) {
                writePlacesTable(PlaceType.COMMUNITY, communities, writer);
            }
            writeCitiesTable(cities, writer);
        } catch (IOException ex) {
            Logger.getLogger(CityDataStructureBuilder.class.getName()).throwing(CityDataStructureBuilder.class.getName(), "save", ex);
        }
        return filePath;
    }

    private void writePlacesTable(PlaceType placeType, Map<String, Place> places, BufferedWriter writer) throws IOException {
        if (!states.isEmpty()) {
            writeCreateTable(placeType, writer);
            writer.write(sqlDialect.prepareInsertStart(placeType.getTableName()));
            writer.newLine();
            AtomicInteger index = new AtomicInteger(1);
            places.values().forEach(place -> writeWithException(index, placeType, place, writer));
            writer.write(sqlDialect.prepareInsertEnd(placeType.getTableName()));
            writer.newLine();
        }
    }

    private void writeCitiesTable(Map<String, City> cities, BufferedWriter writer) throws IOException {
        if (!cities.isEmpty()) {
            writeCreateTable(PlaceType.CITY, writer);
            writer.write(sqlDialect.prepareInsertStart(PlaceType.CITY.getTableName()));
            writer.newLine();
            AtomicInteger index = new AtomicInteger(1);
            cities.values().forEach(place -> writeWithException(index, PlaceType.CITY, place, writer));
            writer.write(sqlDialect.prepareInsertEnd(PlaceType.CITY.getTableName()));
            writer.newLine();
        }
    }

    private void parse(@NonNull String line) {
        try {
            String[] tokens = line.split("\\t");
            City city = new City(tokens[1], tokens[2]);
            Place state = states.get(tokens[4]);
            if (state == null) {
                state = new Place(PlaceType.STATE, tokens[4], tokens[3]);
                state.setId(states.size() + 1);
                states.put(state.getCode(), state);
            }

            String key = state.getCode() + "-" + tokens[6];
            Place county = counties.get(key);
            if (county == null) {
                county = new Place(PlaceType.COUNTY, tokens[6], tokens[5]);
                county.setId(counties.size() + 1);
                county.setParent(state);
                counties.put(key, county);
            }

            if (withCommunities) {
                if (tokens[8].isBlank() || !tokens[7].isBlank()) {
                    Logger.getLogger(CityDataStructureBuilder.class.getName()).warning("Community is null for city:["+city+"]");
                    return;
                }
                key += key + "-" + tokens[8];
                Place community = communities.get(key);
                if (community == null) {
                    try {
                        community = new Place(PlaceType.COMMUNITY, tokens[8], tokens[7]);
                        community.setId(communities.size() + 1);
                        community.setParent(county);
                        communities.put(key, community);
                    } catch (IllegalArgumentException ex) {
                        // skip community - can be NULL
                    }
                }
                city.setParent(community);

            } else {
                city.setParent(county);
            }
            if(!city.getParent().getParent().getCode().equals(state.getCode())) {
                Logger.getLogger(CityDataStructureBuilder.class.getName()).warning("Parents mismatched for city:["+city+"]");
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
        if (Objects.requireNonNull(placeType) == PlaceType.STATE) {
            writer.write(sqlDialect.getInsertTableQuery(placeType,
                placeType.getTableName(),
                placeType.getCodeColumn(),
                placeType.getNameColumn(),
                place.getId(),
                place.getCode(),
                place.getName()));
        } else {
            writer.write(sqlDialect.getInsertTableQuery(placeType,
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
