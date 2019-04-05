package fw.renderer.mesh;

import fw.geometry.util.MathException.ZeroVectorException;
import fw.geometry.util.MathUtils;
import fw.geometry.util.Point3D;

public class Face {

	public Vertex p0, p1, p2;
	private Point3D normal = null;

	public Face(Vertex p0, Vertex p1, Vertex p2) throws FlatFaceException {
		this.p0 = p0;
		this.p1 = p1;
		this.p2 = p2;
		try {
			normal = MathUtils.getNormal(p0, p1, p2);
		} catch (ZeroVectorException e) {
			throw new FlatFaceException();
		}
	}

	public Point3D getNormal() {
		return normal;
	}
	
	public void reverseOrientation() {
		normal = normal.getScaled(-1);
		Vertex p1c = p1;
		p1 = p2;
		p2 = p1c;
	}
	

	public class FlatFaceException extends Exception {
		private static final long serialVersionUID = 7191011728017756358L;

		public FlatFaceException() {
			super();
		}

	}
}