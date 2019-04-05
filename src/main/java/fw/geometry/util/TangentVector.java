package fw.geometry.util;


public class TangentVector {

	public double x, y, z, t;
	
	public TangentVector(double x, double y, double z, double t) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.t = t;
	}

	public TangentVector(double x, double y, double z) {
		this(x, y, z, 0);
	}
	
	public TangentVector(double x, double y) {
		this(x, y, 0);
	}
	
	public TangentVector(Point3D p) {
		this(p.x, p.y, p.z);
	}

	public TangentVector(Point4D p) {
		this(p.x, p.y, p.z, p.t);
	}

	@Override
	public String toString() {
		return "TangentVector : dx1="+x+" dx2="+y+" dx3="+z+" dx4="+t;
	}

	public Point3D getPoint3D() {
		return new Point3D(x, y, z);
	}

	public Point4D getPoint4D() {
		return new Point4D(x, y, z, t);
	}
	
}
