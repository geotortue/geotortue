package geotortue.geometry;

import java.util.Vector;

import fw.geometry.util.QRotation;
import geotortue.core.GTMessageFactory.GTTrouble;
import geotortue.core.Turtle;
import geotortue.core.TurtlePen;
import geotortue.geometry.GTGeometryI.GeometryException;
import geotortue.geometry.obj.GTObject;
import geotortue.geometry.obj.GTPolygon.NonFlatPolygonException;
import geotortue.geometry.obj.GTSegment;

public class GTTransport {

	final private Vector<GTPoint> points = new Vector<GTPoint>();
	
	final private GTRotation rotation;
	final private MODE mode;
	
	public enum MODE {DO_NOTHING, REDEFINE_ROTATION, CUMULATE_ROTATION};
	
	public GTTransport(MODE m, QRotation r, GTPoint... ps) throws GeometryException {
		this.mode = m;
		this.rotation = (r!=null)? new GTRotation(r) : null;
		

		for (GTPoint p : ps) {
			if (isNaN(p))
				throw new GeometryException(GTTrouble.GTJEP_TRANSPORT);
			points.add(p);
		}
	}
	
	public GTTransport(GTPoint p, GTPoint q) throws GeometryException {
		this(MODE.DO_NOTHING, null, p, q);
	}

	public void apply(Turtle t) throws NonFlatPolygonException {
		t.setPosition(points.lastElement());
		switch (mode) {
		case REDEFINE_ROTATION:
			t.setRotation(rotation);
			break;
		case CUMULATE_ROTATION:
			t.setRotation(rotation.apply(t.getRotation4D()));
		default:
			break;
		}
	}
	
	private boolean isNaN(GTPoint p) {
		return isNaN(p.getU1()) || isNaN(p.getU2()) || isNaN(p.getU3());
	}
	
	private boolean isNaN(double x) {
		return Double.isNaN(x) || Double.isInfinite(x);
	}

	
	public Vector<GTObject> getPath(TurtlePen pen) {
		Vector<GTObject> path = new Vector<GTObject>();
		for (int idx = 0; idx < points.size()-1; idx++)
			path.add(new GTSegment(points.get(idx), points.get(idx+1), pen));
		return path;
	}
}