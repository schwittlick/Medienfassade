package visuals;

import processing.core.PApplet;
import processing.core.PGraphics;

public class BesucherInnenAussenVisuals extends Visuals {

  public BesucherInnenAussenVisuals(PApplet parentApplet, PGraphics offscreenBuffer) {
    super(parentApplet, offscreenBuffer);
  }

  private int noiseValues[];
  private float increment = 0.01f;
  private float zoff = 0.0f;
  private float zincrement = 0.02f;

  public void init() {
    noiseValues = new int[offscreenBuffer.width];
    offscreenBuffer.stroke(255);
    offscreenBuffer.noFill();
  }

  public void run() {
    updateNoiseField();
    offscreenBuffer.background(0);
   
    offscreenBuffer.beginDraw();
    offscreenBuffer.pushMatrix();
    offscreenBuffer.translate(0, PApplet.map(parentApplet.mouseY, 0, parentApplet.height, -100, 100));
    for (int i = 0; i < offscreenBuffer.width; i++) {
      //offscreenBuffer.point(i, noiseValues[i]);
      offscreenBuffer.line(i, offscreenBuffer.height, i, noiseValues[i]);
    }
    offscreenBuffer.popMatrix();
    offscreenBuffer.noStroke();
    offscreenBuffer.endDraw();
  }

  private void updateNoiseField() {
    parentApplet.noiseDetail(8, PApplet.map(parentApplet.mouseX, 0, parentApplet.width, 0, 1));
    float xoff = 0.0f;
    for (int x = 0; x < offscreenBuffer.width; x++) {
      xoff += increment;
      float yoff = 0.0f;
      for (int y = 0; y < 100; y++) {
        yoff += increment;
        float bright = parentApplet.noise(xoff, yoff, zoff) * 255;
        noiseValues[x] = (int)PApplet.map(bright, 0, 255, 0, offscreenBuffer.height);
      }
    }
    zoff += zincrement;
  }
}
