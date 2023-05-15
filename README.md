# GeoNames Database Importer

This tool parse the data provided by <a href="http://download.geonames.org/export/zip/" target="_blank">GeoNames</a>. As result an SQL file
is produced to able to import it in different DB servers. Supported databases are: MSSQL, MySQL, PostgreSQL and Oracle.

The data format is tab-delimited text in utf8 encoding, with the following fields :

```
country code         : iso country code, 2 characters
postal code          : varchar(20)
place name           : varchar(180)
State                : 1. order subdivision (state) varchar(100)
State short name     : 1. order subdivision (state) varchar(20)
County               : 2. order subdivision (county/province) varchar(100)
County short name    : 2. order subdivision (county/province) varchar(20)
Community            : 3. order subdivision (community) varchar(100)
Community short name : 3. order subdivision (community) varchar(20)
latitude             : estimated latitude (wgs84)
longitude            : estimated longitude (wgs84)
accuracy             : accuracy of lat/lng from 1=estimated, 4=geonameid, 6=centroid of addresses or shape
```

Example:

```
US	10160	New York	New York	NY	New York	061			40.7808	-73.9772	4
```
## Cities, Communities, Counties and States ##

The app parses the file provided by the <a href="http://download.geonames.org/export/zip/" target="_blank">GeoNames</a>, 
creates a SQL file with a full list of cities, states, counties and communities. Note that the community may be missing 
for a certain urban place.

Example:
```
INSERT INTO states (ID, code, name) VALUES (34, "NY", "New York");
...
INSERT INTO cities (ID, stateId, countyId, communityId, postalCode, name) VALUES (2654, 36, 56, null, "10160", "New York");
```
The SQL file will create tables with the following structure.

Table States

| Name          | Type           | Null  | Extra              |
| ------------- |:--------------:| -----:| ------------------:|
| ID            | int            |  NO   | PK, auto_increment |
| code          | varchar(20)    |  NO   |                    |
| name          | varchar(100)   |  NO   |                    |

Table Counties

| Name    | Type           | Null  |              Extra |
|---------|:--------------:| -----:|-------------------:|
| ID      | int            |  NO   | PK, auto_increment |
| stateId | int            |  NO   |                    |
| code    | varchar(20)    |  NO   |                    |
| name    | varchar(100)   |  NO   |                    |

Table Communities

| Name     | Type           | Null  |              Extra |
|----------|:--------------:| -----:|-------------------:|
| ID       | int            |  NO   | PK, auto_increment |
| countyId | int            |  NO   |                    |
| code     | varchar(20)    |  NO   |                    |
| name     | varchar(100)   |  NO   |                    |

Table Cities

| Name        | Type           | Null |              Extra |
|-------------|:--------------:|-----:|-------------------:|
| ID          | int            |   NO | PK, auto_increment |
| stateId     | int            |   NO |                    |
| countyId    | int            |   NO |                    |
| communityId | int            |  YES |                    |
| postalCode  | varchar(20)    |   NO |                    |
| name        | varchar(100)   |   NO |                    |



## SQL Dialects ##
This application supports various SQL dialects like:
- Mysql / MariaDB
- PostgreSQL 
- Oracle
- MS SQL Server

## Usage ##

```
./bin/GeoNames_DBImporter US.txt mysql
```
where the first argument is the path to the txt file provided by the
<a href="http://download.geonames.org/export/zip/" target="_blank">GeoNames</a>. 
The second parameter is the dialect that the generated SQL file will support.

