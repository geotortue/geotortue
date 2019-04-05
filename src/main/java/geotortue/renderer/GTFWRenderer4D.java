package geotortue.renderer;

import java.awt.Color;

import fw.geometry.util.Point3D;
import fw.geometry.util.Point4D;
import fw.geometry.util.QRotation4D;
import fw.renderer.core.RenderJob;
import fw.renderer.core.RendererSettingsI;
import fw.renderer.mesh.Mesh;
import geotortue.core.Turtle;
import geotortue.core.TurtleAvatar3D;
import geotortue.geometry.GTEuclidean4DGeometry;
import geotortue.geometry.GTGeometryI;
import geotortue.geometry.GTPoint;


public class GTFWRenderer4D extends GTFWRenderer3D {

	public GTFWRenderer4D(RendererSettingsI s, RenderJob<GTPoint> r, GTLightingContext gtlc) {
		super(s, r, gtlc);
	}
	
	@Override
	public void draw(Turtle t, GTGeometryI g) {
		if (!(g instanceof GTEuclidean4DGeometry))
			return;
		
		TurtleAvatar3D avatar = t.getAvatar3D();
		Point3D p = g.get3DCoordinates(t.getPosition());
		
		GTEuclidean4DGeometry g4 = (GTEuclidean4DGeometry) g;
		QRotation4D r = g4.getRotation4D();
		for (Mesh m : avatar.getMeshes(p, r.apply(t.getRotation4D()), 1/getUnit()))
			draw(m);
	}
	
	@Override
	public void drawAxis(GTGeometryI g, Turtle t) {
		if (!(g instanceof GTEuclidean4DGeometry))
			return;

		GTPoint p0 = t.getPosition();
		Point4D p4 = new Point4D(p0.getU1(), p0.getU2(), p0.getU3(), p0.getU4());
		QRotation4D r = t.getRotation4D();
		
		double u = 100 / getUnit();
		double uc = (t.getOrientation() > 0) ? u : -u;
		
		Point4D px4 = r.apply(new Point4D(uc, 0, 0, 0)).getTranslated(p4);
		Point4D py4 = r.apply(new Point4D(0, u, 0, 0)).getTranslated(p4);
		Point4D pz4 = r.apply(new Point4D(0, 0, u, 0)).getTranslated(p4);
		Point4D pt4 = r.apply(new Point4D(0, 0, 0, u)).getTranslated(p4);
		
		Point3D p = g.get3DCoordinates(t.getPosition());
		Point3D px = g.get3DCoordinates(new GTPoint(px4));
		Point3D py = g.get3DCoordinates(new GTPoint(py4));
		Point3D pz = g.get3DCoordinates(new GTPoint(pz4));
		Point3D pt = g.get3DCoordinates(new GTPoint(pt4));
		
		drawLine(p, px, Color.RED, 1);
		drawLine(p, py, Color.BLUE, 1);
		drawLine(p, pz, Color.GREEN, 1);
		drawLine(p, pt, Color.MAGENTA, 1);
	}
}
