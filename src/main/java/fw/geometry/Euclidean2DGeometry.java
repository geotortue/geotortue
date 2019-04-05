package fw.geometry;

import javax.swing.JPanel;

import fw.app.Translator.TKey;
import fw.geometry.obj.GDot;
import fw.geometry.obj.GPoint;
import fw.geometry.obj.GSegment;
import fw.geometry.util.Point3D;
import fw.gui.FWSettingsActionPuller;
import fw.renderer.core.RendererI;
import fw.xml.XMLReader;
import fw.xml.XMLWriter;


public class Euclidean2DGeometry<T extends GPoint> implements GeometryI<T> {
	
	private static final TKey NAME = new TKey(Euclidean2DGeometry.class, "name");

	@Override
	public TKey getTitle() {
		return NAME;
	}
	
	@Override
	public Point3D get3DCoordinates(T p) {
		return new Point3D(p.getU1(), p.getU2(), 0);
	}

	@Override
	public double distance(T p, T q) {
		double dx = q.getU1()- p.getU1();
		double dy = q.getU2()- p.getU2();
		return Math.sqrt(dx*dx + dy*dy);
	}
	
	public void draw(GSegment<T> s, RendererI<T> rc){
		Point3D p = get3DCoordinates(s.getStart());
		Point3D q = get3DCoordinates(s.getEnd());
		rc.drawLine(p, q, s.getColor(), s.getThickness());
	}
	
	public void draw(GDot<T> d, RendererI<T> r){
		Point3D p = get3DCoordinates(d.getPosition());
		r.drawPixel(p, d.getColor(), d.getThickness());
	}
	
	@Override
	public void init(RendererI<T> r) {
		r.reset();
	}
	
	@Override
	public void paintBackground(RendererI<T> rc){
	}
	
	@Override
	public int getDimensionCount() {
		return 2;
	}
	
	
	/*
	 * XML
	 */


	@Override
	public String getXMLTag() {
		return "Euclidean2DGeometry";
	}
	
	@Override
	public XMLWriter getXMLProperties() {
		return new XMLWriter(this);
	}

	@Override
	public XMLReader loadXMLProperties(XMLReader e) {
		return e.popChild(this);
	}

	/*
	 * 
	 */

	@Override
	public JPanel getSettingsPane(FWSettingsActionPuller actions) {
		return new JPanel();
	}

	@Override
	public void paintForeground(RendererI<T> r) {
	}
}
