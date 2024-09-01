package com.thcontest.response;

import ucar.nc2.NetcdfFile;

/**
 * DTO for details about a NetCDF data file
 * @param location
 * @param title
 * @param id
 * @param fileType
 * @param fileDesc
 * @param fileVersion
 * @param className
 * @param rawDetails - contents of NetCDFDump
 */
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
