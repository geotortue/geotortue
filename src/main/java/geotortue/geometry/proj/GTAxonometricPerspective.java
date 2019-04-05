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



public class GTAxonometricPerspective extends GTLinearPerspective {
	
	private static final TKey NAME = new TKey(GTAxonometricPerspective.class, "name");

	@Override
	public TKey getTitle() {
		return NAME;
	}

	private static final TKey OMEGA = new TKey(GTAxonometricPerspective.class, "omega");
	private static final TKey ALPHA = new TKey(GTAxonometricPerspective.class, "alpha");
	
	private FWAngle omega = new FWAngle("omega", Math.PI/4);
	private FWAngle alpha = new FWAngle("alpha", Math.PI/4);
	
	public GTAxonometricPerspective() {
		updateMatrix();
	}

	@Override
	protected PerspectiveMatrix getMatrix() {
		double cosOmega = Math.cos(omega.getValue());
		double sinOmega = Math.sin(omega.getValue());
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
		return "GTAxonometricPerspective";
	}
	
	@Override
	public XMLWriter getXMLProperties() {
		XMLWriter e = new XMLWriter(this);
		omega.storeValue(e);
		alpha.storeValue(e);
		return e;
	}

	@Override
	public XMLReader loadXMLProperties(XMLReader e) {
		XMLReader child = super.loadXMLProperties(e);
		omega.fetchValue(child, Math.PI/4);
		alpha.fetchValue(child, Math.PI/4);
		updateMatrix();
		return child;
	}

	/*
	 * FWS
	 */
	public JPanel getSettingsPane(final FWSettingsActionPuller actions) {
		FWParameterListener<Double> l = getupdateAction(actions);
		return VerticalPairingLayout.createPanel(10, 10, 
				new FWLabel(ALPHA), alpha.getComponent(l),
				new FWLabel(OMEGA), omega.getComponent(l));
	}
};