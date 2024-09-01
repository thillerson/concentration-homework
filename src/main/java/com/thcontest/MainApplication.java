package com.thcontest;

import com.thcontest.data.ConcentrationDataHelper;
import com.thcontest.response.Error;
import com.thcontest.response.ConcentrationData;
import com.thcontest.response.NetcdfFileDetails;
import com.thcontest.exception.DataFileException;
import com.thcontest.exception.IndexOutOfRangeException;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ucar.ma2.InvalidRangeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * MainApplication - the Spring Boot application. Also contains all three endpoint definitions.
 */
@RestController
@SpringBootApplication
public class MainApplication {

	Logger logger = LoggerFactory.getLogger(MainApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(MainApplication.class, args);
	}

	/**
	 * getInfo - `/get-info` endpoint
	 * @return NetcdfFileDetails contains information about the NetCDF data file
	 */
	@GetMapping("/get-info")
	public NetcdfFileDetails getInfo() {
		try{
			return new ConcentrationDataHelper().details();
		} catch (IOException ioe) {
			logger.error("Error reading data file", ioe);
			throw new DataFileException("Error reading data file");
		}
  }

	/**
	 * getData - `/get-data` endpoint
	 * @param timeIndex int value for time index to retrieve
	 * @param zIndex int value for z index to retrieve
	 * @return ConcentrationData concentration data for current time and z indices
	 * @throws DataFileException if there is an issue reading the data file
	 * @throws IndexOutOfRangeException if one of the given index variables is out of range
	 */
	@GetMapping("/get-data")
	public ConcentrationData getData(
		@RequestParam(name = "time-index", required = false) Integer timeIndex,
		@RequestParam(name = "z-index", required = false) Integer zIndex
	) {
		try{
			return new ConcentrationDataHelper().concentrationAtTimeAndZ(timeIndex, zIndex);
		} catch (IOException ioe) {
			logger.error("Error reading data file", ioe);
			throw new DataFileException("Error reading data file");
		} catch (InvalidRangeException ire) {
			throw new IndexOutOfRangeException(ire.getMessage());
		}
	}

	/**
	 * getImage - `/get-image` endpoint
	 * @param timeIndex int value for time index to retrieve
	 * @param zIndex int value for z index to retrieve
	 * @return byte[] PNG encoded image data visualizing concentration at the given time and z indices
	 * @throws DataFileException if there is an issue reading the data file
	 * @throws IndexOutOfRangeException if one of the given index variables is out of range
	 */
	@GetMapping(
		path = "/get-image",
		produces = MediaType.IMAGE_PNG_VALUE
	)
	public byte[] getImage(
		@RequestParam(name = "time-index", required = false) Integer timeIndex,
		@RequestParam(name = "z-index", required = false) Integer zIndex
	) {
		ConcentrationDataHelper dataHelper = new ConcentrationDataHelper();
    try {
      var image =  dataHelper.imageAtTimeAndZ(timeIndex, zIndex);
			var baos = new ByteArrayOutputStream();
			ImageIO.write(image, "png", baos);
			return baos.toByteArray();
		} catch (IOException ioe) {
			logger.error("Error reading data file", ioe);
			throw new DataFileException("Error reading data file");
		} catch (InvalidRangeException ire) {
			throw new IndexOutOfRangeException(ire.getMessage());
		}
  }

	/**
	 * handleIndexOutOfRange - error handler returning a JSON error message for IndexOutOfRangeException
	 * @param e IndexOutOfRangeException
	 * @return Error document
	 */
	@ExceptionHandler(IndexOutOfRangeException.class)
	public ResponseEntity<Error> handleIndexOutOfRange(IndexOutOfRangeException e) {
		var err = new Error(
			"Index out of range",
			e.getMessage()
		);
		return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
	}

	/**
	 * handleDataFileError - error handler returning a JSON error message for DataFileException
	 * @param e DataFileException
	 * @return Error document
	 */
	@ExceptionHandler(DataFileException.class)
	public ResponseEntity<Error> handleDataFileError(DataFileException e) {
		var err = new Error(
			"Error with data file",
			e.getMessage()
		);
		return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}