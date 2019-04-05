package geotortue.geometry.obj;

import fw.geometry.util.MathUtils;
import fw.geometry.util.Point3D;
import geotortue.core.TurtlePen;
import geotortue.geometry.GTPoint;

public class GTHPGeoPolygon extends GTTriangulatedPolygon {

	public GTHPGeoPolygon(GTPoint p, TurtlePen pen) {
		super(p, pen);
	}

	@Override
	public Point3D getMiddle(Point3D p, Point3D q) {
		if (q.x - p.x == 0)
			return new Point3D((p.x + q.x) / 2, (p.y + q.y) / 2, 0);
		final double omega = (p.x + q.x + (p.y - q.y) / (p.x - q.x) * (p.y + q.y)) / 2;
		final double R = MathUtils.abs(p.x - omega, p.y);

		double tp = Math.atan2(p.y, p.x - omega);
		double tq = Math.atan2(q.y, q.x - omega);
		double tm = (tp + tq) / 2;

		return new Point3D(omega + R * Math.cos(tm), R * Math.sin(tm), 0);
	}

}
