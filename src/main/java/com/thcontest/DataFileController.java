package com.thcontest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * DataFileController - Controller responsible for loading the NetCDF data file
 */
@RestController
public class DataFileController {

  Logger logger = LoggerFactory.getLogger(DataFileController.class);

  /**
   * Expecting this to come from an environment variable, see `src/main/resources/application.properties`
   */
  @Value("${datafile.path}")
  String datafilePath;

  /**
   *  concentrationDataFile - This endpoint loads the bytes of the NetCDF datafile from disk
   * @return byte[] NetCDF bytes
   */
  @GetMapping("/concentration-data-file")
  public byte[] concentrationDataFile() {

    logger.debug(String.format(">>>>>> Using Netcdf data file path: %s", datafilePath));

    Resource concentrationFileResource = new FileSystemResource(datafilePath);
    try {
      File file = concentrationFileResource.getFile();
      byte[] bytes = new byte[(int) file.length()];
      try(FileInputStream fis = new FileInputStream(file)) {
        fis.read(bytes);
      }
      return bytes;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

}
