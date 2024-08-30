package com.thcontest.response;

public record ConcentrationData(
  Integer timeIndex,
  Integer zIndex,
  String rawData
) { }
