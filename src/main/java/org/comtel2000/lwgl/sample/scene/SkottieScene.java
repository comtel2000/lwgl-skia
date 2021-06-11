package org.comtel2000.lwgl.sample.scene;

import java.lang.invoke.MethodHandles;
import java.util.Objects;

import org.jetbrains.skija.Canvas;
import org.jetbrains.skija.Paint;
import org.jetbrains.skija.PaintMode;
import org.jetbrains.skija.Rect;
import org.jetbrains.skija.skottie.Animation;
import org.jetbrains.skija.skottie.AnimationBuilder;
import org.jetbrains.skija.skottie.AnimationBuilderFlag;
import org.jetbrains.skija.skottie.LogLevel;
import org.jetbrains.skija.skottie.Logger;
import org.jetbrains.skija.skottie.RenderFlag;
import org.jetbrains.skija.sksg.InvalidationController;
import org.slf4j.LoggerFactory;

/**
 * sample from https://github.com/JetBrains/skija/tree/master/examples/scenes/src
 *
 */
public class SkottieScene extends SkiaScene {

  private static org.slf4j.Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private Animation animation;
  private InvalidationController ic = new InvalidationController();
  private String animationVariant;
  private String error;


  public SkottieScene() {
    readVariants(SkottieScene.class.getResource("/animations"));
  }

  @Override
  public void draw(Canvas canvas, int width, int height, float dpi, int xpos, int ypos) {
    if (!Objects.equals(animationVariant, variantTitle())) {
      if (animation != null)
        animation.close();
      animation = null;
      error = null;

      try {
        animation = new AnimationBuilder(AnimationBuilderFlag.DEFER_IMAGE_LOADING, AnimationBuilderFlag.PREFER_EMBEDDED_FONTS).setLogger(new Logger() {
          @Override
          public void log(LogLevel level, String message, String json) {
            logger.warn("{}: {}\n{}", level, message, json);
          }
        }).buildFromString(asString(SkottieScene.class.getResource("/animations/" + variantTitle())));
      } catch (IllegalArgumentException e) {
      }
      animationVariant = variantTitle();
    }

    if (animation == null) {
      drawStringCentered(canvas, "animations/" + variantTitle() + ": " + error, width / 2f, height / 2f, inter13, blackFill);
      return;
    }

    float progress = (System.currentTimeMillis() % (long) (1000 * animation.getDuration())) / (1000 * animation.getDuration());
    ic.reset();
    animation.seek(progress, ic);

    var animationWidth = width - 20;
    var animationHeight = animationWidth / animation.getWidth() * animation.getHeight();
    if (animationHeight > height - 20) {
      animationWidth /= animationHeight / (height - 20);
      animationHeight = height - 20;
    }
    var scale = animationWidth / animation.getWidth();

    var bounds = Rect.makeXYWH((width - animationWidth) / 2f, (height - animationHeight) / 2f - 2, animationWidth, animationHeight);
    try (var paint = new Paint().setColor(0xFF64C7BE).setMode(PaintMode.STROKE).setStrokeWidth(1)) {
      canvas.drawRect(Rect.makeXYWH(bounds.getLeft() - 0.5f, bounds.getTop() - 0.5f, bounds.getWidth() + 1f, bounds.getHeight() + 1f), paint);

      paint.setMode(PaintMode.FILL);
      canvas.drawRect(Rect.makeXYWH(bounds.getLeft() - 1, bounds.getBottom(), ((bounds.getWidth() + 2) * progress), 4), paint);

      animation.render(canvas, bounds, RenderFlag.SKIP_TOP_LEVEL_ISOLATION);

      paint.setColor(0x40CC3333).setMode(PaintMode.STROKE).setStrokeWidth(4);
      Rect dirtyRect = ic.getBounds().scale(scale).offset(bounds.getLeft(), bounds.getTop()).intersect(bounds);
      if (dirtyRect != null)
        canvas.drawRect(dirtyRect, paint);
    }
  }
}
