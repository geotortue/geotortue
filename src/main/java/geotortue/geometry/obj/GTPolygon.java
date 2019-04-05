package geotortue.geometry.obj;

import fw.app.Translator.TKey;
import fw.geometry.obj.GObject;
import geotortue.core.TurtlePen;
import geotortue.geometry.GTGeometryI;
import geotortue.geometry.GTPoint;
import geotortue.renderer.GTRendererI;

public abstract class GTPolygon  extends GObject implements GTObject {
	
	protected final static TKey FILLING_ERROR = new TKey(GTPolygon.class, "fillingError"); 


	public GTPolygon(TurtlePen pen) {
		super(pen.getColor(), pen.getThickness());
	}

	@Override
	public void draw(GTGeometryI g, GTRendererI r) {
		g.fill(this, r);
	}

	public abstract void add(GTPoint position) throws NonFlatPolygonException;

	public void add(GTArc arc) {
	}
	
	public static class NonFlatPolygonException extends Exception {
		private static final long serialVersionUID = -2716036942041913448L;
		
	}

}
