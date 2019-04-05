package geotortue.geometry;

import fw.geometry.obj.GPoint;
import fw.geometry.obj.GPoint3D;
import fw.geometry.obj.GPoint4D;
import fw.geometry.util.MathUtils;
import fw.geometry.util.Point3D;
import fw.geometry.util.Point4D;
import fw.geometry.util.QRotation;

public class GTPoint extends GPoint4D {
	
	public GTPoint(double x, double y, double z, double t) {
		super(x, y, z, t);
	}
	
	public GTPoint(double x, double y, double z) {
		this(x, y, z, 0);
	}

	public boolean equals(GTPoint p) {
		new Exception("replace equals(GTPoint p) by ").printStackTrace();
		return MathUtils.abs2(getU1()-p.getU1(), getU2()-p.getU2(), getU3()-p.getU3())<1E-30;
	}
	
	public boolean quasiEquals(GTPoint p) {
		return MathUtils.abs2(getU1()-p.getU1(), getU2()-p.getU2(), getU3()-p.getU3(), getU4()-p.getU4())<1E-30;
	}
	
	public GTPoint(Point4D p) {
		this(p.x, p.y, p.z, p.t);
	}
	
	public GTPoint(GPoint3D p) {
		this(p.getU1(), p.getU2(), p.getU3());
	}
	
	public GTPoint(GPoint p) {
		this(p.getU1(), p.getU2(), 0);
	}

	public GTPoint getTranslated(double tx, double ty, double tz) {
		return new GTPoint(getU1() + tx, getU2() + ty, getU3() + tz);
	}
	
	public GTPoint getTranslated(Point3D p) {
		return new GTPoint(getU1() + p.x, getU2() + p.y, getU3() + p.z);
	}
	
	public GTPoint getTranslated(Point4D p) {
		return new GTPoint(getU1() + p.x, getU2() + p.y, getU3() + p.z, getU4() + p.t);
	}

	
	public GTPoint getTransformed(QRotation r) {
		Point3D p = r.apply(new Point3D(u1, u2, u3));
		return new GTPoint(p.x, p.y, p.z);
	}

	public void transform(QRotation r) {
		Point3D p = r.apply(new Point3D(u1, u2, u3));
		u1 = p.x;
		u2 = p.y;
		u3 = p.z;
	}
	
	public GTPoint getScaled(double k) {
		return new GTPoint(k*u1, k*u2, k*u3);
	}

	public static enum MASK {u1, u2, u3, u4};
	
	
	public Point4D toPoint4D() {
		return new Point4D(u1, u2, u3, u4);		
	}
	
	/**
	 * @return
	 */
	public Point3D toPoint3D(MASK m) {
		switch (m) {
		case u1:
			return new Point3D(u2, u3, u4);
		case u2:
			return new Point3D(u1, u3, u4);
		case u3:
			return new Point3D(u1, u2, u4);
		case u4:
			return new Point3D(u1, u2, u3);
		default:
			break;
		}
		return null;
	}
}