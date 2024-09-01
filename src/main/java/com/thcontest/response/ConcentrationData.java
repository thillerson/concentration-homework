package com.thcontest.response;

import java.util.List;

/**
 * DTO for returning a JSON document of concentration data at given time and z indices
 * @param timeIndex
 * @param zIndex
 * @param concentrationField
 * @param concentrations
 */
public record ConcentrationData(
  Integer timeIndex,
  Integer zIndex,
  String[][] concentrationField,
  List<ConcentrationDataPoint> concentrations
) { }
