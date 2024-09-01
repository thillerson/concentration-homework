package com.thcontest.response;

/**
 * DTO for returning concentration data at a specific point
 * @param concentration
 * @param x
 * @param y
 */
public record ConcentrationDataPoint(
  String concentration,
  int x,
  int y
) {
}
