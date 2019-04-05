package geotortue.geometry.obj;

import java.util.ArrayList;

import fw.geometry.util.Point3D;
import geotortue.geometry.GTEuclidean2DGeometry;
import geotortue.geometry.GTPoint;

public class GTPath {
	
	public enum ELEMENT_ID {segment, arc};
	private final GTEuclidean2DGeometry geo; 
	private final ArrayList<PathElement> elements = new ArrayList<>();
	private final Point3D startingPoint;
	private final int thickness;
	
	public GTPath(GTEuclidean2DGeometry g, GTPoint initialPoint, int thickness) {
		this.geo = g;
		this.startingPoint = g.get3DCoordinates(initialPoint);
		this.thickness = thickness;
	}
	
	public Point3D getStartingPoint() {
		return startingPoint;
	}
	
	public ArrayList<PathElement> getElements() {
		return elements;
	}
	
	public void addSegment(GTPoint p) {
		elements.add(new SegmentElement(geo.get3DCoordinates(p)));
	}
	
	public void addArc(GTArc a) {
		elements.add(new ArcElement(a));
	}
	
	public static class PathElement {
		private final ELEMENT_ID id;
		
		private PathElement(ELEMENT_ID id) {
			this.id = id;
		}

		public ELEMENT_ID getId() {
			return id;
		}
	}
	
	
	public class SegmentElement extends PathElement {
		private final Point3D endPoint;

		private SegmentElement(Point3D end) {
			super(ELEMENT_ID.segment);
			this.endPoint = end;
		}

		public Point3D getEndPoint() {
			return endPoint;
		}
	}

	public class ArcElement extends PathElement {
		private final double radius;
		private final double startAngle;
		private final double endAngle;
		private final Point3D center;
		
		private ArcElement(GTArc arc) {
			super(ELEMENT_ID.arc);
			this.center = geo.get3DCoordinates(arc.getCenter());
			Point3D p1 = geo.get3DCoordinates(arc.getStartingPoint());
			Point3D q = p1.getTranslated(center.opp());
			this.radius = q.abs();
			this.startAngle = Math.atan2(q.y, q.x);
			double angle = arc.getAngle();
			this.endAngle = startAngle+angle;
		}

		public double getRadius() {
			return radius;
		}

		public double getStartAngle() {
			return startAngle;
		}

		public double getEndAngle() {
			return endAngle;
		}

		public Point3D getCenter() {
			return center;
		}
	}

	public int getThickness() {
		return thickness;
	}

}
