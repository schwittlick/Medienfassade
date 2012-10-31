package utilities;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PShape;

import toxi.geom.Vec3D;
import toxi.geom.mesh.Face;
import toxi.geom.mesh.STLReader;
import toxi.geom.mesh.TriangleMesh;
import toxi.processing.ToxiclibsSupport;

public class STLParser {

  private byte[] buf = new byte[12];

  private final float bufferToFloat() {
    return Float.intBitsToFloat(bufferToInt());
  }

  private final int bufferToInt() {
    return byteToInt(buf[0]) | (byteToInt(buf[1]) << 8) | (byteToInt(buf[2]) << 16)
        | (byteToInt(buf[3]) << 24);
  }

  private final int byteToInt(byte b) {
    return (b < 0 ? 256 + b : b);
  }

  private Vec3D readVector(DataInputStream ds) throws IOException {
    ds.read(buf, 0, 4);
    float x = bufferToFloat();
    ds.read(buf, 0, 4);
    float y = bufferToFloat();
    ds.read(buf, 0, 4);
    float z = bufferToFloat();
    return new Vec3D(x, y, z);
  }

  private PApplet parent;
  private TriangleMesh stl;
  private PShape shape;
  private ToxiclibsSupport gfx;

  public STLParser(PApplet parentApplet, String fileName) {
    stl = null;
    parent = parentApplet;
    gfx = new ToxiclibsSupport(parent);
    try {
      stl =
          (TriangleMesh) loadSTLToTriangleMesh(new FileInputStream(fileName),
              fileName.substring(fileName.lastIndexOf('/') + 1));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

    shape = parent.createShape(PConstants.TRIANGLE);
    shape.fill(120);
    shape.noStroke( );
    Iterator<Face> it = stl.faces.iterator();
    while (it.hasNext()) {
      Face f = (Face) it.next();
      shape.vertex(f.a.x, f.a.y, f.a.z);
      shape.vertex(f.b.x, f.b.y, f.b.z);
      shape.vertex(f.c.x, f.c.y, f.c.z);
    }
    STLReader reader;
    shape.end();
  }

  public void drawShape() {
    parent.shape(shape);
  }

  public void drawMesh() {
    gfx.mesh(stl);
  }

  public void scale(float factor) {
    shape.scale(factor, factor, factor);
  }

  private TriangleMesh loadSTLToTriangleMesh(InputStream stream, String meshName) {
    TriangleMesh mesh = null;
    try {
      DataInputStream ds = new DataInputStream(stream);
      // read header, ignore color model
      for (int i = 0; i < 80; i++) {
        ds.read();
      }
      // read num faces
      ds.read(buf, 0, 4);
      int numFaces = bufferToInt();
      mesh = new TriangleMesh(meshName);
      for (int i = 0; i < numFaces; i++) {
        // ignore face normal
        ds.read(buf, 0, 12);
        // face vertices
        Vec3D a = readVector(ds);
        Vec3D b = readVector(ds);
        Vec3D c = readVector(ds);
        mesh.addFace(a, b, c);
        // ignore colour
        ds.read(buf, 0, 2);
      }
      mesh.computeVertexNormals();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return mesh;
  }
  
  private PShape loadSTLToShape(InputStream stream){
    PShape shape = null;
    try {
      DataInputStream ds = new DataInputStream(stream);
      // read header, ignore color model
      for (int i = 0; i < 80; i++) {
        ds.read();
      }
      // read num faces
      ds.read(buf, 0, 4);
      int numFaces = bufferToInt();
      shape = parent.createShape(PConstants.TRIANGLE);
      for (int i = 0; i < numFaces; i++) {
        // ignore face normal
        ds.read(buf, 0, 12);
        // face vertices
        Vec3D a = readVector(ds);
        Vec3D b = readVector(ds);
        Vec3D c = readVector(ds);
        
        shape.vertex(a.x, a.y, a.z);
        shape.vertex(b.x, b.y, b.z);
        shape.vertex(c.x, c.y, c.z);
        // ignore colour
        ds.read(buf, 0, 2);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    shape.end();
    return shape;
  }
  
  /**
   * TODO
   * @param model
   * @return
   *//*
  PShape objModelToPShape(OBJModel model) {
    PShape shape = createShape(QUADS);
    shape.noStroke();
    for (int i=0; i<model.getFaceCount(); i++) {
      PVector[] faceVerts = model.getFaceVertices(i);
      for (int j=0; j<faceVerts.length; j++) {
        shape.vertex(faceVerts[j].x, faceVerts[j].y, faceVerts[j].z);
      }
    }
    shape.end();
    return shape;
  }*/
}
