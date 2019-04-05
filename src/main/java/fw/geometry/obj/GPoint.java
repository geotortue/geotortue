package fw.geometry.obj;


public class GPoint {
	
	protected double u1, u2;
	
	public GPoint(double x, double y){
		this.u1=x;
		this.u2=y;
	}
	
	public double getU1() {
		return u1;
	}

	public double getU2() {
		return u2;
	}
	
	public GPoint getTranslated(double tx, double ty) {
		return new GPoint(u1+tx, u2+ty);
	}
	
	public void translate(GPoint t) {
		u1 += t.u1;
		u2 += t.u2;
	}

	@Override
	public String toString() {
		return u1+" "+u2;
	}
}
