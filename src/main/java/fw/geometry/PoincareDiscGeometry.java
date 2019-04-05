package fw.geometry;

import java.awt.Color;

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


public class PoincareDiscGeometry<T extends GPoint> extends HyperbolicGeometry<T> {
	

	private static final TKey NAME = new TKey(PoincareDiscGeometry.class, "name");

	@Override
	public TKey getTitle() {
		return NAME;
	}
	


	public Point3D get3DCoordinates(T gp) {
		Point3D p = super.get3DCoordinates(gp);
		return new Point3D(p.x/(1+p.z), p.y/(1+p.z), 0);
	}

	@Override
	public void draw(GSegment<T> s, RendererI<T> rc) {
		Point3D p = get3DCoordinates(s.getStart());
		Point3D q = get3DCoordinates(s.getEnd());
		
		//if (Math.abs((p.x-q.x))*rc.getUnit() < 1) {
			//rc.drawLine(p, q, s.getColor());
			//return;
		//}
		
		double np = 1 + p.x*p.x + p.y*p.y;  
		double nq = 1 + q.x*q.x + q.y*q.y;
		
		double det = 2 * (p.x*q.y - p.y*q.x);
		
		if (Math.abs(det)<1E-8) {
			rc.drawLine(p, q, s.getColor(), s.getThickness());
			return;
		}
		
		final Point3D omega = new Point3D( (np*q.y - nq*p.y)/det,  (nq*p.x - np*q.x)/det, 0);
		//double alpha = Math.acos(1/omega.abs());
		double R = Math.sqrt(omega.abs2()-1);//Math.tan(alpha);
		
		//double err = Math.abs(alpha-Math.PI/2);
		//if (Double.isNaN(err) || err<1E-9) {
			//rc.drawLine(p, q, s.getColor());
			//return;
		//}
		
		double tp = Math.atan2(p.y-omega.y, p.x-omega.x);
		double tq = Math.atan2(q.y-omega.y, q.x-omega.x);
		
		if (Math.abs(tq - tp) > Math.PI)
			if (tp > tq)
				tp -= 2*Math.PI;
			else 
				tp += 2*Math.PI;

		rc.drawArc2D(omega, R, tp, tq, s.getColor(), s.getThickness());
	}
	
	@Override
	public void draw(GDot<T> d, RendererI<T> r){
		Point3D p = get3DCoordinates(d.getPosition());
		r.drawPixel(p, d.getColor(), d.getThickness());
	}

	public void init(final RendererI<T> r) {
		r.reset();
		r.setBackground(Color.DARK_GRAY);
		r.setUnit(Math.min(r.getWidth(), r.getHeight()) / 2.1);
	}

	@Override
	public void paintBackground(RendererI<T> rc) {
		rc.fillCircle(new Point3D(0, 0, 0), 1, Color.WHITE);
	}
	
	@Override
	public void paintForeground(RendererI<T> rc) {
		rc.drawCircleFog(new Point3D(0, 0, 0), 1, Color.DARK_GRAY);
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
		return "PoincareDiscGeometry";
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
	 * FWS
	 */
	
	public JPanel getSettingsPane(FWSettingsActionPuller actions) {
		return new JPanel();
	}

}