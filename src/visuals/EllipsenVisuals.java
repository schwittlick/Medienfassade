package visuals;

import processing.core.PApplet;
import processing.core.PGraphics;

public class EllipsenVisuals extends Visuals {

  private boolean gradient = false;

  public EllipsenVisuals(PApplet parentApplet, PGraphics offscreenBuffer) {
    super(parentApplet, offscreenBuffer);
  }

  public void init() {

  }

  public void run() {
    offscreenBuffer.background(0, 0, 100);
    offscreenBuffer.beginDraw();
    offscreenBuffer.colorMode(PApplet.HSB,100);
//offscreenBuffer.
    float stepSize = 4;
    for (int x = 0; x <= offscreenBuffer.width; x += stepSize) {
      for (int y = 0; y <= offscreenBuffer.height; y += stepSize) {
        float distanceToMouse =
            PApplet
                .dist(x, y, PApplet.map(parentApplet.mouseX, 0, parentApplet.width, 0,
                    offscreenBuffer.width), PApplet.map(parentApplet.mouseY, 0,
                    parentApplet.height, 0, offscreenBuffer.height));
        float ellipseDiameter = distanceToMouse * 0.2f;
        if (gradient) {
          offscreenBuffer.fill(0, 0, 100 - distanceToMouse * 0.3f);
        } else {
          offscreenBuffer.fill(0);
        }
        offscreenBuffer.ellipse(x, y, ellipseDiameter, ellipseDiameter);
      }
    }
    offscreenBuffer.endDraw();
    offscreenBuffer.colorMode(PApplet.RGB, 255);

    if (parentApplet.mousePressed) {
      gradient = !gradient;
    }
  }

}
