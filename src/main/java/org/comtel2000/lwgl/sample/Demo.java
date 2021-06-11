package org.comtel2000.lwgl.sample;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_G;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UP;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_V;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwGetWindowContentScale;
import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.glfw.GLFW.glfwSetScrollCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.io.PrintStream;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Objects;

import org.comtel2000.lwgl.sample.scene.HUD;
import org.comtel2000.lwgl.sample.scene.SkiaScene;
import org.comtel2000.lwgl.sample.scene.SkottieScene;
import org.jetbrains.skija.BackendRenderTarget;
import org.jetbrains.skija.Canvas;
import org.jetbrains.skija.ColorSpace;
import org.jetbrains.skija.DirectContext;
import org.jetbrains.skija.FramebufferFormat;
import org.jetbrains.skija.IRect;
import org.jetbrains.skija.PixelGeometry;
import org.jetbrains.skija.Surface;
import org.jetbrains.skija.SurfaceColorFormat;
import org.jetbrains.skija.SurfaceOrigin;
import org.jetbrains.skija.SurfaceProps;
import org.jetbrains.skija.impl.Library;
import org.jetbrains.skija.impl.Stats;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SKIJA Demo App
 * 
 * <li>-Dorg.lwjgl.util.Debug=true</li>
 * <li>-Dskija.logLevel=DEBUG</li>
 * <li>-Djava.awt.headless=true</li>
 * <li>-enableassertions</li>
 * <li>-enablesystemassertions</li>
 * <li>-Xcheck:jni</li>
 * 
 * @author comtel
 *
 */
public class Demo {

  private static Logger logger = LoggerFactory.getLogger(Demo.class);


  private long window;
  private int width;
  private int height;
  private float dpi = 1f;
  private int xpos = 0;
  private int ypos = 0;
  private boolean vsync = true;
  private boolean stats = true;

  private String os = System.getProperty("os.name").toLowerCase();
  private SkiaScene skiaScene;
  private HUD hud;

  public void run(IRect bounds) {

    createWindow(bounds);
    loop();

    glfwFreeCallbacks(window);
    glfwDestroyWindow(window);
    glfwTerminate();

    Objects.requireNonNull(glfwSetErrorCallback(null)).free();
  }

  private void updateDimensions(boolean centerScreen) {
    try (MemoryStack stack = stackPush()) {
      IntBuffer pWidth = stack.mallocInt(1);
      IntBuffer pHeight = stack.mallocInt(1);
      glfwGetWindowSize(window, pWidth, pHeight);

      FloatBuffer xScale = stack.mallocFloat(1);
      FloatBuffer yScale = stack.mallocFloat(1);
      glfwGetWindowContentScale(window, xScale, yScale);

      this.width = (int) (pWidth.get(0) / xScale.get(0));
      this.height = (int) (pHeight.get(0) / yScale.get(0));
      this.dpi = xScale.get(0);

      logger.info("FramebufferSize {}x{}, scale {}, window {}x{}", pWidth.get(0), pHeight.get(0), this.dpi, this.width, this.height);

      if (centerScreen) {
        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(window, (vidmode.width() - pWidth.get(0)) / 2, (vidmode.height() - pHeight.get(0)) / 2);
      }
    }
  }


  private void createWindow(IRect bounds) {
    glfwDefaultWindowHints(); // optional, the current window hints are already the default
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
    glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

    window = glfwCreateWindow(bounds.getWidth(), bounds.getHeight(), "SMC Skija Demo", NULL, NULL);
    if (window == NULL)
      throw new RuntimeException("Failed to create the GLFW window");

    glfwSetKeyCallback(window, (win, key, scancode, action, mods) -> {
      if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
        glfwSetWindowShouldClose(win, true);
    });

    glfwSetWindowPos(window, bounds.getLeft(), bounds.getTop());
    updateDimensions(true);
    xpos = width / 2;
    ypos = height / 2;

    glfwMakeContextCurrent(window);
    glfwSwapInterval(vsync ? 1 : 0); // Enable v-sync
    glfwShowWindow(window);
  }

  private DirectContext context;
  private BackendRenderTarget renderTarget;
  private Surface surface;
  private Canvas canvas;

  private void initSkia() {
    Stats.enabled = stats;

    if (surface != null) {
      surface.close();
    }
    if (renderTarget != null) {
      renderTarget.close();
    }

    renderTarget = BackendRenderTarget.makeGL((int) (width * dpi), (int) (height * dpi),
        /* samples */0, /* stencil */8, /* fbId */0, FramebufferFormat.GR_GL_RGBA8);

    surface = Surface.makeFromBackendRenderTarget(context, renderTarget, SurfaceOrigin.BOTTOM_LEFT, SurfaceColorFormat.RGBA_8888, ColorSpace.getDisplayP3(),
        new SurfaceProps(PixelGeometry.RGB_H));

    canvas = surface.getCanvas();
  }

  private void draw() {
    canvas.save();
    skiaScene.draw(canvas, width, height, 1.0f, 0, 0);
    canvas.restore();

    if (Stats.enabled) {
      hud.setScene(skiaScene);
      hud.tick();
      hud.setVsync(vsync);
      canvas.save();
      hud.draw(canvas, width, height);
      canvas.restore();
    }
    context.flush();
    glfwSwapBuffers(window);
  }

  private void loop() {
    GL.createCapabilities();
    if ("false".equals(System.getProperty("skija.staticLoad"))) {
      System.out.println("skija.staticLoad");
      Library.load();
    }

    glClearColor(0.5f, 0.5f, 0.5f, 0.0f);

    context = DirectContext.makeGL();
    hud = new HUD();

    GLFW.glfwSetWindowSizeCallback(window, (win, w, h) -> {
      updateDimensions(false);
      initSkia();
      draw();
    });

    glfwSetCursorPosCallback(window, (win, x, y) -> {
      if (os.contains("mac") || os.contains("darwin")) {
        this.xpos = (int) x;
        this.ypos = (int) y;
      } else {
        this.xpos = (int) (x / dpi);
        this.ypos = (int) (y / dpi);
      }
    });

    glfwSetMouseButtonCallback(window, (win, button, action, mods) -> {
      System.out.println("Button " + button + " " + (action == 0 ? "released" : "pressed"));
    });

    glfwSetScrollCallback(window, (win, xoffset, yoffset) -> {
      System.out.println("Scrolled " + (float) xoffset * dpi + " " + (float) yoffset * dpi);
    });

    glfwSetKeyCallback(window, (win, key, scancode, action, mods) -> {
      if (action == GLFW_PRESS) {
        switch (key) {
          case GLFW_KEY_LEFT:
            break;
          case GLFW_KEY_RIGHT:
            break;
          case GLFW_KEY_UP:
            skiaScene.changeVariant(1);
            break;
          case GLFW_KEY_DOWN:
            skiaScene.changeVariant(-1);
            break;
          case GLFW_KEY_V:
            vsync = !vsync;
            glfwSwapInterval(vsync ? 1 : 0);
            break;
          case GLFW_KEY_S:
            stats = !stats;
            Stats.enabled = stats;
            break;
          case GLFW_KEY_G:
            System.out.println("Before GC " + Stats.allocated);
            System.gc();
            break;
          default:
            break;
        }
      }
    });
    initSkia();

    hud = new HUD();
    skiaScene = new SkottieScene();// new ShadersScene();

    while (!glfwWindowShouldClose(window)) {
      glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
      draw();
      glfwPollEvents();
    }
  }

  public static void main(String[] args) throws Exception {

    GLFWErrorCallback.createPrint(new PrintStream(new LoggingOutputStream(logger, true)));

    if (!glfwInit())
      throw new IllegalStateException("Unable to initialize GLFW");

    GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
    int width = (int) (vidmode.width() * 0.75);
    int height = (int) (vidmode.height() * 0.75);
    IRect bounds = IRect.makeXYWH(Math.max(0, (vidmode.width() - width) / 2), Math.max(0, (vidmode.height() - height) / 2), width, height);
    new Demo().run(bounds);
  }
}
