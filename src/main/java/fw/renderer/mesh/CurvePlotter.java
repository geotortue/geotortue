package fw.renderer.mesh;

import fw.geometry.obj.GPoint;
import fw.geometry.util.Point3D;

public abstract class CurvePlotter<T extends GPoint> {

	private final double tmin, tmax;
	private final int tdiv;
	
	public int getCount() {
		return tdiv;
	}

	public CurvePlotter(double tmin, double tmax, int tdiv) {
		this.tmin = tmin;
		this.tmax = tmax;
		this.tdiv = Math.max(1, tdiv);;
	}
	
	public double getStep() {
		return Math.abs((tmax-tmin)/tdiv);
	}
	
	public double getT0() {
		return tmin;
	}
	
	public abstract Point3D getPoint(double t);
	
}
