package com.thcontest.data;

import com.thcontest.response.NetcdfFileDetails;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;

import java.io.IOException;
import java.net.URI;

public class ConcentrationDataHelper {
  private NetcdfFile dataFile;

  private ConcentrationDataHelper() { /* use a static factory method */ }

  public static ConcentrationDataHelper fromURL(URI remoteLocation) throws IOException {
    var helper = new ConcentrationDataHelper();
    helper.initFromURI(remoteLocation);
    return helper;
  }

  private void initFromURI(URI uri) throws IOException {
    try (NetcdfFile ncfile = NetcdfFiles.openInMemory(uri)) {
      dataFile = ncfile;
    }
  }

  public NetcdfFileDetails details() {
    return NetcdfFileDetails.fromFile(dataFile);
  }

}
