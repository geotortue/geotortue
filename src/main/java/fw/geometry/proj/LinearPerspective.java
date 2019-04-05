package fw.geometry.proj;

import fw.geometry.util.Point3D;

public class LinearPerspective extends Perspective {
	private PerspectiveMatrix matrix = new PerspectiveMatrix();
	
	protected PerspectiveMatrix getMatrix() {
		return matrix;
	}
	
	protected void setMatrix(PerspectiveMatrix m) {
		matrix = m;
	}
	
	@Override
	public final ZPoint toZSpace(Point3D p) throws InvisibleZPointException {
		Point3D q = matrix.mul(p);
		double x = q.x;
		double y = q.y;
		double z = q.z;
		if (z > getMaximumZDepth())
			throw new InvisibleZPointException(this, z);
		return new ZPoint(x, y, z);
	}

	@Override
	public Point3D liftTo3DSpace(ZPoint p) {
		return matrix.getInverse().mul(new Point3D(p.x, p.y, p.z));
	}
	
	
}
