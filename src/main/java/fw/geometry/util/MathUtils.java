package fw.geometry.util;

import java.util.Locale;

import fw.app.Translator;
import fw.geometry.proj.ZPoint;
import fw.geometry.util.MathException.ZeroVectorException;



public class MathUtils {

	public static double dotProduct(Point3D p, Point3D q) {
		return p.x * q.x + p.y * q.y + p.z * q.z; 
	}
	
	public static double dotProduct(Point4D p, Point4D q) {
		return p.x * q.x + p.y * q.y + p.z * q.z + p.t * q.t; 
	}
	
	public static Point3D getNormal(Point3D p0, Point3D p1, Point3D p2) throws ZeroVectorException {
		Point3D u = new Point3D(p1.x - p0.x, p1.y - p0.y, p1.z - p0.z);
		Point3D v = new Point3D(p2.x - p0.x, p2.y - p0.y, p2.z - p0.z);
		return getNormalized(crossProduct(u, v));
	}
	
	public static double getNormalZComponent(ZPoint p0, ZPoint p1, ZPoint p2) {
		double ux = p1.x - p0.x;
		double uy = p1.y - p0.y;
		double vx = p2.x - p0.x;
		double vy = p2.y - p0.y;
		return  ux * vy - uy * vx;
	}
	
	public static Point3D getVector(Point3D p, Point3D q) {
		return new Point3D(q.x-p.x, q.y-p.y, q.z-p.z);
	}
	
	public static boolean isZero(Point3D p){
		return (p.x==0)&&(p.y==0)&&(p.z==0);
	}
	
	public static Point3D getNormalized(Point3D p) throws ZeroVectorException {
		double n = p.abs();
		if (n==0)
			throw new MathException.ZeroVectorException();
		if (n==1)
			return p;
		return new Point3D(p.x/n, p.y/n, p.z/n);
	}
	
	public static Point4D getNormalized(Point4D p) throws ZeroVectorException {
		double n = p.abs();
		if (n==0)
			throw new MathException.ZeroVectorException();
		if (n==1)
			return p;
		return new Point4D(p.x/n, p.y/n, p.z/n, p.t/n);
	}

	public static Point3D crossProduct(Point3D p, Point3D q) {
		return new Point3D(
				p.y * q.z - p.z * q.y,
				p.z * q.x - p.x * q.z, 
				p.x * q.y - p.y * q.x); 
	}
	
	public static Point3D normalizedCrossProduct(Point3D p, Point3D q) throws ZeroVectorException {
		return getNormalized(crossProduct(p, q));
	}

	public static double abs(double x, double y) {
		return Math.sqrt(abs2(x, y));
	}
	
	public static double abs2(double x, double y) {
		return x*x + y*y;
	}
	
	public static double abs(double x, double y, double z) {
		return Math.sqrt(abs2(x, y, z));
	}
	
	public static double abs2(double x, double y, double z) {
		return x*x + y*y + z*z;
	}
	
	public static double abs2(double x, double y, double z, double t) {
		return x*x + y*y + z*z + t*t;
	}
	
	public static double abs(double x, double y, double z, double t) {
		return Math.sqrt(abs2(x, y, z, t));
	}
	
    public static int round(double x) throws MathException {
    	if (Double.isNaN(x))
    		throw new MathException("MathUtils.round(double x) : "+x+" is not a number");
    	if (Double.isInfinite(x))
    		throw new MathException("MathUtils.round(double x) : "+x+" is infinite");
    	if (x>Integer.MAX_VALUE)
    		throw new MathException("MathUtils.round(double x) : "+x+" is greater than Integer.MaxValue");
    	if (x<Integer.MIN_VALUE)
    		throw new MathException("MathUtils.round(double x) : "+x+" is less than Integer.MinValue");
    	int n=(int) Math.floor(x);
    	if (Math.abs(x-n)>1)
    		new MathException("MathUtils.round(double x) : "+" floor("+x+") != "+n).printStackTrace();
    	
    	if (x-n>=0.5) 
    		return n+1;
    	else
    		return n;
    }

	public static double getSqDistance(Point3D p, Point3D q) {
		double dx = p.x-q.x;
		double dy = p.y-q.y;
		double dz = p.z-q.z;
		return dx*dx + dy*dy + dz*dz;
	}
	
	public boolean areCoplanar(Point3D p, Point3D q, Point3D r, Point3D s){
		Point3D pq = crossProduct(p, q);
		Point3D rs = crossProduct(r, s);
		Point3D pqrs = crossProduct(pq, rs);
		return pqrs.abs2()==0;
	}
	
	public static String format(double v, int p) { 
		Locale loc = Translator.getLocale();
//		if (Double.isInfinite(v))
//			return (v>0)? "+∞" : "-∞";
		
		int iv = (int) v;
		double prec = Math.pow(10, -p);

		if (Math.abs(v - iv) * 2 <= prec)
			return String.format(loc, "%d", iv);

		String pattern = "%." + p;
		pattern += (Math.abs(v) > 1E10) ? "E" : "f";
		return String.format(loc, pattern, v);
	}

}