package geotortue.geometry.obj;

import geotortue.core.TurtlePen;
import geotortue.geometry.GTGeometryI;
import geotortue.geometry.GTPoint;
import geotortue.renderer.GTRendererI;

public class GTFilledCircle extends GTCircle {

	public GTFilledCircle(GTPoint c, double r, TurtlePen pen) {
		super(c, r, pen);
	}

	@Override
	public void draw(GTGeometryI g, GTRendererI r) {
		g.fill(this, r);
	}
}
