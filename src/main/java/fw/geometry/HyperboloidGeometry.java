package fw.geometry;

import java.awt.Color;

import javax.swing.JPanel;

import fw.app.Translator.TKey;
import fw.geometry.obj.GDot;
import fw.geometry.obj.GPoint;
import fw.geometry.obj.GSegment;
import fw.geometry.util.MathException.ZeroVectorException;
import fw.geometry.util.Point3D;
import fw.geometry.util.QRotation;
import fw.gui.FWSettingsActionPuller;
import fw.renderer.core.RendererI;
import fw.xml.XMLReader;
import fw.xml.XMLWriter;


public class HyperboloidGeometry<T extends GPoint> extends HyperbolicGeometry<T> {

	private static final TKey NAME = new TKey(HyperboloidGeometry.class, "name");

	@Override
	public TKey getTitle() {
		return NAME;
	}
	



	@Override
	public void draw(GSegment<T> s, RendererI<T> r) {
	}
	
	@Override
	public void draw(GDot<T> d, RendererI<T> r){
	}

	@Override
	public void paintBackground(RendererI<T> r) {
	}

	@Override
	public void init(RendererI<T> r) {
		r.setBackground(Color.WHITE);
		r.reset();
		
		try {
			r.setSpaceTransform(new QRotation(new Point3D(0, 1, 0), Math.PI));
		} catch (ZeroVectorException ex) {
			ex.printStackTrace(); // should not occur
		}
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
		return "";
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
	
	public JPanel getSettingsPane(final FWSettingsActionPuller actions) {
		return new JPanel();
	}


	@Override
	public void paintForeground(RendererI<T> r) {
	}
}
