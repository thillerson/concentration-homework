package com.thcontest.data;

import com.thcontest.response.ConcentrationData;
import com.thcontest.response.ConcentrationDataPoint;
import com.thcontest.response.NetcdfFileDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.Variable;
import ucar.nc2.write.Ncdump;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConcentrationDataHelper {
  private final Logger logger = LoggerFactory.getLogger(ConcentrationDataHelper.class);
  private URI concentrationFileURI;

  private ConcentrationDataHelper() { /* use a static factory method */ }

  public static ConcentrationDataHelper fromDefault() {
    try {
      var uri = new URI("http://localhost:8080/concentration-data-file");
      return ConcentrationDataHelper.fromURL(uri);
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  public static ConcentrationDataHelper fromURL(URI remoteLocation) {
    var helper = new ConcentrationDataHelper();
    helper.initWithURI(remoteLocation);
    return helper;
  }

  private void initWithURI(URI uri) {
    concentrationFileURI = uri;
  }

  public NetcdfFileDetails details() throws IOException {
    try (NetcdfFile ncfile = NetcdfFiles.openInMemory(concentrationFileURI)) {
      return NetcdfFileDetails.fromFile(ncfile);
    }
  }

  public ConcentrationData concentrationAtTimeAndZ(Integer timeIndex, Integer zIndex) throws IOException, InvalidRangeException {
    try (NetcdfFile ncfile = NetcdfFiles.openInMemory(concentrationFileURI)) {
      // double concentration(time=8, z=1, y=27, x=36);
      Variable v = ncfile.findVariable("concentration");

      int xDimensionIndex = v.findDimensionIndex("x");
      int yDimensionIndex = v.findDimensionIndex("y");
      Dimension xDimension = v.getDimension(xDimensionIndex);
      Dimension yDimension = v.getDimension(yDimensionIndex);
      int[] origin = new int[]{timeIndex, zIndex, 0, 0};
      int[] size = new int[]{1, 1, yDimension.getLength(), xDimension.getLength()};

      String[][] concentrationField = new String[yDimension.getLength()][xDimension.getLength()];

      Array a = v.read(origin, size);
      List<ConcentrationDataPoint> concentrations = new ArrayList<>();

      IndexIterator iter = a.getIndexIterator();
      int currentX, currentY;
      double currentConcentration;
      String formattedConcentration;
      int[] currentCounter;
      while (iter.hasNext()) {
        currentConcentration = iter.getDoubleNext();
        currentCounter = iter.getCurrentCounter();
        currentX = currentCounter[xDimensionIndex];
        currentY = currentCounter[yDimensionIndex];
        if (currentConcentration == 0.0) {
          formattedConcentration = "0.0";
        } else {
          formattedConcentration = String.format("%8.4e", currentConcentration);
        }
        concentrationField[currentY][currentX] = formattedConcentration;
        concentrations.add(new ConcentrationDataPoint(formattedConcentration, currentX + 1, currentY + 1));
      }

      return new ConcentrationData(timeIndex, zIndex, concentrationField, concentrations);
    }
  }
}