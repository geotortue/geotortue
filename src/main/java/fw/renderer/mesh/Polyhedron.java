package fw.renderer.mesh;

import java.util.HashSet;

import fw.geometry.util.Point3D;

public class Polyhedron {

	private HashSet<int[]> scheme;
	private Vertex[] vertices;
	
	
	public Polyhedron(Point3D[] ps, HashSet<int[]> scheme){
		this.scheme = scheme;
		int verticesCount = ps.length;
		this.vertices = new Vertex[verticesCount];
		for (int idx = 0; idx < verticesCount; idx++)
			vertices[idx] = new Vertex(ps[idx]);
		for (int[] faceScheme : scheme)
			addFace(faceScheme);
	}
	
	public Polyhedron(Point3D... vertices) {
		this(vertices, new HashSet<int[]>());
	}
	
	public void addFace(int... faceScheme){
		scheme.add(faceScheme);
	}
	
	public Vertex[] getVertices() {
		return vertices;
	}
	
	public HashSet<int[]> getScheme() {
		return scheme;
	}
}