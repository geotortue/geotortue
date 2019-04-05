package fw.geometry.obj;

public class GPoint4D extends GPoint3D {
	
	protected double u4;

	public GPoint4D(double x, double y, double z, double t) {
		super(x, y, z);
		this.u4 = t;
	}

	public double getU4() {
		return u4;
	}

	public String toString() {
		return "GPoint3D : ("+u1+",  "+u2+", "+u3+", "+u4+")";
	}
}
