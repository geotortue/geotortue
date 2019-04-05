package geotortue.geometry.obj;

import fw.geometry.util.Point3D;
import geotortue.core.TurtlePen;
import geotortue.geometry.GTPoint;

public class GTDiscGeoPolygon extends GTTriangulatedPolygon {

	public GTDiscGeoPolygon(GTPoint p, TurtlePen pen) {
		super(p, pen);
	}

	@Override
	public Point3D getMiddle(Point3D p, Point3D q) {
		double np = 1 + p.x*p.x + p.y*p.y;  
		double nq = 1 + q.x*q.x + q.y*q.y;
		
		double det = 2 * (p.x*q.y - p.y*q.x);
		
		if (Math.abs(det)<1E-8) {
			return new Point3D((p.x+q.x)/2, (p.y+q.y)/2, 0);
		}
		
		final Point3D omega = new Point3D( (np*q.y - nq*p.y)/det,  (nq*p.x - np*q.x)/det, 0);
		double R = Math.sqrt(omega.abs2()-1);
		
		double tp = Math.atan2(p.y-omega.y, p.x-omega.x);
		double tq = Math.atan2(q.y-omega.y, q.x-omega.x);
		
		if (Math.abs(tq - tp) > Math.PI)
			if (tp > tq)
				tp -= 2*Math.PI;
			else 
				tp += 2*Math.PI;
		double tm = (tp+tq)/2;
		
		return new Point3D(omega.x + R * Math.cos(tm), omega.y + R * Math.sin(tm), 0);
	}
}


