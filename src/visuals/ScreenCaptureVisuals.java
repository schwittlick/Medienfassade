package visuals;

import com.onformative.screencapturer.ScreenCapturer;

import processing.core.PApplet;
import processing.core.PGraphics;

public class ScreenCaptureVisuals extends Visuals {

  private ScreenCapturer cap;

  public ScreenCaptureVisuals(PApplet parentApplet, PGraphics offscreenBuffer) {
    super(parentApplet, offscreenBuffer);
  }

  public void init() {
    cap = new ScreenCapturer(offscreenBuffer.width, offscreenBuffer.height, 30);
  }

  public void run() {
    offscreenBuffer.beginDraw();

    offscreenBuffer.image(cap.getImage(), 0, 0);

    offscreenBuffer.endDraw();
  }
}
  