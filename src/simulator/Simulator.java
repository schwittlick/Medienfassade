package simulator;

import java.awt.Dimension;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import toxi.geom.mesh.*;
import peasy.*;
import toxi.processing.*;
import utilities.STLParser;
import visuals.BesucherInnenAussenVisuals;
import visuals.EllipsenVisuals;
import visuals.FluidVisuals;
import visuals.MatrixVisuals;
import visuals.ScreenCaptureVisuals;
import visuals.Visuals;
import visuals.WaterVisuals;

@SuppressWarnings({"unused", "serial"})
public class Simulator extends PApplet {

  private final Dimension ledDimension = new Dimension(280, 90);
  private boolean isCapture = false;

  private STLParser haus;
  private STLParser kamera;
  private ToxiclibsSupport gfx;
  private PGraphics offscreenBuffer;
  private PeasyCam peasyCam;

  private Visuals visual;

  /**
   * this method is called once in the beginning
   */
  public void setup() {
    size(1000, 600, OPENGL);
    frameRate(500);
    peasyCam = new PeasyCam(this, 900);
    peasyCam.setResetOnDoubleClick(false);
    peasyCam.rotateX(radians(90));
    peasyCam.rotateZ(radians(180));
    gfx = new ToxiclibsSupport(this);
    offscreenBuffer = createGraphics(ledDimension.width, ledDimension.height, P3D);
    visual = new WaterVisuals(this, offscreenBuffer);

    initModels();
  }


  private void initModels() {
    haus = new STLParser(this, sketchPath("data\\medienfassade_haus_ground3_incl_people.stl"));
    haus.scale(5);

    kamera = new STLParser(this, sketchPath("data\\surveillance_cam1.stl"));
    kamera.scale(0.4f);
  }

  /**
   * this method is called constantly in a interval depending on the current frame rate
   */
  public void draw() {
    frame.setTitle((int) frameRate + "fps");
    background(60);

    // drawAxis();
    drawLighting();
    drawModels();
    drawVisuals();
    drawLEDMatrix();
    peasyCam.beginHUD();
    image(offscreenBuffer, 0, height-offscreenBuffer.height);
    peasyCam.endHUD();
  }

  private void drawAxis() {
    strokeWeight(1);
    stroke(255, 0, 0);
    line(0, 0, 0, 800, 0, 0);
    stroke(0, 255, 0);
    line(0, 0, 0, 0, 800, 0);
    stroke(0, 0, 255);
    line(0, 0, 0, 0, 0, 800);
    noStroke();
  }

  private void drawLighting() {
    pointLight(255, 255, 255, 0, -281, 150);
    ambientLight(128, 128, 128);
  }

  private void drawModels() {
    noStroke();
    // rendering house
    haus.drawShape();

    // rendering the camera attached to the house
    pushMatrix();
    translate(0, -251, 100);
    rotateZ(radians(90));
    kamera.drawShape();
    popMatrix();
  }

  private void drawVisuals() {
    visual.run();
  }

  private void drawLEDMatrix() {
    pushMatrix();
    rotateX(radians(-90));
    rotateY(radians(180));
    strokeWeight(2);
    translate(-147, -226, 240);
    noFill();
    strokeWeight(1);
    offscreenBuffer.loadPixels();
    for (int i = 0; i < offscreenBuffer.height; i++) {
      for (int j = 0; j < offscreenBuffer.width; j++) {
        int pos = i * offscreenBuffer.width + j;
        stroke(offscreenBuffer.pixels[pos]);
        point(j, i);
      }
    }
    popMatrix();
  }

  /**
   * this method is called once a key on the keyboard has been pressed.
   */
  public void keyPressed() {
    if (key == 's') {
      // ScreenCaptureVisuals tmp = (ScreenCaptureVisuals) visual;
      // tmp.getCapturer().setVisible(false);
    }

    changeVisuals(key);
  }



  private void changeVisuals(char keyPressed) throws NumberFormatException {
    int key;
    try {
      key = Integer.parseInt(keyPressed + "");
    } catch (NumberFormatException e) {
      key = 0;
    }

    switch (key) {
      case 1:
        visual = new ScreenCaptureVisuals(this, offscreenBuffer);
        break;
      case 2:
        visual = new MatrixVisuals(this, offscreenBuffer);
        break;
      case 3:
        visual = new WaterVisuals(this, offscreenBuffer);
        break;
      case 4:
        visual = new BesucherInnenAussenVisuals(this, offscreenBuffer);
        break;
      case 5:
        visual = new EllipsenVisuals(this, offscreenBuffer);
        break;
      case 6:
        // visual = new FluidVisuals(this, offscreenBuffer);
        break;
    }
  }

  public void mouseMoved() {
    visual.mouseMoved();
  }

  static public void main(String args[]) {
    PApplet.main(new String[] { /* "--present", */"simulator.Simulator"});
  }
}
