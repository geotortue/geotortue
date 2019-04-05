package fw.renderer.mesh;

import fw.geometry.util.Point3D;

public class Sphere3D extends UVSurface {

	private final double radius;
	private final Point3D center;
	
	public Sphere3D(Point3D c, double radius, int nDiv) {
		this.center = c;
		this.radius = radius;
		buildUVSurface(-Math.PI/2, Math.PI/2, nDiv, 0., 2*Math.PI, 2*nDiv);
	}
	
	public Sphere3D(double radius, int nDiv) {
		this(new Point3D(0, 0, 0), radius, nDiv);
	}

	@Override
	protected Point3D getPoint(double u, double v) {
		double cosPhi  = Math.cos(u);
		double sinPhi  = Math.sin(u);
		double cosTheta = Math.cos(v);
		double sinTheta = Math.sin(v);
		return new Point3D(radius * cosPhi*sinTheta, radius * sinPhi, radius * cosPhi*cosTheta).getTranslated(center);
	}

	@Override
	protected int[][] getFaces(int i, int j, int n, int m) {
		int idx00 = j*(n+1)+i;
		int idx01 = idx00+1;
		int idx10 = idx00+n+1;
		int idx11 = idx10+1;
		
		if (j==m-1) {
			idx10 = i;
			idx11 = i+1;
		}
		
		if (i==0) 
			idx00 = 0;
		
		if (i==n-1)
			idx11 = (m+1)*(n+1)-1;

		int[] f = new int[]{idx00, idx10, idx11, idx01};
		
		if (i==0)
			f = new int[]{idx00, idx11, idx01};

		if (i==n-1)
			f = new int[]{idx00, idx10, idx11};
		
		return new int[][]{f};
	}
}