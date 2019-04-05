package fw.geometry;

import fw.geometry.obj.GDot;
import fw.geometry.obj.GPoint;
import fw.geometry.obj.GSegment;
import fw.geometry.util.Point3D;
import fw.gui.FWSettings;
import fw.renderer.core.RendererI;
import fw.xml.XMLCapabilities;

public interface GeometryI<T extends GPoint> extends XMLCapabilities, FWSettings {

	public Point3D get3DCoordinates(T p);

	public void draw(GSegment<T> s, RendererI<T> r);
	
	public void draw(GDot<T> d, RendererI<T> r);
	
	public double distance(T p, T q);

	public void init(RendererI<T> r);
	
	public void paintBackground(RendererI<T> r);
	
	public void paintForeground(RendererI<T> r);
	
	public int getDimensionCount();
	
}