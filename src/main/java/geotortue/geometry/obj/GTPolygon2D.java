/**
 * 
 */
package geotortue.geometry.obj;

import geotortue.core.TurtlePen;
import geotortue.geometry.GTEuclidean2DGeometry;
import geotortue.geometry.GTPoint;

public class GTPolygon2D extends GTPolygon {
	
	private final GTPath path;

	public GTPolygon2D(GTEuclidean2DGeometry g, GTPoint p, TurtlePen pen) {
		super(pen);
		this.path = new GTPath(g, p, pen.getThickness());
	}

	@Override
	public void add(GTPoint position) {
		path.addSegment(position);
	}

	@Override
	public void add(GTArc arc) {
		path.addArc(arc);
	}
	
	public GTPath getShape() {
		return path;
	}
}
