package org.papi.geonames.dbimporter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Stream;

import lombok.NonNull;
import org.papi.geonames.dbimporter.api.DivisionType;
import org.papi.geonames.dbimporter.api.SQLDialectEnum;
import org.papi.geonames.dbimporter.api.spi.SqlDialect;
import org.papi.geonames.dbimporter.model.City;
import org.papi.geonames.dbimporter.model.Country;
import org.papi.geonames.dbimporter.model.Division;

/**
 * @author Plamen Uzunov
 */
public class DivisionsDataStructureBuilder {

    private static final Logger LOG = Logger.getLogger(DivisionsDataStructureBuilder.class.getName());

    private String fileName;
    private SqlDialect sqlDialect = null;
    private final Map<String, Division> states = new HashMap<>();
    private final Map<String, Division> counties = new HashMap<>();
    private final Map<String, Division> communities = new HashMap<>();
    private final Map<String, City> cities = new HashMap<>();

    private int countryId = -1;
    private Country country;

    private AtomicInteger citiesIndex = null;
    private AtomicInteger communitiesIndex = null;
    private AtomicInteger countiesIndex = null;
    private AtomicInteger statesIndex = null;

    public static DivisionsDataStructureBuilder create() {
        return new DivisionsDataStructureBuilder();
    }

    public DivisionsDataStructureBuilder fromFile(@NonNull String fileName) {
        this.fileName = fileName;
        return this;
    }

    public DivisionsDataStructureBuilder withSqlDialect(String sqlDialect) {
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

    public DivisionsDataStructureBuilder withCountryId(int countryId) {
        this.countryId = countryId;
        return this;
    }

    private Country findCountry(String countryCode) {
        Country res = null;
        for (Locale l : Locale.getAvailableLocales()) {
            if (l.getCountry().equals(countryCode)) {
                res = new Country(countryCode, l.getDisplayCountry());
                res.setOriginalName(l.getDisplayCountry(l));
                res.setCode3(l.getISO3Country());
                if (countryId > 0) {
                    res.setId(countryId);
                }
                break;
            }
        }
        return res;
    }

    public DivisionsDataStructureBuilder withCitiesIndex(int citiesIndex) {
        this.citiesIndex = new AtomicInteger(citiesIndex);
        return this;
    }

    public DivisionsDataStructureBuilder withCommunitiesIndex(int communitiesIndex) {
        this.communitiesIndex = new AtomicInteger(communitiesIndex);
        return this;
    }

    public DivisionsDataStructureBuilder withCountiesIndex(int countiesIndex) {
        this.countiesIndex = new AtomicInteger(countiesIndex);
        return this;
    }

    public DivisionsDataStructureBuilder withStatesIndex(int statesIndex) {
        this.statesIndex = new AtomicInteger(statesIndex);
        return this;
    }

    public DivisionsDataStructureBuilder buildStructure() {
        Path path = Paths.get(fileName);
        try (Stream<String> lines = Files.lines(path)) {
            lines.forEach(this::parse);
        } catch (IOException ex) {
            LOG.throwing(DivisionsDataStructureBuilder.class.getName(), "build", ex);
        }
        return this;
    }

    public Path save(@NonNull String sqlFile) {
        Path filePath = Path.of(country.getCode() + "_" + sqlFile);
        // truncate the file if exists, otherwise creates a new empty file
        try {
            Files.write(filePath, new byte[0]);
        } catch (IOException ex) {
            LOG.throwing(DivisionsDataStructureBuilder.class.getName(), "save", ex);
        }
        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            writeDivisionsTable(DivisionType.COUNTRY, Map.of(country.getCode(), country), writer);
            writeDivisionsTable(DivisionType.STATE, states, writer);
            writeDivisionsTable(DivisionType.COUNTY, counties, writer);
            writeDivisionsTable(DivisionType.COMMUNITY, communities, writer);
            writeCitiesTable(cities, writer);
        } catch (IOException ex) {
            LOG.throwing(DivisionsDataStructureBuilder.class.getName(), "save", ex);
        }

        System.out.println("MAX cities index:" + citiesIndex.get());
        System.out.println("MAX communities index:" + communitiesIndex.get());
        System.out.println("MAX counties index:" + countiesIndex.get());
        System.out.println("MAX states index:" + statesIndex.get());

        return filePath;
    }

    private void writeDivisionsTable(DivisionType placeType, Map<String, Division> divisionMap, BufferedWriter writer) throws IOException {
        writeCreateTable(placeType, writer);
        writer.newLine();
        if (!divisionMap.isEmpty()) {
            writer.write(sqlDialect.prepareInsertStart(placeType.getTableName()));
            writer.newLine();
            divisionMap.values().forEach(place -> writeWithException(placeType, place, writer));
            writer.write(sqlDialect.prepareInsertEnd(placeType.getTableName()));
            writer.newLine();
        }
    }

    private void writeCitiesTable(Map<String, City> cities, BufferedWriter writer) throws IOException {
        writeCreateTable(DivisionType.CITY, writer);
        writer.newLine();
        if (!cities.isEmpty()) {
            writer.write(sqlDialect.prepareInsertStart(DivisionType.CITY.getTableName()));
            writer.newLine();
            cities.values().forEach(place -> writeWithException(DivisionType.CITY, place, writer));
            writer.write(sqlDialect.prepareInsertEnd(DivisionType.CITY.getTableName()));
            writer.newLine();
        }
    }

    private void parse(@NonNull String line) {
        try {
            String[] tokens = line.split("\\t");
            if (country == null) {
                country = findCountry(tokens[0]);
            }
            City city = new City(tokens[1], tokens[2]);
            city.setId(citiesIndex.getAndIncrement());
            Division state = states.get(tokens[4]);
            if (state == null) {
                try {
                    state = new Division(DivisionType.STATE, tokens[4], tokens[3]);
                } catch (Throwable th) {
                    LOG.warning("Blank state:\n" + line);
                    return;
                }
                state.setId(statesIndex.getAndIncrement());
                state.setParent(country);
                states.put(state.getCode(), state);
            }
            city.setParent(state);

            if (!tokens[6].isBlank() && !tokens[5].isBlank()) {
                String key = state.getCode() + "-";
                key += tokens[6];
                Division county = counties.get(key);
                if (county == null) {
                    county = new Division(DivisionType.COUNTY, tokens[6], tokens[5]);
                    county.setId(countiesIndex.getAndIncrement());
                    county.setParent(state);
                    counties.put(key, county);
                }
                city.setCounty(county);

                if (!tokens[8].isBlank() && !tokens[7].isBlank()) {
                    key += "-" + tokens[8];
                    Division community = communities.get(key);
                    if (community == null) {
                        try {
                            community = new Division(DivisionType.COMMUNITY, tokens[8], tokens[7]);
                            community.setId(communitiesIndex.getAndIncrement());
                            community.setParent(county);
                            communities.put(key, community);
                        } catch (IllegalArgumentException ex) {
                            // skip community - can be NULL
                        }
                    }
                    city.setCommunity(community);
                }
            }
            cities.put(city.getCode(), city);
        } catch (IllegalArgumentException ex) {
            // skip the current line
            LOG.warning(ex.getMessage());
        }
    }

    private void writeCreateTable(@NonNull DivisionType placeType, BufferedWriter writer) throws IOException {
        if (placeType == DivisionType.COUNTRY) {
            writer.write(sqlDialect.getCreateTableQuery(placeType,
                placeType.getTableName()));
        } else {
            writer.write(sqlDialect.getCreateTableQuery(placeType,
                placeType.getTableName(),
                placeType.getParentColumn(),
                placeType.getCodeColumn(),
                placeType.getNameColumn()));
        }
        writer.newLine();
    }

    private void writeWithException(DivisionType divisionType,
                                    Division division,
                                    BufferedWriter writer) {
        try {
            writeInsertRow(divisionType, division, writer);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void writeInsertRow(DivisionType divisionType,
                                Division division,
                                BufferedWriter writer)
        throws IOException {
        if (Objects.requireNonNull(divisionType) == DivisionType.CITY) {
            City city = (City) division;
            Integer countyId = null;
            if (city.getCounty() != null) {
                countyId = city.getCounty().getId();
            }
            Integer communityId = null;
            if (city.getCommunity() != null) {
                communityId = city.getCommunity().getId();
            }
            writer.write(sqlDialect.getInsertTableQuery(divisionType,
                divisionType.getTableName(),
                divisionType.getParentColumn(),
                divisionType.getCodeColumn(),
                divisionType.getNameColumn(),
                city.getId(),
                city.getParent().getId(),
                countyId,
                communityId,
                city.getCode(),
                city.getName()));
        } else if (Objects.requireNonNull(divisionType) == DivisionType.COUNTRY) {
            Country country = (Country) division;
            writer.write(sqlDialect.getInsertTableQuery(divisionType,
                divisionType.getTableName(),
                country.getId(),
                country.getCode(),
                country.getName(),
                country.getCode3(),
                country.getOriginalName()));
        } else {
            writer.write(sqlDialect.getInsertTableQuery(divisionType,
                divisionType.getTableName(),
                divisionType.getParentColumn(),
                divisionType.getCodeColumn(),
                divisionType.getNameColumn(),
                division.getId(),
                division.getParent().getId(),
                division.getCode(),
                division.getName()));
        }
        writer.newLine();
    }

}
