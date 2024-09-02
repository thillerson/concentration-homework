# Concentration Data API "Takehome Assignment"

The assignment was to provide an API, implemented using Spring Boot, that queries a NetCDF data file. The following endpoints were required
1. /get-info, returns the NetCDF detailed information.
1. /get-data, params to include time index and z index, returns json response that
includes x, y, and concentration data.
1. /get-image, params to include time index and z index, returns png visualization of
concentration.

## Running the Project

The project can be run as a containerized application or "locally".

### Setup

If running locally using `gradle` (see below), i.e. not as a containerized application, first copy `.env.example` to `.env`, and change the `DATAFILE_PATH` variable to have the absolute path to the project's `data/concentration.timeseries.nc` NetCDF data file.

Note that the `.env.production` environment file will be used verbatim in any containerized deployments. (This is just for convenience and in a real world situation would be handled differently).

### Running
The project can be run in the following ways:
1. **Gradle**: Execute `./gradlew runBoot`
1. **Docker**: A `Dockerfile` is provided
1. **Docker compose**: Execute `docker-compose up`

## Tour

### Endpoints

#### `/get-info`
Returns information about the NetCDF data file. A JSON document contains some of the file information as fields, and more data about the contents of the file as a "dump", inside the `rawDetails` field.
#### `/get-data`
Returns concentration data at given time and z indices. Example `/get-data?time-index=0&z-index=0`. If either of the indices are out of bounds, an error is returned, for example: 
```json
{
  "message": "Index out of range",
  "detail": "Illegal Range for dimension 0: last requested 12 > max 7"
}
```

The JSON document returned has four fields:
* `timeIndex` - the requested time index as an integer
* `zIndex` - the requested z index as an integer
* `concentrationField` - the concentration data as an array of arrays of strings. The outer array is the `y` coordinates and the inner arrays are the `x` coordinates. The concentration data are returned as strings containing concentration values in scientific notation
* `concentrations` - this is a slightly different way of delivering the concentration values, just for discussion. Instead of an array "grid", it is a list of concentration data points in the form:
```json
{
  "concentration": "0.0",
  "x": 1,
  "y": 1
}
```

#### `/get-image`
Returns a PNG image visualizing dispersion at given time and z indices. Example `/get-image?time-index=0&z-index=0`. The image attempts to depict concentration with grayscale values, where black is `0` and white is the maximum concentration in the dataset at the current time and z index.

### Code

All the code should have documentation in javadoc form, and helpful comments. For details, look in the code. Here is an overview of the project:
#### `data`
Contains NetCDF data file
#### `src/main/java/com/thcontest`
Contains Spring Boot Controllers
#### `src/main/java/com/thcontest/data`
`ConcentrationDataHelper` is responsible for reading data from the NetCDF data file.
#### `src/main/java/com/thcontest/exception`
The main exceptions dealt with in the application
#### `src/main/java/com/thcontest/response`
Record files which represent JSON return objects

## TODO

Some improvements that could be made:

* Index out of range error could give more detail about which index is the issue.
* The `get-info` endpoint could give more information about the `concentration` variable, such as how big the `x`, `y`, and `z` variable ranges are.
* The image "algorithm" for choosing a gray value for concentration only takes the minimum and maximum values from the current time and z indices, not the full dataset. Comparing the images from different time indices will not give an valid impression of concentration over time.
* The "algorithm" for choosing the image color could be improved. 