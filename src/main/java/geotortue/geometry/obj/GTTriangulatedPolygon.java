package geotortue.geometry.obj;

import java.util.Vector;

import fw.geometry.proj.PerspectiveI.InvisibleZPointException;
import fw.geometry.util.MathUtils;
import fw.geometry.util.Pixel;
import fw.geometry.util.Point3D;
import fw.renderer.core.RendererI;
import fw.renderer.mesh.FVMesh;
import fw.renderer.mesh.Polyhedron;
import fw.renderer.shader.FlatShader;
import geotortue.core.TurtlePen;
import geotortue.geometry.GTGeometryI;
import geotortue.geometry.GTPoint;
import geotortue.renderer.GTRendererI;
import geotortue.renderer.GTRendererSettings;

public abstract class GTTriangulatedPolygon extends GTPolygon {

	private final Vector<GTPoint> points = new Vector<GTPoint>();
	
	public GTTriangulatedPolygon(GTPoint p, TurtlePen pen) {
		super(pen);
		add(p);
	}

	@Override
	public void add(GTPoint position) {
		points.add(position);
	}

	private Vector<Point3D> getVertices_(GTGeometryI gc, RendererI<GTPoint> r) {
		Vector<Point3D> vs = new Vector<Point3D>();
		int nPoints = points.size();
		if (nPoints < 3)
			return vs; 
		
		for (int idx = 0; idx < nPoints; idx++)
			vs.add(gc.get3DCoordinates(points.elementAt(idx)));
		
		if (MathUtils.getSqDistance(vs.lastElement(), vs.firstElement()) > 1E-10)
			vs.add(vs.firstElement());
		return vs;
	}
	
	private Point3D[] getTriangulizedPath(GTGeometryI g, GTRendererI r) {
		Vector<Point3D> vs = getVertices_(g, r); 
		int idx = 0;
		while (idx < vs.size()-1) {
			if (vs.size() > GTRendererSettings.getMaxNumPoints()) {
				r.setErrorMessage(FILLING_ERROR.translate());
				break;
			}
				
			Point3D p0 = vs.elementAt(idx);
			Point3D p1 = vs.elementAt(idx+1);
			try {
				Pixel px0 = r.toScreen(p0);
				Pixel px1 = r.toScreen(p1);
				
				if (Math.abs(px1.i-px0.i) + Math.abs(px1.j-px0.j) > GTRendererSettings.getMaxDist())
					vs.insertElementAt(getMiddle(p0, p1), idx+1);
				else
					idx++;
			} catch (InvisibleZPointException ex) {
				idx++;
			}
		}
		return vs.toArray(new Point3D[0]);
	}
	
	public FVMesh getMesh(GTGeometryI g, GTRendererI r) {
		Point3D[] ps = getTriangulizedPath(g, r);
		Polyhedron poly = new Polyhedron(ps);
		return new FVMesh(poly, getColor(), new FlatShader());
	}
	
	protected abstract Point3D getMiddle(Point3D p, Point3D q);
	
	public static class FillingException extends Exception {
		private static final long serialVersionUID = 7439075127570702371L;

	}
}