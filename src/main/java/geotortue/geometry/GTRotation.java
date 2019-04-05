package geotortue.geometry;

import fw.geometry.util.Point3D;
import fw.geometry.util.Point4D;
import fw.geometry.util.QRotation;
import fw.geometry.util.QRotation4D;

public class GTRotation extends QRotation4D {
	
	public GTRotation() {
		super();
	}
	
	public GTRotation(QRotation r) {
		super(r, r.inv());
	}

	public GTRotation(QRotation r1, QRotation r2) {
		super(r1, r2);
	}

	private GTRotation(QRotation4D r) {
		this.q1 = r.getQ1();
		this.q2 = r.getQ2();
	}

	public GTRotation apply(QRotation4D r) {
		return new GTRotation(super.apply(r));
	}

	public Point3D apply(Point3D p) {
		Point4D p4 = super.apply(new Point4D(p.x, p.y, p.z, 0));
		return new Point3D(p4.x, p4.y, p4.z);
	}

	public GTRotation inv() {
		return new GTRotation(q1.inv(), q2.inv());
	}


	public QRotation getRotation3D() {
		return q1;
	}

	public static GTRotation getZRotation(double angle) {
		return new GTRotation(QRotation.getZRotation(angle));
	}
}