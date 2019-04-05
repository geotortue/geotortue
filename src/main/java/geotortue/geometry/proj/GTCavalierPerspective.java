package geotortue.geometry.proj;

import javax.swing.JPanel;

import fw.app.Translator.TKey;
import fw.geometry.proj.PerspectiveMatrix;
import fw.gui.FWLabel;
import fw.gui.FWSettingsActionPuller;
import fw.gui.layout.VerticalPairingLayout;
import fw.gui.params.FWAngle;
import fw.gui.params.FWDouble;
import fw.gui.params.FWParameterListener;
import fw.xml.XMLReader;
import fw.xml.XMLWriter;


public class GTCavalierPerspective extends GTLinearPerspective {

	private static final TKey NAME = new TKey(GTCavalierPerspective.class, "name");

	@Override
	public TKey getTitle() {
		return NAME;
	}

	private static final TKey RATIO = new TKey(GTCavalierPerspective.class, "ratio");
	private static final TKey VANISHING_POINT = new TKey(GTCavalierPerspective.class, "vanishingPoint");

	private FWAngle alpha = new FWAngle("alpha", -3*Math.PI/4);;
	private FWDouble ratio = new FWDouble("ratio", 0.5, 0.1, 2, 0.01);
	
	public GTCavalierPerspective() {
		updateMatrix();
	}

	@Override
	protected PerspectiveMatrix getMatrix() {
		double k = ratio.getValue();
		double a = alpha.getValue();
		return new PerspectiveMatrix(
				1, 0, k*Math.cos(a),
				0, 1, k*Math.sin(a),
				0, 0, k);
	}
	
	/*
	 * XML
	 */

	@Override
	public String getXMLTag() {
		return "GTCavalierPerspective";
	}
	
	@Override
	public XMLWriter getXMLProperties() {
		XMLWriter e = new XMLWriter(this);
		ratio.storeValue(e);
		alpha.storeValue(e);
		return e;
	}

	@Override
	public XMLReader loadXMLProperties(XMLReader e) {
		XMLReader child = super.loadXMLProperties(e);
		ratio.fetchValue(child, 0.5);
		alpha.fetchValue(child, -3*Math.PI/4);
		updateMatrix();
		return child;
	}

	/*
	 * FWS
	 */
	public JPanel getSettingsPane(final FWSettingsActionPuller actions) {
		FWParameterListener<Double> l = getupdateAction(actions);
		return VerticalPairingLayout.createPanel(10, 10, 
				new FWLabel(VANISHING_POINT), alpha.getComponent(l),
				new FWLabel(RATIO), ratio.getComponent(l));
	}
}