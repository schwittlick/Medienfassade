package visuals;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;

public class WaterVisuals extends Visuals {
  private int heightMap[][][]; // water surface (2 pages).
  private int turbulenceMap[][]; // turbulence map
  private int line[]; // line optimizer;
  private int space;
  private int radius, heightMax, density;
  private int page = 0;
  private PImage water;

  public WaterVisuals(PApplet parentApplet, PGraphics offscreenBuffer) {
    super(parentApplet, offscreenBuffer);
  }

  public void init() {
    initWater();
    initMap();
  }

  void initWater() {
    float zoff = 0;
    water = new PImage(offscreenBuffer.width, offscreenBuffer.height);
    water.loadPixels();
    for (int y = 0; y < offscreenBuffer.height; y++) {
      for (int x = 0; x < offscreenBuffer.width; x++) {
        zoff += 0.0001f;
        float bright = ((parentApplet.noise(x * 0.01f, y * 0.01f, zoff)) * 255);
        water.pixels[x + y * offscreenBuffer.width] = 0xFF000000 | ((int) bright);
      }
    }
    water.updatePixels();
    water.filter(PApplet.BLUR, 4.5f);
    offscreenBuffer.noFill();
    //offscreenBuffer.stroke(0, 0, 255);
    parentApplet.stroke(0,0,255);
  }

  void initMap() {
    // the height map is made of two "pages".
    // one to calculate the current state, and another to keep the previous state.
    heightMap = new int[2][offscreenBuffer.width][offscreenBuffer.height];
    line = new int[offscreenBuffer.height];
    for (int l = 0; l < offscreenBuffer.height; l++) {
      line[l] = l * offscreenBuffer.width;
    }
    density = 5;
    radius = 20;
    space = offscreenBuffer.width * offscreenBuffer.height - 1;

    // the turbulence map, is an array to make a smooth turbulence over the height map.
    turbulenceMap = new int[radius * 2][radius * 2]; // turbulence map.
    int r = radius * radius;
    int squarex, squarey;
    double dist;

    for (int x = -radius; x < radius; x++) {
      squarex = x * x;
      for (int y = -radius; y < radius; y++) {
        squarey = y * y;
        dist = Math.sqrt(squarex + squarey);
        if ((squarex) + (squarey) < r) {
          turbulenceMap[radius + x][radius + y] += (int) (900 * ((float) radius - dist));
        }
      }
    }
  }


  public void run() {
    waterFilter();
    updateWater();
    page ^= 1; // page switching.
  }
  
  public void mouseMoved(){
    makeTurbulence((int) (PApplet.map(parentApplet.mouseX, 0, parentApplet.width, 0,
      offscreenBuffer.width)), (int) (PApplet.map(parentApplet.mouseY, 0, parentApplet.height,
      0, offscreenBuffer.height)));
  }

  private void waterFilter() {
    for (int x = 0; x < offscreenBuffer.width; x++) {
      for (int y = 0; y < offscreenBuffer.height; y++) {
        int n = y - 1 < 0 ? 0 : y - 1;
        int s = y + 1 > (offscreenBuffer.height) - 1 ? (offscreenBuffer.height) - 1 : y + 1;
        int e = x + 1 > (offscreenBuffer.width) - 1 ? (offscreenBuffer.width) - 1 : x + 1;
        int w = x - 1 < 0 ? 0 : x - 1;

        // water filter. I used to thought that this effect
        // had something to do with physics... :)

        // it a kind of image filter, but instead of applying to an image,
        // we apply it to the height map, that encodes the height of the waves.
        int value =
            ((heightMap[page][w][n] + heightMap[page][x][n] + heightMap[page][e][n]
                + heightMap[page][w][y] + heightMap[page][e][y] + heightMap[page][w][s]
                + heightMap[page][x][s] + heightMap[page][e][s]) >> 2)
                - heightMap[page ^ 1][x][y];

        heightMap[page ^ 1][x][y] = value - (value >> density);
      }
    }
  }

  @SuppressWarnings("deprecation")
  private void updateWater() {
    offscreenBuffer.loadPixels();
    PApplet.arraycopy(water.pixels, offscreenBuffer.pixels); // not really needed...
    for (int y = 0; y < offscreenBuffer.height - 1; y++) {
      for (int x = 0; x < offscreenBuffer.width - 1; x++) {
        // using the heightmap to distort underlying image
        int deltax = heightMap[page][x][y] - heightMap[page][(x) + 1][y];
        int deltay = heightMap[page][x][y] - heightMap[page][x][(y) + 1];

        int offsetx = (deltax >> 3) + x;
        int offsety = (deltay >> 3) + y;

        offsetx =
            offsetx > offscreenBuffer.width ? offscreenBuffer.width - 1 : offsetx < 0 ? 0 : offsetx;
        offsety =
            offsety > offscreenBuffer.height ? offscreenBuffer.height - 1 : offsety < 0
                ? 0
                : offsety;

        int offset = (offsety * offscreenBuffer.width) + offsetx;
        offset = offset < 0 ? 0 : offset > space ? space : offset;
        // Getting the water pixel with distortion and...
        // apply some fake lightning, in true color.
        int pixel = water.pixels[offset];
        int red = (pixel >> 16) & 0xff;
        int green = (pixel >> 8) & 0xff;
        int blue = (pixel) & 0xff;
        int light = (deltax + deltay) >> 6;
        red += light;
        green += light;
        blue += light;
        red = red > 255 ? 255 : red < 0 ? 0 : red;
        green = green > 255 ? 255 : green < 0 ? 0 : green;
        blue = blue > 255 ? 255 : blue < 0 ? 0 : blue;
        // updating our image source.
        offscreenBuffer.pixels[line[y] + x] = 0xff000000 | (red << 16) | (green << 8) | blue;
      }
    }
    offscreenBuffer.updatePixels();
  }

  void makeTurbulence(int cx, int cy) {
    int r = radius * radius;
    int left = cx < radius ? -cx + 1 : -radius;
    int right =
        cx > (offscreenBuffer.width - 1) - radius ? (offscreenBuffer.width - 1) - cx : radius;
    int top = cy < radius ? -cy + 1 : -radius;
    int bottom =
        cy > (offscreenBuffer.height - 1) - radius ? (offscreenBuffer.height - 1) - cy : radius;

    for (int x = left; x < right; x++) {
      int xsqr = x * x;
      for (int y = top; y < bottom; y++) {
        if ((xsqr) + (y * y) < r)
          heightMap[page ^ 1][cx + x][cy + y] += turbulenceMap[radius + x][radius + y];
      }
    }
  }


}
