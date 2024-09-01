package com.thcontest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * DataFileException - for problems with the NetCDF data file
 */
@ResponseStatus(value= HttpStatus.INTERNAL_SERVER_ERROR, reason="Unable to open data file")
public class DataFileException extends RuntimeException {
  public DataFileException(String message) {
    super(message);
  }
}
