package com.thcontest.data;

import com.thcontest.response.ConcentrationData;
import com.thcontest.response.ConcentrationDataPoint;
import com.thcontest.response.NetcdfFileDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.Variable;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class ConcentrationDataHelper {
  private final Logger logger = LoggerFactory.getLogger(ConcentrationDataHelper.class);
  private URI concentrationFileURI;

  private static class ConcentrationVarInfo {
    int xDimensionIndex;
    int yDimensionIndex;
    Dimension xDimension;
    Dimension yDimension;
    Array array;

  }

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
      var concentrationInfo = infoForTimeAndZ(timeIndex, zIndex, ncfile);
      var concentrationField = new String[concentrationInfo.yDimension.getLength()][concentrationInfo.xDimension.getLength()];
      List<ConcentrationDataPoint> concentrations = new ArrayList<>();

      double currentConcentration;
      String formattedConcentration;
      int currentX, currentY;
      int[] currentCounter;
      var iter = concentrationInfo.array.getIndexIterator();
      while (iter.hasNext()) {
        currentConcentration = iter.getDoubleNext();
        currentCounter = iter.getCurrentCounter();
        currentX = currentCounter[concentrationInfo.xDimensionIndex];
        currentY = currentCounter[concentrationInfo.yDimensionIndex];
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

  //public BufferedImage imageAtTimeAndZ(Integer timeIndex, Integer zIndex) throws IOException, InvalidRangeException {

  //}

  private ConcentrationVarInfo infoForTimeAndZ(int timeIndex, int zIndex, NetcdfFile file) throws InvalidRangeException, IOException {
    // double concentration(time=8, z=1, y=27, x=36);
    Variable v = file.findVariable("concentration");

    int xDimensionIndex = v.findDimensionIndex("x");
    int yDimensionIndex = v.findDimensionIndex("y");
    Dimension xDimension = v.getDimension(xDimensionIndex);
    Dimension yDimension = v.getDimension(yDimensionIndex);
    int[] origin = new int[]{timeIndex, zIndex, 0, 0};
    int[] size = new int[]{1, 1, yDimension.getLength(), xDimension.getLength()};
    Array a = v.read(origin, size);

    var d = new ConcentrationVarInfo();
    d.xDimensionIndex = xDimensionIndex;
    d.yDimensionIndex = yDimensionIndex;
    d.xDimension = xDimension;
    d.yDimension = yDimension;
    d.array = a;
    return d;
  }
}