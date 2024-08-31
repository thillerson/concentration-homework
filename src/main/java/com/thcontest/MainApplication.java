package com.thcontest;

import com.thcontest.data.ConcentrationDataHelper;
import com.thcontest.response.ConcentrationData;
import com.thcontest.response.NetcdfFileDetails;
import com.thcontest.response.exception.DataFileException;
import com.thcontest.response.exception.IndexOutOfRangeException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import ucar.ma2.InvalidRangeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@SpringBootApplication
public class MainApplication {

	Logger logger = LoggerFactory.getLogger(MainApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(MainApplication.class, args);
	}

	@GetMapping("/get-info")
	public NetcdfFileDetails getInfo() {
		try{
			ConcentrationDataHelper dataHelper = ConcentrationDataHelper.fromDefault();
			return dataHelper.details();
		} catch (IOException ioe) {
			logger.error("Error reading data file", ioe);
			throw new DataFileException("Error reading data file");
		}
  }

	@GetMapping("/get-data")
	public ConcentrationData getData(
		@RequestParam(name = "time-index", required = false) Integer timeIndex,
		@RequestParam(name = "z-index", required = false) Integer zIndex
	) {
		try{
			ConcentrationDataHelper dataHelper = ConcentrationDataHelper.fromDefault();
			return dataHelper.concentrationAtTimeAndZ(timeIndex, zIndex);
		} catch (IOException ioe) {
			logger.error("Error reading data file", ioe);
			throw new DataFileException("Error reading data file");
		} catch (InvalidRangeException ire) {
			throw new IndexOutOfRangeException(ire.getMessage());
		}
	}
}
