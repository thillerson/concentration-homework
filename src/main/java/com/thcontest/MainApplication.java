package com.thcontest;

import com.thcontest.data.ConcentrationDataHelper;
import com.thcontest.response.ConcentrationData;
import com.thcontest.response.NetcdfFileDetails;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ucar.nc2.Variable;
import ucar.nc2.write.Ncdump;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

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
			// TODO: return an error
			logger.error("Error reading file", ioe);
			throw new RuntimeException(ioe);
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
			// TODO: return an error
			logger.error("Error reading data file", ioe);
			throw new RuntimeException(ioe);
		}
	}
}
