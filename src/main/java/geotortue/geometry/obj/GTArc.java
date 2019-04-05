package geotortue.geometry.obj;

import fw.geometry.obj.GObject;
import geotortue.core.TurtlePen;
import geotortue.geometry.GTEuclidean2DGeometry;
import geotortue.geometry.GTGeometryI;
import geotortue.geometry.GTPoint;
import geotortue.renderer.GTRendererI;

public class GTArc extends GObject implements GTObject {
	
	private final GTPoint center, startingPoint;
	private final double angle;

	public GTArc(GTPoint c, GTPoint p, double a, TurtlePen pen) {
		super(pen.getColor(), pen.getThickness());
		this.center = c;
		this.startingPoint = p;
		this.angle = a;
	}

	@Override
	public void draw(GTGeometryI g, GTRendererI r) {
		g.draw(this, r);
	}

	public GTPoint getCenter() {
		return center;
	}

	public GTPoint getStartingPoint() {
		return startingPoint;
	}

	public double getAngle() {
		return angle;
	}

	public GTPoint getEndPoint(GTEuclidean2DGeometry g) {
		return g.getArcEnd(startingPoint, center, angle);
	}
	


}