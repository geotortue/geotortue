package geotortue.geometry.obj;

import java.util.HashSet;
import java.util.Vector;

import fw.geometry.util.MathException.ZeroVectorException;
import fw.geometry.util.MathUtils;
import fw.geometry.util.Point3D;
import fw.renderer.core.RendererI;
import fw.renderer.mesh.FVMesh;
import fw.renderer.mesh.Polyhedron;
import fw.renderer.shader.FlatShader;
import geotortue.core.TurtlePen;
import geotortue.geometry.GTGeometryI;
import geotortue.geometry.GTPoint;
import geotortue.geometry.GTPoint.MASK;
import geotortue.renderer.GTRendererI;

public class GTVerticesPolygon extends GTPolygon {

	private final Vector<GTPoint> points = new Vector<GTPoint>();
	private Point3D firstVector, normalVector = null;
	
	public GTVerticesPolygon(GTPoint p, TurtlePen pen) {
		super(pen);
		try {
			add(p);
		} catch (NonFlatPolygonException e) {
		}
	}

	@Override
	public void add(GTPoint p) throws NonFlatPolygonException {
		if (points.isEmpty()) {
			points.add(p);
			return;
		}
		Point3D p_ = p.toPoint3D(MASK.u4);
		Point3D p0 = points.elementAt(0).toPoint3D(MASK.u4);
		Point3D v = MathUtils.getVector(p0, p_);
		if (normalVector!=null) { 
			if (Math.abs(MathUtils.dotProduct(normalVector, v))>1E-8) // raise exception if the polygon is not flat
				throw new NonFlatPolygonException();
			points.add(p);
			return;
		} 
		
		if (firstVector == null) {
			try {
				firstVector = MathUtils.getNormalized(v);
			} catch (ZeroVectorException e) {
			}
			points.add(p);
			return;
		}

		if (normalVector == null) {
			try {
				normalVector = MathUtils.normalizedCrossProduct(firstVector, v);
			} catch (ZeroVectorException e) {
			}
			points.add(p);
			return;
		}
	}

	public FVMesh getMesh(GTGeometryI g, GTRendererI r) {
		Point3D[] ps = getVertices(g, r);
		
		HashSet<int[]> scheme = new HashSet<int[]>();
		for (int idx = 2; idx < ps.length-1; idx++)
			scheme.add(new int[]{0, idx-1, idx});
		
		Polyhedron poly = new Polyhedron(ps, scheme);
		return new FVMesh(poly, getColor(), new FlatShader());
	}
	
	public Point3D[] getVertices(GTGeometryI gc, RendererI<GTPoint> r) {
		return getVertices_(gc, r).toArray(new Point3D[0]);
	}
	
	private Vector<Point3D> getVertices_(GTGeometryI gc, RendererI<GTPoint> r) {
		Vector<Point3D> vs = new Vector<Point3D>();
		int nPoints = points.size();
		if (nPoints < 3)
			return vs; 
		
		for (int idx = 0; idx < nPoints; idx++)
			vs.add(gc.get3DCoordinates(points.elementAt(idx)));
		
		if (MathUtils.getSqDistance(vs.lastElement(), vs.firstElement()) > 1E-20)
			vs.add(vs.firstElement());
		return vs;
	}
}