package fw.renderer.mesh;

import java.util.Vector;

import fw.geometry.util.MathException.ZeroVectorException;
import fw.geometry.util.MathUtils;
import fw.geometry.util.Point3D;

public class Vertex extends Point3D {

	private Point3D normal;

	public Vertex(Point3D coords) {
		super(coords.x, coords.y, coords.z);
	}
	
	public Vertex(double x, double y, double z) {
		super(x, y, z);
	}

	public void computeMeanNormal(Vector<Face> faces) {
		if (faces.isEmpty())
			return;
		double x = 0;
		double y = 0;
		double z = 0;
		for (Face f : faces) {
			Point3D normal = f.getNormal();
			x += normal.x;
			y += normal.y;
			z += normal.z;
		}

		try {
			this.normal = MathUtils.getNormalized(new Point3D(x, y, z));
		} catch (ZeroVectorException ex) {
			this.normal = new Point3D(x, y, z);
		}
	}

	public Point3D getNormal() {
		return normal;
	}
	
	public void translate(Point3D p) {
		x += p.x;
		y += p.y;
		z += p.z;
	}
	
	public void scale(double k) {
		x *= k;
		y *= k;
		z *= k;
	}
	
	public void setCoordinates(Point3D p){
		x = p.x;
		y = p.y;
		z = p.z;
	}

	public void flipNormal() {
		if (normal != null)
			normal = normal.getScaled(-1);
		
	}
}