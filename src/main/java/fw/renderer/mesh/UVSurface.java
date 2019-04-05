package fw.renderer.mesh;

import java.awt.Color;
import java.util.HashSet;

import fw.geometry.util.Point3D;
import fw.renderer.shader.ShaderI;



public abstract class UVSurface {
	
	protected Polyhedron polyhedron;
	
	public Polyhedron getPolyhedron() {
		return polyhedron;
	}

	protected void buildUVSurface(double umin, double umax, int udiv, double vmin, double vmax, int vdiv){
		int n = udiv;
		int m = vdiv;
		double u = umin;
		double du = (umax-umin)/udiv;
		double v = vmin;
		double dv = (vmax-vmin)/vdiv;
		
		Point3D[] points = new Point3D[(n+1)*(m+1)];
		for (int j = 0; j <= m; j++) {
			u = umin;
			for (int i = 0; i <= n; i++) {
				points[j*(n+1)+i] = getPoint(u, v);
				u += du;
			}
			v += dv;
		}
		
		HashSet<int[]> scheme = new HashSet<int[]>(2*n*m);
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++)
				for (int[] fs : getFaces(i, j, n, m))
					scheme.add(fs);
		}
		
		this.polyhedron = new Polyhedron(points, scheme);
	}
	
	protected abstract Point3D getPoint(double u, double v);
	
	protected int[][] getFaces(int i, int j, int n, int m) {
		int idx00 = j*(n+1)+i;
		int idx01 = idx00+1;
		int idx10 = idx00+n+1;
		int idx11 = idx10+1;
		int[] f1 = new int[]{idx00, idx01, idx11};
		int[] f2 = new int[]{idx00, idx11, idx10};
		return new int[][]{f1, f2};
	}
	
	public FVMesh getMesh(Color c, ShaderI s){
		return new FVMesh(polyhedron, c, s);
	}
}