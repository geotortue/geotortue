package fw.geometry.proj;


import fw.geometry.util.Point3D;

public class PerspectiveMatrix {

	private double a00, a01, a02, a10, a11, a12, a20, a21, a22;

	private double b00, b01, b02, b10, b11, b12, b20, b21, b22;
	
	private double det;
	
	public PerspectiveMatrix(
			double a00, double a01, double a02, 
			double a10, double a11, double a12, 
			double a20, double a21, double a22) {
		this.det =  det(a00, a01, a02, a10, a11, a12, a20, a21, a22);
		if (det!=0) {
			this.a00 = a00;
			this.a01 = a01;
			this.a02 = a02;
			this.a10 = a10;
			this.a11 = a11;
			this.a12 = a12;
			this.a20 = a20;
			this.a21 = a21;
			this.a22 = a22;
			computeInverse();
		} else { 
			new MathException("Illegal perspective matrix").printStackTrace();
			System.err.println(a00+" "+a01+" "+a02);
			System.err.println(a10+" "+a11+" "+a12);
			System.err.println(a20+" "+a21+" "+a22);
		}
	}
	
	private double det(
			double a00, double a01, double a02, 
			double a10, double a11, double a12, 
			double a20, double a21, double a22) {
		return a00*a11*a22 + a10*a21*a02 + a20*a01*a12 
			- (a20*a11*a02 + a10*a01*a22 + a00*a21*a12);
	}
	
	private void computeInverse() {
		this.b00 = (a11*a22 - a21*a12) / det;
		this.b01 = (a02*a21 - a01*a22) / det;
		this.b02 = (a01*a12 - a02*a11) / det;
		this.b10 = (a12*a20 - a10*a22) / det;
		this.b11 = (a00*a22 - a02*a20) / det;
		this.b12 = (a02*a10 - a00*a12) / det;
		this.b20 = (a10*a21 - a11*a20) / det;
		this.b21 = (a01*a20 - a00*a21) / det;
		this.b22 = (a00*a11 - a01*a10) / det;
	}

	public PerspectiveMatrix() {
		this(	1, 0, 0, 
				0, 1, 0,
				0, 0, 1);
	}

	public Point3D mul(Point3D p) {
		return new Point3D(
				a00*p.x + a01*p.y + a02*p.z,
				a10*p.x + a11*p.y + a12*p.z,
				a20*p.x + a21*p.y + a22*p.z); 
	}
	
	PerspectiveMatrix getInverse() {
		return new PerspectiveMatrix(b00, b01, b02, b10, b11, b12, b20, b21, b22);
	}

	private static class MathException extends Exception {
		private static final long serialVersionUID = -3991668790817848188L;

		public MathException(String message) {
			super(message);
		}
	}
}