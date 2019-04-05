package fw.renderer.mesh;

import java.awt.Color;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

import fw.geometry.obj.GPoint;
import fw.renderer.mesh.Face.FlatFaceException;
import fw.renderer.shader.ShaderI;



public class FVMesh extends Mesh {

	private Vertex[] vertices;
	private Face[] faces;

	private Hashtable<Vertex, Vector<Face>> adjacencyTable;
	private HashSet<int[]> faceScheme;
	
	public <T extends GPoint> FVMesh(Polyhedron poly, Color c, ShaderI s) {
		super(c);
		this.adjacencyTable = new Hashtable<Vertex, Vector<Face>>();
		this.vertices = poly.getVertices();
		this.faceScheme = poly.getScheme();
		
		for (int idx = 0; idx < vertices.length; idx++)
			adjacencyTable.put(vertices[idx], new Vector<Face>());

		Vector<Face> facesV = new Vector<Face>();
		for (int[] vIndices : poly.getScheme())
			addFace(vIndices, facesV);
		this.faces = facesV.toArray(new Face[facesV.size()]);

		setShader(s);
	}
	
	public void reverseOrientation() {
		for (int idx = 0; idx < faces.length; idx++)
			faces[idx].reverseOrientation();
		for (int idx = 0; idx < vertices.length; idx++)
			vertices[idx].flipNormal();
		updateShader();
	}
	
	private void addFace(int[] vIndices, Vector<Face> vect) {
		for (int idx = 1; idx < vIndices.length-1; idx++) {
			int idx0 = vIndices[0];
			int idx1 = vIndices[idx];
			int idx2 = vIndices[idx+1];
			Vertex v0 = vertices[idx0];
			Vertex v1 = vertices[idx1];
			Vertex v2 = vertices[idx2];
			try {
				Face f = new Face(v0, v1, v2);
				adjacencyTable.get(v0).add(f);
				adjacencyTable.get(v1).add(f);
				adjacencyTable.get(v2).add(f);
				vect.add(f);
			} catch (FlatFaceException e) {
			}
		}
	}
	
	@Override
	public Vertex[] getVertices() {
		return vertices;
	}
	
	@Override
	public Face[] getFaces() {
		return faces;
	}
	
	@Override
	public Vector<Face> getFacesContaining(Vertex v) {
		return adjacencyTable.get(v);
	}

	@Override
	public HashSet<int[]> getFaceScheme() {
		return faceScheme;
	}
	
	
}