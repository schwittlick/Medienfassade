package visuals;

import processing.core.PApplet;
import processing.core.PGraphics;

public class Visuals {

  public PGraphics offscreenBuffer;
  public PApplet parentApplet;

  public Visuals(PApplet parentApplet, PGraphics offscreenBuffer) {
    this.offscreenBuffer = offscreenBuffer;
    this.parentApplet = parentApplet;
    init();
  }

  public void init() {

  }

  public void run() {
    offscreenBuffer.beginDraw();

    offscreenBuffer.background(0);
    offscreenBuffer.noStroke();
    offscreenBuffer.fill(255);
    offscreenBuffer.rect(0, 0, 40, 40);

    offscreenBuffer.endDraw();
  }
  
  public void mouseMoved(){
    
  }
}
