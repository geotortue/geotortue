package geotortue.geometry.proj;

import java.awt.Dimension;
import java.awt.geom.Point2D.Double;

import javax.swing.JPanel;

import fw.app.Translator.TKey;
import fw.geometry.proj.PerspectiveI;
import fw.geometry.proj.ZPoint;
import fw.geometry.util.MathException;
import fw.geometry.util.Pixel;
import fw.geometry.util.Point3D;
import fw.gui.FWSettings;
import fw.gui.FWSettingsActionPuller;
import fw.xml.XMLCapabilities;
import fw.xml.XMLReader;
import fw.xml.XMLWriter;


public abstract class GTPerspective implements PerspectiveI, FWSettings, XMLCapabilities {
	
	protected abstract PerspectiveI getDelegateProjection();
	
	@Override
	public Pixel toScreen(ZPoint p) throws MathException {
		return getDelegateProjection().toScreen(p);
	}

	@Override
	public double getMaximumZDepth() {
		return getDelegateProjection().getMaximumZDepth();
	}

	@Override
	public ZPoint toZSpace(Point3D p) throws InvisibleZPointException {
		return getDelegateProjection().toZSpace(p);
	}
	
	@Override
	public Double toScreen2D(ZPoint p) {
		return getDelegateProjection().toScreen2D(p);
	}

	@Override
	public Point3D liftTo3DSpace(Pixel p) {
		return getDelegateProjection().liftTo3DSpace(p);
	}
	
	@Override
	public void setScreenSize(Dimension d) {
		getDelegateProjection().setScreenSize(d);
	}

	public String toString() {
		return getTitle().translate();
	}
	
	
	public abstract TKey getTitle();
	
	
	/*
	 * XML
	 */
	
	@Override
	public XMLWriter getXMLProperties() {
		return new XMLWriter(this);
	}

	
	@Override
	public XMLReader loadXMLProperties(XMLReader e) {
		return  e.popChild(this);
	}

	/*
	 * FWS
	 */
	public JPanel getSettingsPane(final FWSettingsActionPuller actions) {
		return new JPanel();
	}
}