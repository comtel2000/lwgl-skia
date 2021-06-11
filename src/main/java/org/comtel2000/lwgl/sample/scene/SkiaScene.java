package org.comtel2000.lwgl.sample.scene;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.comtel2000.lwgl.sample.FontUtils;
import org.jetbrains.skija.Canvas;
import org.jetbrains.skija.Font;
import org.jetbrains.skija.Paint;
import org.jetbrains.skija.Rect;
import org.jetbrains.skija.Typeface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * sample from https://github.com/JetBrains/skija/tree/master/examples/scenes/src
 *
 */
public abstract class SkiaScene {

  private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public static final Typeface inter = FontUtils.typefaceFromURL(HUD.class.getResource("/fonts/InterHinted-Regular.ttf"));
  public static final Typeface jbMono = FontUtils.typefaceFromURL(HUD.class.getResource("/fonts/JetBrainsMono-Regular.ttf"));
  public static final Font inter13 = new Font(inter, 13).setSubpixel(true);
  public static final Paint blackFill = new Paint().setColor(0xFF000000);

  protected final List<String> _variants = new ArrayList<>();
  protected int _variantIdx = 0;

  public boolean scale() {
    return true;
  }

  public String variantTitle() {
    return _variants.get(_variantIdx);
  }

  public void changeVariant(int delta) {
    _variantIdx = (_variantIdx + _variants.size() + delta) % _variants.size();
  }

  public abstract void draw(Canvas canvas, int width, int height, float dpi, int xpos, int ypos);

  public void onScroll(float dx, float dy) {}

  public static String file(String path) {
    return path;
  }

  public static void drawStringCentered(Canvas canvas, String text, float x, float y, Font font, Paint paint) {
    var bounds = font.measureText(text, paint);
    float lineHeight = font.getMetrics().getCapHeight();
    canvas.drawString(text, x - bounds.getRight() / 2, y + lineHeight / 2, font, paint);
  }

  public static void drawStringLeft(Canvas canvas, String text, Rect outer, Font font, Paint paint) {
    var metrics = font.getMetrics();
    float innerHeight = metrics.getDescent() - metrics.getAscent();
    canvas.drawString(text, outer.getLeft(), outer.getTop() + (outer.getHeight() - innerHeight) / 2f - metrics.getAscent(), font, paint);
  }

  public static float phase() {
    var angle = (System.currentTimeMillis() % 5000) / 5000.0 * Math.PI * 2.0;
    var phase = Math.sin(angle) * 1.2;
    phase = Math.min(1.0, Math.max(-1.0, phase));
    return (float) (phase + 1) / 2f;
  }

  public static String formatFloat(float f) {
    return String.format(Locale.ENGLISH, "%.02f", f).replaceAll("\\.?0+$", "");
  }

  protected void readVariants(URL url) {
    if (url.toExternalForm().contains("!")) {
      readJarVariants(url);
      return;
    }
    try (var stream = Files.newDirectoryStream(Path.of(url.toURI()))) {
      for (var p : stream) {
        if (!Files.isDirectory(p)) {
          var file = p.getFileName().toString().replace("/", "");
          _variants.add(file);
        }
      }
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
  }

  private void readJarVariants(URL url) {
    var array = url.toExternalForm().split("!");
    try (var fs = FileSystems.newFileSystem(URI.create(array[0]), Collections.emptyMap())) {
      final var path = fs.getPath(array[1]);
      try (var stream = Files.newDirectoryStream(path)) {
        for (var p : stream) {
          if (!Files.isDirectory(p)) {
            var file = p.getFileName().toString().replace("/", "");
            _variants.add(file);
          }
        }
      }
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
  }

  public String asString(URL url) {
    if (url == null) {
      return null;
    }

    try {
      return Files.readString(Paths.get(url.toURI()));
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
    return null;
  }

  public static String formatFloatArray(float[] fs) {
    var sb = new StringBuilder("[");
    for (var i = 0; i < fs.length; ++i) {
      sb.append(formatFloat(fs[i]));
      if (i < fs.length - 1)
        sb.append(", ");
    }
    sb.append("]");
    return sb.toString();
  }
}
