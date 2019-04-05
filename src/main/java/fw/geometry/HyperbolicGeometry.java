package fw.geometry;

import fw.app.FWConsole;
import fw.geometry.obj.GPoint;
import fw.geometry.util.MathException;
import fw.geometry.util.Point3D;

public abstract class HyperbolicGeometry<T extends GPoint> implements GeometryI<T> {

	public Point3D get3DCoordinates(T p) {
		return getHyperbolicCoordinates(p);
	}

	private Point3D getHyperbolicCoordinates(T p) {
		double x = p.getU1(); 
		double y = p.getU2();
		return new Point3D(x, y, Math.sqrt(1+x*x+y*y));
	}

	@Override
	public double distance(T gp, T gq) {
		Point3D p = getHyperbolicCoordinates(gp);
		Point3D q = getHyperbolicCoordinates(gq);
		return acosh(- p.x*q.x - p.y*q.y + p.z*q.z);
	}
	
	private double acosh(double x) {
		return Math.log( x + sqrt(x*x-1) );
	}
	
	public void check(double x, double y, double z) throws MathException {
		double error = Math.abs((x*x+y*y-z*z+1)/(z*z));
		if (Double.isNaN(error) || error>1E-7)
			throw new MathException("relative error = "+error+" > 1E-7");
	}
	
	private double sqrt(double x) {
		if (x==0)
			return 0;
		if (x<0) {
				if (x>-1E-8)
					return 0;
				FWConsole.printWarning(this, "HyperbolicGeometry.sqrt() : "+x+" < -1E-8");
				return 0;
		}
		return Math.sqrt(x);
	}
}
