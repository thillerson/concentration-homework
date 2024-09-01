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

/**
 * ConcentrationDataHelper NetCDF querying
 */
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

  /**
   *  ConcentrationDataHelper helps read data from a NetCDF file
   */
  public ConcentrationDataHelper() {
    try {
      concentrationFileURI = new URI("http://localhost:8080/concentration-data-file");
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * details gets details about the NetCDF file
   * @return NetcdfFileDetails a detail DTO
   * @throws IOException if there is an issue reading the NetCDF file
   */
  public NetcdfFileDetails details() throws IOException {
    try (NetcdfFile ncfile = NetcdfFiles.openInMemory(concentrationFileURI)) {
      return NetcdfFileDetails.fromFile(ncfile);
    }
  }

  /**
   *  concentrationAtTimeAndZ - gets concentration data
   * @param timeIndex - an integer time index
   * @param zIndex - an integer z index
   * @return ConcentrationData data about concentration at certain x and y coordinates
   * @throws IOException if there is an issue reading the NetCDF file
   * @throws InvalidRangeException if one or more of time or z indices are out of valid range
   */
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

  /**
   * imageAtTimeAndZ returns an image visualizing concentration data at given time and z indices
   * @param timeIndex - an integer time index
   * @param zIndex - an integer z index
   * @return BufferedImage image data
   * @throws IOException if there is an issue reading the NetCDF file
   * @throws InvalidRangeException if one or more of time or z indices are out of valid range
   */
  public BufferedImage imageAtTimeAndZ(Integer timeIndex, Integer zIndex) throws IOException, InvalidRangeException {
    try (NetcdfFile ncfile = NetcdfFiles.openInMemory(concentrationFileURI)) {
      var concentrationInfo = infoForTimeAndZ(timeIndex, zIndex, ncfile);
      return concentrationToImage(concentrationInfo);
    }
  }

  /**
   * Loads the concentration data and returns a value object of data at a time and z index
   */
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

  /**
   * Turns concentration data into an image
   */
  private static BufferedImage concentrationToImage(ConcentrationVarInfo concentrationInfo) {
    // Scale factor for making image a reasonable size
    int imageScaleFactor = 20;
    int margin = imageScaleFactor * 2;
    int width = (concentrationInfo.xDimension.getLength() * imageScaleFactor) + margin * 2;
    int height = (concentrationInfo.yDimension.getLength() * imageScaleFactor) + margin * 2;

    BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    Graphics g = bufferedImage.getGraphics();
    // White background fill
    g.setColor(Color.WHITE);
    g.fillRect(0, 0, width - 1, height - 1);
    // Black border
    g.setColor(Color.BLACK);
    g.drawRect(0, 0, width - 1, height - 1);
    // Helpful title
    g.drawString(
      String.format("Concentration at time-index: %s, z-index: %s", concentrationInfo.timeIndex, concentrationInfo.zIndex),
      margin/2, margin/2
    );

    double currentConcentration;
    // Take one pass through the array of concentration data to find min and max on a log scale
    List<Double> logValues = new ArrayList<>();
    var iter = concentrationInfo.array.getIndexIterator();
    while (iter.hasNext()) {
      currentConcentration = iter.getDoubleNext();
      logValues.add(Math.log1p(currentConcentration));
    }
    double min = logValues.stream().filter(n -> n > 0).min(Double::compareTo).get();
    double max = logValues.stream().max(Double::compareTo).get();

    // Take another pass through to paint the grid of concentration values
    int currentX, currentY;
    int[] currentCounter;
    iter = concentrationInfo.array.getIndexIterator();
    while (iter.hasNext()) {
      currentConcentration = iter.getDoubleNext();
      currentCounter = iter.getCurrentCounter();
      currentX = currentCounter[concentrationInfo.xDimensionIndex];
      currentY = currentCounter[concentrationInfo.yDimensionIndex];

      // This attempts to find a suitable grayscale value between the min and max values
      // of the current array
      double log= Math.log1p(currentConcentration);
      float grayscale = Math.abs((float) (((log - min) / (max - min)) * 255));
      // Hax
      if (grayscale > 1) {
        grayscale = 1;
      }

      // Paint a concentration value
      g.setColor(new Color(grayscale, grayscale, grayscale));
      g.fillRect((currentX * imageScaleFactor) + margin, (currentY * imageScaleFactor) + margin, imageScaleFactor - 1, imageScaleFactor - 1);
    }

    return bufferedImage;
  }

}