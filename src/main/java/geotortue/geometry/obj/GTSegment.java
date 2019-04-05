package geotortue.geometry.obj;

import fw.geometry.obj.GSegment;
import geotortue.core.TurtlePen;
import geotortue.geometry.GTGeometryI;
import geotortue.geometry.GTPoint;
import geotortue.renderer.GTRendererI;

public class GTSegment extends GSegment<GTPoint> implements GTObject {


	public GTSegment(GTPoint p0, GTPoint p1,TurtlePen pen) {
		super(p0, p1, pen.getColor(), pen.getThickness());
	}

	@Override
	public void draw(GTGeometryI g, GTRendererI r) {
		g.draw(this, r);
	}

}
