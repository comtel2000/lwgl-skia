package org.comtel2000.lwgl.sample;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.jetbrains.skija.Data;
import org.jetbrains.skija.Typeface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FontUtils {

  private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  
  private FontUtils() {}

  public static Data dataFromURL(URL url) {
    try {
      return Data.makeFromBytes(Files.readAllBytes(Paths.get(url.toURI())));
    } catch (IOException | URISyntaxException e) {
      logger.error(e.getMessage(), e);
    }
    return null;
  }

  public static Typeface typefaceFromURL(URL url) {
    Data data = dataFromURL(url);
    if (data != null) {
      return Typeface.makeFromData(data);
    }
    return null;
  }
}
