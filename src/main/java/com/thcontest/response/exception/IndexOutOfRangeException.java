package com.thcontest.response.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.BAD_REQUEST, reason="Index out of range")
public class IndexOutOfRangeException extends RuntimeException {
  public IndexOutOfRangeException(String message) {
    super(message);
  }

}

