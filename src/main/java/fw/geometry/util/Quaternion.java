package fw.geometry.util;



public class Quaternion {
	
	final double s, x, y, z;
	
	public double getS() {
		return s;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getZ() {
		return z;
	}

	public Quaternion(double s, double x, double y, double z) {
		this.s = s;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Quaternion(Quaternion q) {
		this(q.s, q.x, q.y, q.z);
	}
	
	protected static Quaternion mul(Quaternion p, Quaternion q) {
		double s = p.s * q.s - p.x * q.x - p.y * q.y - p.z * q.z;
		Point3D m = pmul(p, q);
		return new Quaternion(s, m.x, m.y, m.z);
	}
	
	private static Point3D pmul(Quaternion p, Quaternion q) {
		double x = p.s * q.x + p.x * q.s + p.y * q.z - p.z * q.y;		
		double y = p.s * q.y - p.x * q.z + p.y * q.s + p.z * q.x;
		double z = p.s * q.z + p.x * q.y - p.y * q.x + p.z * q.s;
		return new Point3D(x, y, z);
	}
	
	public double abs(){
		return Math.sqrt(s*s + x*x + y*y + z*z);
	}

	public Quaternion inv() {
		double n = abs();
		return new Quaternion(s /n, -x / n, -y / n, -z / n);
	}
	
	public Point3D apply(Point3D p){
		if (s==1 && x==0 && y==0 && z==0)
			return p;

		Quaternion q = new Quaternion(0, p.x, p.y, p.z);
		return pmul(this, mul(q, this.inv()));
	}
	
	public String toString() {
		return "q = "+s+" + "+x+".i + "+y+".j + "+z+".k";
	}
}
