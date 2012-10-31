package visuals;

import msafluid.MSAFluidSolver2D;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.opengl.PGraphicsOpenGL;

import java.nio.FloatBuffer;

import javax.media.opengl.GL;


public class FluidVisuals extends Visuals {

  private final float FLUID_WIDTH = 200;

  private float invWidth, invHeight; // inverse of screen dimensions
  private float aspectRatio, aspectRatio2;
  private boolean renderUsingVA = true;
  private MSAFluidSolver2D fluidSolver;
  //private ParticleSystem particleSystem;
  private PImage imgFluid;
  private boolean drawFluid = true;

  public FluidVisuals(PApplet parentApplet, PGraphics offscreenBuffer) {
    super(parentApplet, offscreenBuffer);
    //init();
  }
/*
  public void init() {
    invWidth = 1.0f / offscreenBuffer.width;
    invHeight = 1.0f / offscreenBuffer.height;
    aspectRatio = offscreenBuffer.width * invHeight;
    aspectRatio2 = aspectRatio * aspectRatio;

    // create fluid and set options
    fluidSolver =
        new MSAFluidSolver2D((int) (FLUID_WIDTH),
            (int) (FLUID_WIDTH * offscreenBuffer.height / offscreenBuffer.width));
    fluidSolver.enableRGB(true).setFadeSpeed(0.003f).setDeltaT(0.5f).setVisc(0.0001f);

    // create image to hold fluid picture
    imgFluid =
        parentApplet.createImage(fluidSolver.getWidth(), fluidSolver.getHeight(), PConstants.RGB);

    // create particle system
    particleSystem = new ParticleSystem();
  }

  public void run() {
    fluidSolver.update();

    if (parentApplet.mousePressed) {
      for (int i = 0; i < fluidSolver.getNumCells(); i++) {
        int d = 2;
        imgFluid.pixels[i] =
            parentApplet.color(fluidSolver.r[i] * d, fluidSolver.g[i] * d, fluidSolver.b[i] * d);
      }
      imgFluid.updatePixels();// fastblur(imgFluid, 2);
      offscreenBuffer.beginDraw();
      offscreenBuffer.image(imgFluid, 0, 0, offscreenBuffer.width, offscreenBuffer.height);
      offscreenBuffer.endDraw();
    }

    particleSystem.updateAndDraw();
  }

  public void mouseMoved() {
    float mouseNormX = parentApplet.mouseX * invWidth;
    float mouseNormY = parentApplet.mouseY * invHeight;
    float mouseVelX = (parentApplet.mouseX - parentApplet.pmouseX) * invWidth;
    float mouseVelY = (parentApplet.mouseY - parentApplet.pmouseY) * invHeight;

    addForce(mouseNormX, mouseNormY, mouseVelX, mouseVelY);
  }

  // add force and dye to fluid, and create particles
  void addForce(float x, float y, float dx, float dy) {
    float speed = dx * dx + dy * dy * aspectRatio2; // balance the x and y components of speed with
                                                    // the screen aspect ratio

    if (speed > 0) {
      if (x < 0)
        x = 0;
      else if (x > 1) x = 1;
      if (y < 0)
        y = 0;
      else if (y > 1) y = 1;

      float colorMult = 5;
      float velocityMult = 30.0f;

      int index = fluidSolver.getIndexForNormalizedPosition(x, y);

      int drawColor;

      offscreenBuffer.colorMode(PConstants.HSB, 360, 1, 1);
      float hue = ((x + y) * 180 + parentApplet.frameCount) % 360;
      drawColor = parentApplet.color(hue, 1, 1);
      offscreenBuffer.colorMode(PConstants.RGB, 1);

      fluidSolver.rOld[index] += parentApplet.red(drawColor) * colorMult;
      fluidSolver.gOld[index] += parentApplet.green(drawColor) * colorMult;
      fluidSolver.bOld[index] += parentApplet.blue(drawColor) * colorMult;

      particleSystem.addParticles(x * offscreenBuffer.width, y * offscreenBuffer.height, 10);
      fluidSolver.uOld[index] += dx * velocityMult;
      fluidSolver.vOld[index] += dy * velocityMult;
    }
  }

  class ParticleSystem {
    FloatBuffer posArray;
    FloatBuffer colArray;

    final static int maxParticles = 5000;
    int curIndex;

    Particle[] particles;

    ParticleSystem() {
      particles = new Particle[maxParticles];
      for (int i = 0; i < maxParticles; i++)
        particles[i] = new Particle();
      curIndex = 0;

      posArray = BufferUtil.newFloatBuffer(maxParticles * 2 * 2);// 2 coordinates per point, 2
                                                                 // points per particle (current and
                                                                 // previous)
      colArray = BufferUtil.newFloatBuffer(maxParticles * 3 * 2);
    }


    void updateAndDraw() {
      PGraphicsOpenGL pgl = (PGraphicsOpenGL) parentApplet.g; // processings opengl graphics object
      GL gl = (GL) pgl.pgl; // JOGL's GL object

      gl.glEnable(GL.GL_BLEND); // enable blending
      if (!drawFluid) fadeToColor(gl, 0, 0, 0, 0.05f);

      gl.glBlendFunc(GL.GL_ONE, GL.GL_ONE); // additive blending (ignore alpha)
      gl.glEnable(GL.GL_LINE_SMOOTH); // make points round
      gl.glLineWidth(1);


      if (renderUsingVA) {
        for (int i = 0; i < maxParticles; i++) {
          if (particles[i].alpha > 0) {
            particles[i].update();
            particles[i].updateVertexArrays(i, posArray, colArray);
          }
        }
        gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
        gl.glVertexPointer(2, GL.GL_FLOAT, 0, posArray);

        gl.glEnableClientState(GL.GL_COLOR_ARRAY);
        gl.glColorPointer(3, GL.GL_FLOAT, 0, colArray);

        gl.glDrawArrays(GL.GL_LINES, 0, maxParticles * 2);
      } else {
        gl.glBegin(GL.GL_LINES); // start drawing points
        for (int i = 0; i < maxParticles; i++) {
          if (particles[i].alpha > 0) {
            particles[i].update();
            particles[i].drawOldSchool(gl); // use oldschool renderng
          }
        }
        gl.glEnd();
      }

      gl.glDisable(GL.GL_BLEND);
      pgl.endGL();
    }


    void addParticles(float x, float y, int count) {
      for (int i = 0; i < count; i++)
        addParticle(x + parentApplet.random(-15, 15), y + parentApplet.random(-15, 15));
    }

    void fadeToColor(GL gl, float r, float g, float b, float speed) {
      gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
      gl.glColor4f(r, g, b, speed);
      gl.glBegin(GL.GL_QUADS);
      gl.glVertex2f(0, 0);
      gl.glVertex2f(offscreenBuffer.width, 0);
      gl.glVertex2f(offscreenBuffer.width, offscreenBuffer.height);
      gl.glVertex2f(0, offscreenBuffer.height);
      gl.glEnd();
    }


    void addParticle(float x, float y) {
      particles[curIndex].init(x, y);
      curIndex++;
      if (curIndex >= maxParticles) curIndex = 0;
    }

  }

  class Particle {
    final static float MOMENTUM = 0.5f;
    final static float FLUID_FORCE = 0.6f;

    float x, y;
    float vx, vy;
    float radius; // particle's size
    float alpha;
    float mass;

    void init(float x, float y) {
      this.x = x;
      this.y = y;
      vx = 0;
      vy = 0;
      radius = 5;
      alpha = parentApplet.random(0.3f, 1);
      mass = parentApplet.random(0.1f, 1);
    }


    void update() {
      // only update if particle is visible
      if (alpha == 0) return;

      // read fluid info and add to velocity
      int fluidIndex = fluidSolver.getIndexForNormalizedPosition(x * invWidth, y * invHeight);
      vx = fluidSolver.u[fluidIndex] * offscreenBuffer.width * mass * FLUID_FORCE + vx * MOMENTUM;
      vy = fluidSolver.v[fluidIndex] * offscreenBuffer.height * mass * FLUID_FORCE + vy * MOMENTUM;

      // update position
      x += vx;
      y += vy;

      // bounce of edges
      if (x < 0) {
        x = 0;
        vx *= -1;
      } else if (x > offscreenBuffer.width) {
        x = offscreenBuffer.width;
        vx *= -1;
      }

      if (y < 0) {
        y = 0;
        vy *= -1;
      } else if (y > offscreenBuffer.height) {
        y = offscreenBuffer.height;
        vy *= -1;
      }

      // hackish way to make particles glitter when the slow down a lot
      if (vx * vx + vy * vy < 1) {
        vx = parentApplet.random(-1, 1);
        vy = parentApplet.random(-1, 1);
      }

      // fade out a bit (and kill if alpha == 0);
      alpha *= 0.999;
      if (alpha < 0.01) alpha = 0;
    }


    void updateVertexArrays(int i, FloatBuffer posBuffer, FloatBuffer colBuffer) {
      int vi = i * 4;
      posBuffer.put(vi++, x - vx);
      posBuffer.put(vi++, y - vy);
      posBuffer.put(vi++, x);
      posBuffer.put(vi++, y);

      int ci = i * 6;
      colBuffer.put(ci++, alpha);
      colBuffer.put(ci++, alpha);
      colBuffer.put(ci++, alpha);
      colBuffer.put(ci++, alpha);
      colBuffer.put(ci++, alpha);
      colBuffer.put(ci++, alpha);
    }


    void drawOldSchool(GL gl) {
      gl.glColor3f(alpha, alpha, alpha);
      gl.glVertex2f(x - vx, y - vy);
      gl.glVertex2f(x, y);
    }
  }*/
}
