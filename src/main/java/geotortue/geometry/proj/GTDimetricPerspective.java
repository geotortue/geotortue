package geotortue.geometry.proj;

import javax.swing.JPanel;

import fw.app.Translator.TKey;
import fw.geometry.proj.PerspectiveMatrix;
import fw.gui.FWLabel;
import fw.gui.FWSettingsActionPuller;
import fw.gui.layout.VerticalPairingLayout;
import fw.gui.params.FWAngle;
import fw.gui.params.FWParameterListener;
import fw.xml.XMLReader;
import fw.xml.XMLWriter;


public class GTDimetricPerspective extends GTLinearPerspective {
	
	private static final TKey NAME = new TKey(GTDimetricPerspective.class, "name");

	@Override
	public TKey getTitle() {
		return NAME;
	}

	
	private static final TKey ALPHA = new TKey(GTDimetricPerspective.class, "alpha");

	private FWAngle alpha = new FWAngle("alpha", Math.PI/6);
	
	public GTDimetricPerspective() {
		updateMatrix();
	}

	@Override
	protected PerspectiveMatrix getMatrix() {
		double cosOmega = Math.sqrt(1/2.);
		double sinOmega = Math.sqrt(1/2.);
		double cosAlpha = Math.cos(alpha.getValue());
		double sinAlpha = Math.sin(alpha.getValue());
		return new PerspectiveMatrix(
				cosOmega, 			  0, 		-sinOmega,
				-sinOmega * sinAlpha, cosAlpha, -cosOmega * sinAlpha,
				 sinOmega * cosAlpha, sinAlpha,  cosOmega * cosAlpha);
	}

	/*
	 * XML
	 */
	
	@Override
	public String getXMLTag() {
		return "GTDimetricPerspective";
	}
	
	@Override
	public XMLWriter getXMLProperties() {
		XMLWriter e = new XMLWriter(this);
		alpha.storeValue(e);
		return e;
	}

	@Override
	public XMLReader loadXMLProperties(XMLReader e) {
		XMLReader child = super.loadXMLProperties(e);
		alpha.fetchValue(child, Math.PI/6);
		updateMatrix();
		return child;
	}

	/*
	 * FWS
	 */
	public JPanel getSettingsPane(final FWSettingsActionPuller actions) {
		FWParameterListener<Double> l = getupdateAction(actions);
		return VerticalPairingLayout.createPanel(10, 10, 
				new FWLabel(ALPHA), alpha.getComponent(l));
	}
}