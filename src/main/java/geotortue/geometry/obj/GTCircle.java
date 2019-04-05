package geotortue.geometry.obj;

import fw.geometry.obj.GObject;
import geotortue.core.TurtlePen;
import geotortue.geometry.GTGeometryI;
import geotortue.geometry.GTPoint;
import geotortue.renderer.GTRendererI;

public class GTCircle extends GObject implements GTObject {

	private final GTPoint center;
	private final double radius;

	public GTCircle(GTPoint c, double r, TurtlePen pen) {
		super(pen.getColor(), pen.getThickness());
		this.center = c;
		this.radius = r;
	}

	@Override
	public void draw(GTGeometryI g, GTRendererI r) {
		g.draw(this, r);
	}


	public GTPoint getCenter() {
		return center;
	}

	public double getRadius() {
		return radius;
	}

}