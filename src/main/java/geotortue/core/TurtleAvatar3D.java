package geotortue.core;

import java.awt.Color;
import java.util.HashSet;
import java.util.Vector;

import fw.geometry.util.Point3D;
import fw.geometry.util.Point4D;
import fw.geometry.util.QRotation;
import fw.geometry.util.QRotation4D;
import fw.renderer.mesh.FVMesh;
import fw.renderer.mesh.Polyhedron;
import fw.renderer.mesh.Vertex;
import fw.renderer.shader.FlatShader;
import fw.renderer.shader.PhongShader;

public class TurtleAvatar3D {
	
	private GTPolyhedron shell, belly, head, legAR, legAL, legPR, legPL;

	private Color color;
	private final Color bellyColor = new Color(200, 150, 50);
	private final Color shellColor = new Color(150, 100, 25);

	private final double bellySize = 12;
	private final double bH = 0.82 * bellySize;
	private final double bW = bellySize*0.4;
	
	public TurtleAvatar3D(Color color) {
		this.color = color;
		this.shell = getShell();
		this.belly = getBelly();
		this.head = getHead();
		this.legAL = getLeg(1);
		this.legAR = getLeg(-1);
		this.legPL = getLegP(1);
		this.legPR = getLegP(-1);
	}
	
	public Vector<FVMesh> getMeshes(Point3D pos, QRotation r, double k) {
		Vector<FVMesh> meshes = new Vector<FVMesh>();
		meshes.add(new FVMesh(shell.getTransformedClone(pos, r, k), shellColor, new FlatShader()));
		meshes.add(new FVMesh(belly.getTransformedClone(pos, r, k), bellyColor, new FlatShader()));
		meshes.add(new FVMesh(head.getTransformedClone(pos, r, k), color, new PhongShader()));
		meshes.add(new FVMesh(legAR.getTransformedClone(pos, r, k), color, new PhongShader()));
		meshes.add(new FVMesh(legAL.getTransformedClone(pos, r, k), color, new PhongShader()));
		meshes.add(new FVMesh(legPR.getTransformedClone(pos, r, k), color, new PhongShader()));
		meshes.add(new FVMesh(legPL.getTransformedClone(pos, r, k), color, new PhongShader()));
		return meshes;
	}
	
	public Vector<FVMesh> getMeshes(Point3D pos, QRotation4D r, double k) {
		Vector<FVMesh> meshes = new Vector<FVMesh>();
		meshes.add(new FVMesh(shell.getTransformedClone(pos, r, k), shellColor, new FlatShader()));
		meshes.add(new FVMesh(belly.getTransformedClone(pos, r, k), bellyColor, new FlatShader()));
		meshes.add(new FVMesh(head.getTransformedClone(pos, r, k), color, new PhongShader()));
		meshes.add(new FVMesh(legAR.getTransformedClone(pos, r, k), color, new PhongShader()));
		meshes.add(new FVMesh(legAL.getTransformedClone(pos, r, k), color, new PhongShader()));
		meshes.add(new FVMesh(legPR.getTransformedClone(pos, r, k), color, new PhongShader()));
		meshes.add(new FVMesh(legPL.getTransformedClone(pos, r, k), color, new PhongShader()));
		return meshes;
		
		
	}
	
	private static class GTPolyhedron extends Polyhedron {
		
		private GTPolyhedron(Point3D[] vertices, HashSet<int[]> scheme) {
			super(vertices, scheme);
		}
		
		private GTPolyhedron(Point3D[] vertices) {
			super(vertices);
		}
		
		private GTPolyhedron getClone() {
			Vertex[] vertices = getVertices();
			Vertex[] verticesClone = new Vertex[vertices.length];
			for (int idx = 0; idx < vertices.length; idx++)
				verticesClone[idx] = new Vertex(vertices[idx]);
			return new GTPolyhedron(verticesClone, getScheme());
		}
		
		public GTPolyhedron getTransformedClone(Point3D pos, QRotation r, double k) {
			GTPolyhedron clone = getClone();
			clone.scale(k);
			clone.transform(r);
			clone.translate(pos);
			return clone;
		}

		public GTPolyhedron getTransformedClone(Point3D pos, QRotation4D r, double k) {
			GTPolyhedron clone = getClone();
			clone.scale(k);
			clone.transform(r);
			clone.translate(pos);
			return clone;
		}
		
		private void translate(Point3D p) {
			for (Vertex v : getVertices())
				v.translate(p);
		}

		private void transform(QRotation r) {
			for (Vertex v : getVertices())
				v.setCoordinates(r.apply(v));
		}
		
		private void transform(QRotation4D r) {
			for (Vertex v : getVertices()) {
				Point4D p = new Point4D(v.x, v.y, v.z, 0);
				p= r.apply(p);
				v.setCoordinates(new Point3D(p.x, p.y, p.z));
			}
		}
		
		private void scale(double k) {
			for (Vertex v : getVertices())
				v.scale(k);
		}
	}
	
	/*
	 * MESHES
	 */
	
	/*
	 * Belly
	 */
	private final Point3D[] bellyPs = new Point3D[]{
			new Point3D( 2*bW,   0, 0.1),
			new Point3D(   bW,  bH, 0.1),
			new Point3D(  -bW,  bH, 0.1),
			new Point3D(-2*bW,   0, 0.1),
			new Point3D(  -bW, -bH, 0.1),
			new Point3D(   bW, -bH, 0.1)
	};

	private GTPolyhedron getBelly() {
		GTPolyhedron poly = new GTPolyhedron(bellyPs);
		poly.addFace(5, 4, 3, 2, 1, 0);
		return poly;
	}

	/*
	 * Shell
	 */
	private GTPolyhedron getShell() {
		Point3D[] shellVs = new Point3D[18];
		for (int idx = 0; idx < 6; idx++)
			shellVs[idx] = bellyPs[idx];

		for (int idx = 6; idx < 12; idx++)
			shellVs[idx] = bellyPs[idx-6].getTranslated(0, 0, bellySize*0.4);

		for (int idx = 12; idx < 18; idx++)
			shellVs[idx] = bellyPs[idx-12].getScaled(0.5).getTranslated(0, 0, bellySize*0.7);
		
		GTPolyhedron poly = new GTPolyhedron(shellVs);
		poly.addFace(0, 1, 7, 6);
		poly.addFace(1, 2, 8, 7);
		poly.addFace(2, 3, 9, 8);
		poly.addFace(3, 4, 10, 9);
		poly.addFace(4, 5, 11, 10);
		poly.addFace(5, 0, 6, 11);
		poly.addFace(6, 7, 13, 12);
		poly.addFace(7, 8, 14, 13);
		poly.addFace(8, 9, 15, 14);
		poly.addFace(9, 10, 16, 15);
		poly.addFace(10, 11, 17, 16);
		poly.addFace(11, 6, 12, 17); 
		poly.addFace(12, 13, 14, 15, 16, 17);
		return poly;
	}

	/*
	 * Head
	 */
	double headSize = bellySize*0.16;
	private GTPolyhedron getHead() {
		Point3D[] headVs = new Point3D[21];
		
		headVs[0] = new Point3D( headSize, 0,  headSize);
		headVs[1] = new Point3D( headSize, 0, -headSize);
		headVs[2] = new Point3D(-headSize, 0, -headSize);
		headVs[3] = new Point3D(-headSize, 0,  headSize);

		for (int idx = 4; idx < 8; idx++)
			headVs[idx] = headVs[idx-4].getTranslated(0, -headSize*3, 0);
		
		for (int idx = 8; idx < 12; idx++)
			headVs[idx] = headVs[idx-8].getScaled(0.8).getTranslated(0, -headSize*3.2, 0);
		
		for (int idx = 12; idx < 16; idx++)
			headVs[idx] = headVs[idx-12].getScaled(0.8).getTranslated(0, -headSize*4.5, 0);
		
		double hW = 1.8*headSize;
		headVs[16] = new Point3D(  0,  headSize,   0);
		headVs[17] = new Point3D(  0, -headSize, -hW);
		headVs[18] = new Point3D(  0, -headSize,  hW);
		headVs[19] = new Point3D(-hW, -headSize,   0);
		headVs[20] = new Point3D( hW, -headSize,   0);
		
		QRotation r = QRotation.getYRotation(Math.PI/4);
		for (int idx = 0; idx < 21; idx++)
			headVs[idx] = r.apply(headVs[idx]).getTranslated(0, bH+headSize*4.5, bellySize*0.2);
		
		GTPolyhedron poly = new GTPolyhedron(headVs);
		poly.addFace(4, 8, 9, 5);
		poly.addFace(5, 9, 10, 6);
		poly.addFace(6, 10, 11, 7);
		poly.addFace(7, 11, 8, 4);
		poly.addFace(8, 12, 13, 9);
		poly.addFace(9, 13, 14, 10);
		poly.addFace(10, 14, 15, 11);
		poly.addFace(11, 15, 12, 8); 
		poly.addFace(16, 0, 1);
		poly.addFace(16, 1, 2);
		poly.addFace(16, 2, 3);
		poly.addFace(16, 3, 0);
		poly.addFace(17, 1, 5);
		poly.addFace(17, 5, 6);
		poly.addFace(17, 6, 2);
		poly.addFace(17, 2, 1);
		poly.addFace(18, 4, 0);
		poly.addFace(18, 0, 3);
		poly.addFace(18, 3, 7);
		poly.addFace(18, 7, 4); 
		poly.addFace(19, 3, 2);
		poly.addFace(19, 2, 6);
		poly.addFace(19, 6, 7);
		poly.addFace(19, 7, 3); 
		poly.addFace(20, 1, 0);
		poly.addFace(20, 0, 4);
		poly.addFace(20, 4, 5);
		poly.addFace(20, 5, 1);
		return poly;
	}
	
	/*
	 * Legs A
	 */
	private GTPolyhedron getLeg(int e) {
		double legSize = headSize;
		Point3D[] legVs = new Point3D[12];
		
		legVs[0] = new Point3D( legSize*0.7, 0,  legSize);
		legVs[1] = new Point3D(-legSize*0.7, 0,  legSize);
		legVs[2] = new Point3D(-legSize*0.7, 0, -legSize);
		legVs[3] = new Point3D( legSize*0.7, 0, -legSize);
		
		QRotation r = QRotation.getZRotation(-e*Math.PI/6);
		for (int idx = 4; idx < 8; idx++)
			legVs[idx] = r.apply(legVs[idx-4]).getTranslated(0, legSize*2., 0);
		
		for (int idx = 8; idx < 12; idx++)
			legVs[idx] = r.apply(legVs[idx-8].getScaled(0.7)).getTranslated(-e*legSize*1.5, legSize*5, 0);

		r = QRotation.getYRotation(-e*Math.PI/2);
		for (int idx = 0; idx < 12; idx++)
			legVs[idx] = r.apply(legVs[idx]);

		
		r = QRotation.getZRotation(e*Math.PI/3);
		for (int idx = 0; idx < 12; idx++)
			legVs[idx] = r.apply(legVs[idx]).getTranslated(-e*bW*1.2, bH*0.7, bellySize*0.2);
		
		GTPolyhedron poly = new GTPolyhedron(legVs);
		poly.addFace(0, 4, 5, 1);
		poly.addFace(1, 5, 6, 2);
		poly.addFace(2, 6, 7, 3);
		poly.addFace(3, 7, 4, 0);
		poly.addFace(4, 8, 9, 5);
		poly.addFace(5, 9, 10, 6);
		poly.addFace(6, 10, 11, 7);
		poly.addFace(7, 11, 8, 4);
		poly.addFace(11, 10, 9, 8);
		return poly;
	}
	
	/*
	 * Legs P
	 */
	private GTPolyhedron getLegP(int e) {
		double legSize = headSize;
		Point3D[] legVs = new Point3D[12];
		
		legVs[0] = new Point3D( legSize, 0,  legSize);
		legVs[1] = new Point3D(-legSize, 0,  legSize);
		legVs[2] = new Point3D(-legSize, 0, -legSize);
		legVs[3] = new Point3D( legSize, 0, -legSize);
		
		QRotation r = QRotation.getZRotation(-e*Math.PI/8);
		for (int idx = 4; idx < 8; idx++)
			legVs[idx] = r.apply(legVs[idx-4]).getTranslated(0, legSize, 0);
		
		for (int idx = 8; idx < 12; idx++)
			legVs[idx] = r.apply(legVs[idx-8].getScaled(0.8)).getTranslated(-e*legSize, legSize*4, 0);

		r = QRotation.getYRotation(-e*Math.PI/2);
		for (int idx = 0; idx < 12; idx++)
			legVs[idx] = r.apply(legVs[idx]);

		
		r = QRotation.getZRotation(2*e*Math.PI/3);
		for (int idx = 0; idx < 12; idx++)
			legVs[idx] = r.apply(legVs[idx]).getTranslated(-e*bW*1.2, -0.76*bH, bellySize*0.2);
		
		GTPolyhedron poly = new GTPolyhedron(legVs);
		poly.addFace(0, 4, 5, 1);
		poly.addFace(1, 5, 6, 2);
		poly.addFace(2, 6, 7, 3);
		poly.addFace(3, 7, 4, 0);
		poly.addFace(4, 8, 9, 5);
		poly.addFace(5, 9, 10, 6);
		poly.addFace(6, 10, 11, 7);
		poly.addFace(7, 11, 8, 4);
		poly.addFace(11, 10, 9, 8);
		return poly;
	}
}