package fw.geometry.util;

import fw.geometry.util.MathException.ZeroVectorException;


public class QRotation extends Quaternion {

	private QRotation(double s, double x, double y, double z) {
		super(s, x, y, z);
	}
	
	public QRotation(Quaternion q) throws ZeroVectorException {
		super(getNormalized(q));
	}

	
	public QRotation() {
		super(1, 0, 0, 0);
	}
	
	private static Quaternion getNormalized(Quaternion q) throws ZeroVectorException {
		double n = q.abs();
		if (n==1)
			return q;
		if (n==0)
			throw new MathException.ZeroVectorException();
		else
			return new QRotation(q.s/n, q.x/n, q.y/n, q.z/n);
	}
	
	/*
	 * 
	 */
	public QRotation(Point3D axe, double angle) throws ZeroVectorException {
		this(getQuaternion(axe, angle));
	}

	private static Quaternion getQuaternion(Point3D axe, double angle) throws ZeroVectorException {
		Point3D axe0 = axe.getNormalized();
		double x0 = axe0.x;
		double y0 = axe0.y;
		double z0 = axe0.z;
		double cos = Math.cos(angle/2);
		double sin = Math.sin(angle/2);
		return new Quaternion(cos, x0*sin, y0*sin, z0*sin);
	}
	
	/*
	 * The quaternion which sends the base (i, j, k) to (u, v, w = u cross v) 
	 */
	public QRotation(Point3D u, Point3D v) throws MathException {
		this(getQuaternion(u.getNormalized(), v.getNormalized()));
	}
	
	private static Quaternion getQuaternion(Point3D u, Point3D v) throws MathException {
		double dotP = MathUtils.dotProduct(u, v);
		if (Math.abs(dotP)>1E-15)
			throw new MathException("Non orthogonal vectors (u, v)=" + dotP);
		
		Point3D w = MathUtils.crossProduct(u, v);
		double s = 1 + u.x + v.y + w.z;
		double x, y, z;
		if (Math.abs(s)>1E-15) {
			x = v.z - w.y;
			y = w.x - u.z;
			z = u.y - v.x;
		} else {
			x = 1 + u.x;
			if (x != 0) {
				y = u.y;
				z = u.z;
			} else {
				y = 1 + v.y;
				if (y != 0) 
					z = v.z;
				 else 
					z = 1;
			}
		}
		
		return new Quaternion(s, x, y, z);
	}

	public static QRotation getXRotation(double angle) {
		return new QRotation(Math.cos(angle/2), Math.sin(angle/2), 0, 0);
	}
	 
	public static QRotation getYRotation(double angle) {
		return new QRotation(Math.cos(angle/2), 0, Math.sin(angle/2), 0);
	}

	public static QRotation getZRotation(double angle) {
		return new QRotation(Math.cos(angle/2), 0, 0, Math.sin(angle/2));
	}
	
	public static QRotation getI() {
		return new QRotation(0, 1, 0, 0);
	}
	
	public static QRotation getJ() {
		return new QRotation(0, 0, 1, 0);
	}
	
	public static QRotation getK() {
		return new QRotation(0, 0, 0, 1);
	}
	
	public QRotation apply(QRotation r) {
		if (s==1 && x==0 && y==0 && z==0)
			return r;
		Quaternion q = mul(this, r);
		return new QRotation(q.s, q.x, q.y, q.z);
	}
	
	public QRotation inv() {
		return new QRotation(s, -x, -y, -z);
	}

	public double getXYAngle() {
		double j = s*s + y*y - x*x - z*z;
		double i = 2*(x*y - s*z);
		return Math.atan2(j, i);
	}
	
	public Point3D getAxe() {
		try {
			return new Point3D(x, y, z).getNormalized();
		} catch (MathException ex) {
			ex.printStackTrace();
		}
		return new Point3D(1, 0, 0);
	}

	public double get2DAngle() {
		//double sin = new Point3D(x, y, z).abs();
		return 2*Math.atan2(z, s);
	}

	protected static QRotation mul(QRotation p, QRotation q) {
		Quaternion m = Quaternion.mul(p, q);
		return new QRotation(m.s, m.x, m.y, m.z);
	}
	
	
}