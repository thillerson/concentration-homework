package com.thcontest.data;

import com.thcontest.response.ConcentrationData;
import com.thcontest.response.NetcdfFileDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.Variable;
import ucar.nc2.write.Ncdump;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class ConcentrationDataHelper {
  private final Logger logger = LoggerFactory.getLogger(ConcentrationDataHelper.class);
  private URI concentrationFileURI;

  private ConcentrationDataHelper() { /* use a static factory method */ }

  public static ConcentrationDataHelper fromDefault() throws IOException {
    try {
      var uri = new URI("http://localhost:8080/concentration-data-file");
      return ConcentrationDataHelper.fromURL(uri);
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  public static ConcentrationDataHelper fromURL(URI remoteLocation) throws IOException {
    var helper = new ConcentrationDataHelper();
    helper.initWithURI(remoteLocation);
    return helper;
  }

  private void initWithURI(URI uri) throws IOException {
    concentrationFileURI = uri;
  }

  public NetcdfFileDetails details() {
    try (NetcdfFile ncfile = NetcdfFiles.openInMemory(concentrationFileURI)) {
      return NetcdfFileDetails.fromFile(ncfile);
    } catch (IOException e) {
      // TODO: return an error
      throw new RuntimeException(e);
    }
  }

  public ConcentrationData concentrationAtTimeAndZ(Integer timeIndex, Integer zIndex) {
    try (NetcdfFile ncfile = NetcdfFiles.openInMemory(concentrationFileURI)) {
    /*
    double concentration(time=8, z=1, y=27, x=36);
     */
      int[] origin = new int[]{timeIndex, zIndex, 0, 0};
      int[] size = new int[]{1, 1, 27, 36};

      Variable v = ncfile.findVariable("concentration");

      int i = 0;
      for (var d : v.getDimensions()) {
        logger.debug(String.format("Dimension #%d: %s", i, d.getName()));
        i++;
      }

      Array a = v.read(origin, size);
      var dump = Ncdump.printArray(a);

      return new ConcentrationData(timeIndex, zIndex, dump);
    } catch (InvalidRangeException | IOException e) {
      // TODO: return an error
      throw new RuntimeException(e);
    }
  }
}