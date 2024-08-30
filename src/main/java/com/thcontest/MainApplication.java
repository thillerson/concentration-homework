package com.thcontest;

import com.thcontest.data.ConcentrationDataHelper;
import com.thcontest.response.NetcdfFileDetails;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NCdumpW;
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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.ByteArrayOutputStream;
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
		try {
			try{
				final URI concentrationFileURI = new URI("http://localhost:8080/concentration-data-file");
				ConcentrationDataHelper dataHelper = ConcentrationDataHelper.fromURL(concentrationFileURI);
				return dataHelper.details();
			} catch (IOException ioe) {
				// TODO: return an error
				logger.error("Error reading file", ioe);
				throw new RuntimeException(ioe);
			}
		} catch (URISyntaxException e) {// come on, this won't happen
			logger.error("Error building concentration file URI", e);
			throw new RuntimeException(e);
    }
  }

}
