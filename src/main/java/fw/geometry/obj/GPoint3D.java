package fw.geometry.obj;

public class GPoint3D extends GPoint {

	protected double u3;

	public GPoint3D(double x, double y, double z) {
		super(x, y);
		this.u3 = z;
	}
	
	public double getU3() {
		return u3;
	}

	public String toString() {
		return "GPoint3D : ("+u1+",  "+u2+", "+u3+")";
	}
}