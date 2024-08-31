package com.thcontest.response;

import java.util.List;

public record ConcentrationData(
  Integer timeIndex,
  Integer zIndex,
  String[][] concentrationField,
  List<ConcentrationDataPoint> concentrations
) { }
