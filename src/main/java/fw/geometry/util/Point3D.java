package fw.geometry.util;

import fw.geometry.util.MathException.ZeroVectorException;

public class Point3D {

	public double x, y, z;
	
	public Point3D(double x, double y, double z){
		this.x=x;
		this.y=y;
		this.z=z;
	}

	public double abs() {
		return MathUtils.abs(x, y, z);
	}

	public double abs2() {
		return MathUtils.abs2(x, y, z);
	}
	
	public Point3D getTranslated(double tx, double ty, double tz) {
		return new Point3D(x+tx, y+ty, z+tz);
	}

	public Point3D getTranslated(Point3D p) {
		return getTranslated(p.x, p.y, p.z);
	}
	
	public Point3D getNormalized() throws ZeroVectorException {
		return MathUtils.getNormalized(this);
	}

	public Point3D getScaled(double k) {
		return new Point3D(k*x, k*y, k*z);
	}
	
	public String toString(){
		return "Point3D : x = "+x+"; y = "+y+"; z= "+z;
	}

	public Point3D opp() {
		return new Point3D(-x, -y, -z);
	}
}