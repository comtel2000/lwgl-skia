module org.comtel2000.lwgl.sample {
  
  exports org.comtel2000.lwgl.sample.scene;
  exports org.comtel2000.lwgl.sample;

  requires javafx.base;
  requires javafx.controls;
  requires javafx.graphics;
  
  requires org.lwjgl;
  requires org.lwjgl.glfw;
  requires org.lwjgl.opengl;
  requires org.slf4j;
  
  requires org.jetbrains.skija.shared;
  requires org.jetbrains.skija.windows;
}
