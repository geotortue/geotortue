package geotortue.geometry.obj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import fw.geometry.proj.PerspectiveI.InvisibleZPointException;
import fw.geometry.util.MathException.ZeroVectorException;
import fw.geometry.util.MathUtils;
import fw.geometry.util.Pixel;
import fw.geometry.util.Point3D;
import fw.geometry.util.QRotation;
import fw.renderer.core.RendererI;
import fw.renderer.mesh.FVMesh;
import fw.renderer.mesh.Polyhedron;
import fw.renderer.shader.GouraudShader;
import geotortue.core.TurtlePen;
import geotortue.geometry.GTGeometryI;
import geotortue.geometry.GTPoint;
import geotortue.renderer.GTRendererI;
import geotortue.renderer.GTRendererSettings;

public class GTSphericalPolygon extends GTPolygon {

	private final ArrayList<GTPoint> points = new ArrayList<GTPoint>();

	public GTSphericalPolygon(GTPoint p, TurtlePen pen) {
		super(pen);
		add(p);
	}

	@Override
	public void add(GTPoint position) {
		points.add(position);
	}

	private Point3D getMiddle(Point3D p, Point3D q) {
		Point3D n = MathUtils.crossProduct(p, q);
		double absN = n.abs();
		double angle = Math.atan2(absN, MathUtils.dotProduct(p, q));
		try {
			return new QRotation(n, angle / 2).apply(p);
		} catch (ZeroVectorException e) {
			e.printStackTrace(); // should not occur
			return p;
		}
	}
	
	private ArrayList<Point3D> getVertices_(GTGeometryI g) {
		ArrayList<Point3D> vs = new ArrayList<Point3D>();
		int nPoints = points.size();
		if (nPoints < 3)
			return vs;

		for (int idx = 0; idx < nPoints; idx++)
			vs.add(g.get3DCoordinates(points.get(idx)));
		
		if (MathUtils.getSqDistance(vs.get(nPoints-1), vs.get(0)) > 1E-10)
			vs.add(vs.get(0));
		
		return vs;
	}

	public FVMesh getMesh(GTGeometryI g, GTRendererI r) {
		Polyhedron poly = getTriangulizedPolyhedron(g, r);
		if (poly == null)
			return null;
		return new FVMesh(poly, getColor(), new GouraudShader());
	}

	final private HashMap<String, Integer> pointsTable = new HashMap<String, Integer>();

	private Polyhedron getTriangulizedPolyhedron(GTGeometryI g, RendererI<GTPoint> r) {
		pointsTable.clear();
		ArrayList<Point3D> vps = getVertices_(g);
		ArrayList<Triangle> triangles = new ArrayList<Triangle>();
		HashSet<int[]> scheme = new HashSet<int[]>();

		for (int idx = 1; idx < vps.size() - 2; idx++)
			triangles.add(new Triangle(0, idx, idx + 1));

		int idx = 0;
		while (idx < triangles.size()) {
			if (triangles.size() > GTRendererSettings.getMaxNumPoints()) {
				r.setErrorMessage(FILLING_ERROR.translate());
				break;
			}
			
			Triangle tri = triangles.get(idx);
			int idx0 = tri.idx0;
			int idx1 = tri.idx1;
			int idx2 = tri.idx2;
			Point3D p0 = vps.get(idx0);
			Point3D p1 = vps.get(idx1);
			Point3D p2 = vps.get(idx2);

			try {
				Pixel px0 = r.toScreen(p0);
				Pixel px1 = r.toScreen(p1);
				Pixel px2 = r.toScreen(p2);

				
				if (!r.intersects(px0, px1, px2)) {
					triangles.remove(idx);
				} else {
					int l01 = Math.abs(px1.i - px0.i) + Math.abs(px1.j - px0.j);
					int l12 = Math.abs(px2.i - px1.i) + Math.abs(px2.j - px1.j);
					int l02 = Math.abs(px0.i - px2.i) + Math.abs(px0.j - px2.j);
					boolean b = l01 + l12 + l02 <GTRendererSettings.getMaxDist()*3;
					if (b) {
						try {
							Point3D n = MathUtils.getNormal(p0, p1, p2);
							if (MathUtils.dotProduct(p0, n)>0)
								scheme.add(new int[] { idx0, idx1, idx2 });
							else
								scheme.add(new int[] { idx0, idx2, idx1 });
						} catch (ZeroVectorException e) {
						}
						
						idx++;
					} else {
						triangles.remove(idx);
						int idx01 = getMiddleIdx(idx0, idx1, p0, p1, vps);
						int idx02 = getMiddleIdx(idx0, idx2, p0, p2, vps);
						int idx12 = getMiddleIdx(idx1, idx2, p1, p2, vps);
						triangles.add(new Triangle(idx0, idx01, idx02));
						triangles.add(new Triangle(idx1, idx12, idx01));
						triangles.add(new Triangle(idx2, idx02, idx12));
						triangles.add(new Triangle(idx01, idx12, idx02));
				}
				}
			} catch (InvisibleZPointException ex) {
				triangles.remove(idx);
			}
		}

		if (scheme.isEmpty())
			return null;
		Point3D[] ps = vps.toArray(new Point3D[0]);
		return new Polyhedron(ps, scheme);
	}

	private int getMiddleIdx(int idx0, int idx1, Point3D p0, Point3D p1, ArrayList<Point3D> vps) {
		Integer idx = pointsTable.get(encode(idx0, idx1));
		if (idx != null)
			return idx;
		vps.add(getMiddle(p0, p1));
		pointsTable.put(encode(idx0, idx1), vps.size() - 1);
		return vps.size() - 1;
	}

	private class Triangle {
		private final int idx0, idx1, idx2;

		private Triangle(int idx0, int idx1, int idx2) {
			this.idx0 = idx0;
			this.idx1 = idx1;
			this.idx2 = idx2;
		}
	}

	private String encode(int idx0, int idx1) {
		if (idx0 < idx1)
			return idx0 + " " + idx1;
		else
			return idx1 + " " + idx0;
	}

	public static class FillingException extends Exception {
		private static final long serialVersionUID = 7439075127570702371L;

	}
}
