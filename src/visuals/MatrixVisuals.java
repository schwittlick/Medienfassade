package visuals;

import processing.core.PApplet;
import processing.core.PGraphics;

public class MatrixVisuals extends Visuals {

  private int W = 280;
  private int H = 90;
  private int S = 2;
  private int C = W / S;
  private int R = H / S;

  private int[][] curr; // current generation of ca
  private int[][] prev; // previous generation of ca
  private int[] palette; // 256 colors

  public MatrixVisuals(PApplet parentApplet, PGraphics offscreenBuffer) {
    super(parentApplet, offscreenBuffer);
    init();
  }

  public void init() {
    curr = new int[R][C];
    prev = new int[R][C];
    populate();
    palette = new int[256];
    for (int i = 0; i < 256; i++) {
      int g = (int) (PApplet.pow(i / 256.0f, 2.2f) * 255.0);
      palette[i] = parentApplet.color(g / 6, g, g / 3);
    }
  }

  private void populate() {
    for (int r = 0; r < R; r++) {
      for (int c = 0; c < C; c++) {
        prev[r][c] = curr[r][c] = (int) parentApplet.random(256);
      }
    }
  }

  public void run() {
    offscreenBuffer.beginDraw();
    
    update();
    render();
    swap();
    
    offscreenBuffer.endDraw();
  }

  void update() {
    for (int r = 0; r < R; r++) {
      for (int c = 0; c < C; c++) {
        int state = curr[r][c];
        int lr = prev[r][(c - 1 + C) % C] + prev[r][(c + 1) % C];
        int tb = prev[(r - 1 + R) % R][c] + prev[(r + 1) % R][c];
        // here's the "one-liner" version for mode 0:
        // curr[r][c] = (tb==0) ? (state+lr)&0xff : (lr%tb)&0xff;
        // the first equation above adds new "life" to the ca
        // the second equation gives the "matrix"-y effect

        // here's the version that supports the various modes:
        int breed = 0;

        breed = (state + lr) & 0xff;
        curr[r][c] = (tb == 0) ? breed : (lr % tb) & 0xff;
      }
    }
  }

  void render() {
    for (int r = 0, y = 0; r < R; y += S, r++) {
      for (int c = 0, x = 0; c < C; x += S, c++) {
        offscreenBuffer.fill(palette[curr[r][c]]);
        offscreenBuffer.rect(x, y, S, S);
      }
    }
  }

  void swap() {
    int[][] temp = curr;
    curr = prev;
    prev = temp;
  }
}
