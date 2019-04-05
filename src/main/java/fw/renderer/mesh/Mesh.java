package fw.renderer.mesh;


import java.awt.Color;
import java.util.HashSet;
import java.util.Vector;

import fw.renderer.shader.ShaderI;



public abstract class Mesh {

	private Color color;
	private ShaderI shader;
	
	public abstract Vertex[] getVertices();
	public abstract Face[] getFaces();
	public abstract Vector<Face> getFacesContaining(Vertex v);
	public abstract HashSet<int[]> getFaceScheme();

	public Mesh(Color c) {
		this.color = c;
	}
	
	public void updateShader() {
		shader.prepare(this);
	}
	
	public void setShader(ShaderI s) {
		this.shader = s;
		updateShader();
	}

	public ShaderI getShader() {
		return shader;
	}
	
	private boolean flag = false;
	
	public void computeVerticesMeanNormal() {
		if (flag)
			return;
		for (Vertex v : getVertices())
			v.computeMeanNormal(getFacesContaining(v));
		flag = true;
	}
	
	public Color getColor() {
		return color;
	}
}
