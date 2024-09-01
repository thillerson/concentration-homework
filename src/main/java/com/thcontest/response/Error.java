package com.thcontest.response;

/**
 * DTO for returning a JSON error message
 * @param message
 * @param detail
 */
public record Error(
  String message,
  String detail
) {
}
