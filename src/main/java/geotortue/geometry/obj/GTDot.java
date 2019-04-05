package geotortue.geometry.obj;

import fw.geometry.obj.GDot;
import geotortue.core.TurtlePen;
import geotortue.geometry.GTGeometryI;
import geotortue.geometry.GTPoint;
import geotortue.renderer.GTRendererI;

public class GTDot extends GDot<GTPoint> implements GTObject {

	public GTDot(GTPoint p, TurtlePen pen) {
		super(p, pen.getColor(), pen.getThickness());
	}

	@Override
	public void draw(GTGeometryI g, GTRendererI r) {
		g.draw(this, r);
	}
}