package fw.geometry.util;

import fw.geometry.util.MathException.ZeroVectorException;

public class Point4D {

	public double x, y, z, t;
	
	public Point4D(double x, double y, double z, double t){
		this.x=x;
		this.y=y;
		this.z=z;
		this.t=t;
	}
	
	public double abs() {
		return MathUtils.abs(x, y, z, t);
	}

	public Point4D getScaled(double k) {
		return new Point4D(k*x, k*y, k*z, k*t);
	}
	
	public Point4D getTranslated(double dx, double dy, double dz, double dt) {
		return new Point4D(x+dx, y+dy, z+dz, t+dt);
	}

	public Point4D getTranslated(Point4D p) {
		return getTranslated(p.x, p.y, p.z, p.t);
	}
	
	public String toString(){
		return "Point4D : x = "+x+"; y = "+y+"; z= "+z+"; t= "+t;
	}
	
	public Point4D getNormalized() throws ZeroVectorException {
		return MathUtils.getNormalized(this);
	}

}