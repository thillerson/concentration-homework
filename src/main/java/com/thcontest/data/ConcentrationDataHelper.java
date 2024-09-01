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

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class ConcentrationDataHelper {
  private final Logger logger = LoggerFactory.getLogger(ConcentrationDataHelper.class);
  private final URI concentrationFileURI;

  private record ConcentrationVarInfo(
    int timeIndex,
    int zIndex,
    int xDimensionIndex,
    int yDimensionIndex,
    Dimension xDimension,
    Dimension yDimension,
    Array array
  ) {
  }

  public ConcentrationDataHelper() {
    try {
      concentrationFileURI = new URI("http://localhost:8080/concentration-data-file");
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
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

  public BufferedImage imageAtTimeAndZ(Integer timeIndex, Integer zIndex) throws IOException, InvalidRangeException {
    try (NetcdfFile ncfile = NetcdfFiles.openInMemory(concentrationFileURI)) {
      var concentrationInfo = infoForTimeAndZ(timeIndex, zIndex, ncfile);
      return concentrationToImage(concentrationInfo);
    }
  }

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

    return new ConcentrationVarInfo(
      timeIndex,
      zIndex,
      xDimensionIndex,
      yDimensionIndex,
      xDimension,
      yDimension,
      a
    );
  }

  private static BufferedImage concentrationToImage(ConcentrationVarInfo concentrationInfo) {
    int imageScaleFactor = 20;
    int margin = imageScaleFactor * 2;
    int width = (concentrationInfo.xDimension.getLength() * imageScaleFactor) + margin * 2;
    int height = (concentrationInfo.yDimension.getLength() * imageScaleFactor) + margin * 2;

    BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    Graphics g = bufferedImage.getGraphics();
    g.setColor(Color.WHITE);
    g.fillRect(0, 0, width - 1, height - 1);
    g.setColor(Color.BLACK);
    g.drawRect(0, 0, width - 1, height - 1);
    g.drawString(
      String.format("Concentration at time-index: %s, z-index: %s", concentrationInfo.timeIndex, concentrationInfo.zIndex),
      margin/2, margin/2
    );

    double currentConcentration;
    // Take one pass to find min and max
    List<Double> logValues = new ArrayList<>();
    var iter = concentrationInfo.array.getIndexIterator();
    while (iter.hasNext()) {
      currentConcentration = iter.getDoubleNext();
      logValues.add(Math.log1p(currentConcentration));
    }
    double min = logValues.stream().filter(n -> n > 0).min(Double::compareTo).get();
    double max = logValues.stream().max(Double::compareTo).get();

    int currentX, currentY;
    int[] currentCounter;
    iter = concentrationInfo.array.getIndexIterator();
    while (iter.hasNext()) {
      currentConcentration = iter.getDoubleNext();
      currentCounter = iter.getCurrentCounter();
      currentX = currentCounter[concentrationInfo.xDimensionIndex];
      currentY = currentCounter[concentrationInfo.yDimensionIndex];

      double log= Math.log1p(currentConcentration);
      float grayscale = Math.abs((float) (((log - min) / (max - min)) * 255));
      if (grayscale > 1) {
        grayscale = 1;
      }

      g.setColor(new Color(grayscale, grayscale, grayscale));
      g.fillRect((currentX * imageScaleFactor) + margin, (currentY * imageScaleFactor) + margin, imageScaleFactor - 1, imageScaleFactor - 1);
    }

    return bufferedImage;
  }

}