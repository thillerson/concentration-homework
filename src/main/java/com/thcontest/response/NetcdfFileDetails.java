package com.thcontest.response;

import ucar.nc2.NetcdfFile;

public record NetcdfFileDetails(
  String location,
  String title,
  String id,
  String fileType,
  String fileDesc,
  String fileVersion,
  String className,
  String rawDetails
) {
  public static NetcdfFileDetails fromFile(NetcdfFile file) {
    return new NetcdfFileDetails(
      file.getLocation(),
      file.getTitle(),
      file.getId(),
      file.getFileTypeId(),
      file.getFileTypeDescription(),
      file.getFileTypeVersion(),
      file.getClass().getName(),
      file.getDetailInfo()
    );
  }
}
