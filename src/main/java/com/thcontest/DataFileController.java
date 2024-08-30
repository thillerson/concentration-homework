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

@RestController
public class DataFileController {

  Logger logger = LoggerFactory.getLogger(DataFileController.class);

  @Value("${datafile.path}")
  String datafilePath;

  @GetMapping("/concentration-data-file")
  public byte[] ConcentrationDataFile() {

    logger.debug(String.format(">>>>>> Using Netcdf data file path: %s", datafilePath));

    Resource concentrationFileResource = new FileSystemResource(datafilePath);
    try {
      File file = concentrationFileResource.getFile();
      byte[] bytes = new byte[(int) file.length()];
      try(FileInputStream fis = new FileInputStream(file)) {
        fis.read(bytes);
      }
      return bytes;
      //TODO: return a meaningful error
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

}
