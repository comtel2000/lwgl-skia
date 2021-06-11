package org.comtel2000.lwgl.sample.scene;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;

import org.jetbrains.skija.Canvas;
import org.jetbrains.skija.Paint;
import org.jetbrains.skija.RRect;
import org.jetbrains.skija.Rect;
import org.jetbrains.skija.impl.Stats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * sample from https://github.com/JetBrains/skija/tree/master/examples/scenes/src
 *
 */
public class HUD extends SkiaScene {

  private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private long t0 = System.nanoTime();
  private double[] times = new double[155];
  private int timesIdx = 0;

  private SkiaScene scene;
  
  private volatile boolean vsync;

  public void setVsync(boolean vsync) {
    this.vsync = vsync;
  }

  public SkiaScene getScene() {
    return scene;
  }

  public void setScene(SkiaScene scene) {
    this.scene = scene;
  }

  
  public void tick() {
    long t1 = System.nanoTime();
    times[timesIdx] = (t1 - t0) / 1000000.0;
    t0 = t1;
    timesIdx = (timesIdx + 1) % times.length;
  }

  public void log() {
    if (timesIdx % 100 == 0)
      logger.debug(String.format("%.0f fps", 1000.0 / Arrays.stream(times).takeWhile(t -> t > 0).average().orElse(0d)));
  }

  public void draw(Canvas canvas, int width, int height) {
    long nativeCalls = Stats.nativeCalls;
    Stats.nativeCalls = 0;
    int allocated = Stats.allocated.values().stream().reduce(0, Integer::sum);

    try (Paint bg = new Paint().setColor(0x90000000);
        Paint fg = new Paint().setColor(0xFFFFFFFF);
        Paint graph = new Paint().setColor(0xFF00FF00).setStrokeWidth(1);
        Paint graphPast = new Paint().setColor(0x9000FF00).setStrokeWidth(1);
        Paint graphLimit = new Paint().setColor(0xFFcc3333).setStrokeWidth(1)) {

      float variantsHeight = 25;

      // Background
      canvas.translate(width - 230, height - 160 - variantsHeight - 1 * 25);
      canvas.drawRRect(RRect.makeLTRB(0, 0, 225, 180 + variantsHeight, 7), bg);
      canvas.translate(5, 5);

      RRect buttonBounds = RRect.makeLTRB(0, 0, 30, 20, 2);
      Rect labelBounds = Rect.makeLTRB(35, 0, 225, 20);
      
      if (scene._variants.size() > 1) {
        labelBounds = Rect.makeLTRB(35, 0, 225, 20);
        canvas.drawRRect(buttonBounds, bg);
        SkiaScene.drawStringCentered(canvas, "↑↓", 15, 10, SkiaScene.inter13, fg);
        SkiaScene.drawStringLeft(canvas, (scene._variantIdx + 1) + "/" + scene._variants.size() + " " + scene.variantTitle(), labelBounds, SkiaScene.inter13, fg);
        canvas.translate(0, 25);
    }
      
      canvas.drawRRect(buttonBounds, bg);
      drawStringCentered(canvas, "V", 15, 10, inter13, fg);
      drawStringLeft(canvas, "V-Sync: " + (vsync ? "ON" : "OFF"), labelBounds, inter13, fg);
      canvas.translate(0, 25);

      // Stats
      canvas.drawRRect(buttonBounds, bg);
      drawStringCentered(canvas, "S", 15, 10, inter13, fg);
      drawStringLeft(canvas, "Stats: " + (Stats.enabled ? "ON" : "OFF"), labelBounds, inter13, fg);
      canvas.translate(0, 25);

      // GC
      canvas.drawRRect(buttonBounds, bg);
      drawStringCentered(canvas, "G", 15, 10, inter13, fg);
      drawStringLeft(canvas, "GC objects: " + allocated, labelBounds, inter13, fg);
      canvas.translate(0, 25);

      // Native calls
      drawStringLeft(canvas, "Native calls: " + nativeCalls, labelBounds, inter13, fg);
      canvas.translate(0, 25);

      // fps
      canvas.drawRRect(RRect.makeLTRB(0, 0, times.length, 45, 2), bg);
      for (int i = 0; i < times.length; ++i) {
        canvas.drawLine(i, 45, i, 45 - (float) times[i], i > timesIdx ? graphPast : graph);
      }

      for (int refreshRate : new int[] { 30, 60, 120 }) {
        float frameTime = 1000f / refreshRate;
        canvas.drawLine(0, 45 - frameTime, times.length, 45 - frameTime, graphLimit);
      }

      String time = String.format("%.1fms", Arrays.stream(times).takeWhile(t -> t > 0).average().orElse(0d));
      drawStringLeft(canvas, time, Rect.makeLTRB(times.length + 5, 0, 225, 20), inter13, fg);
      String fps = String.format("%.0f fps", 1000.0 / Arrays.stream(times).takeWhile(t -> t > 0).average().orElse(0d));
      drawStringLeft(canvas, fps, Rect.makeLTRB(times.length + 5, 25, 225, 40), inter13, fg);
      canvas.translate(0, 25);
    }
  }

  @Override
  public void draw(Canvas canvas, int width, int height, float dpi, int xpos, int ypos) {
    draw(canvas, width, height);
  }
}
