package geotortue.renderer;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import fw.app.Translator.TKey;
import fw.gui.FWLabel;
import fw.gui.FWSettings;
import fw.gui.FWSettingsActionPuller;
import fw.gui.layout.VerticalFlowLayout;
import fw.gui.layout.VerticalPairingLayout;
import fw.gui.params.FWBoolean;
import fw.gui.params.FWInteger;
import fw.gui.params.FWParameterListener;
import fw.renderer.core.RendererSettingsI;
import fw.xml.XMLCapabilities;
import fw.xml.XMLReader;
import fw.xml.XMLWriter;
import geotortue.gui.GTActionSettingsPuller;


public class GTRendererSettings implements RendererSettingsI, XMLCapabilities, FWSettings {


	private static final TKey NAME = new TKey(GTRendererSettings.class, "settings");

	@Override
	public TKey getTitle() {
		return NAME;
	}

	private static final TKey ANTI_ALIASING = new TKey(GTRendererSettings.class, "antiAliasing");
	private static final TKey BLUR = new TKey(GTRendererSettings.class, "blur");
	private static final TKey POLYGON = new TKey(GTRendererSettings.class, "GTPolygon");
	private static final TKey MAX_DIST = new TKey(GTRendererSettings.class, "maxDist");
	private static final TKey MAX_NUM_POINTS = new TKey(GTRendererSettings.class, "maxNumPoints");

	private final FWBoolean antiAliasing = new FWBoolean("antiAliasing", true);
	private static FWInteger maxNumPoints = new FWInteger("maxNumPoints", 30000, 1000, 100000, 1000); 
	private static FWInteger maxDist = new FWInteger("maxDist", 8, 2, 128);
	private static FWInteger blur = new FWInteger("blur", 20,0, 100, 10);
	
	public boolean isAntiAliasOn() {
		return antiAliasing.getValue();
	}
	
	public static int getMaxNumPoints() {
		return maxNumPoints.getValue();
	}

	public static int getMaxDist() {
		return maxDist.getValue();
	}
	
	public float getBlurAmount() {
		return blur.getValue()/(100f);
	}
	
	/*
	 * XML
	 */
	
	@Override
	public String getXMLTag() {
		return "GTRendererSettings";
	}

	@Override
	public XMLReader loadXMLProperties(XMLReader e) {
		XMLReader child = e.popChild(this);
		antiAliasing.fetchValue(child, true);
		maxNumPoints.fetchValue(child, 30000);
		maxDist.fetchValue(child, 8);
		blur.fetchValue(child, 20);
		return child;
	}

	@Override
	public XMLWriter getXMLProperties() {
		XMLWriter e = new XMLWriter(this);
		antiAliasing.storeValue(e);
		maxNumPoints.storeValue(e);
		maxDist.storeValue(e);
		blur.storeValue(e);
		return e;
	}

	/*
	 * FWS
	 */
	@Override
	public JPanel getSettingsPane(final FWSettingsActionPuller actions) {
		JCheckBox antiAliasingCB = antiAliasing.getComponent(new FWParameterListener<Boolean>() {
			@Override
			public void settingsChanged(Boolean value) {
				actions.fire(GTActionSettingsPuller.UPDATE_RENDERER);
			}
		});
		
		FWParameterListener<Integer> l = new FWParameterListener<Integer>() {
			
			@Override
			public void settingsChanged(Integer value) {
				actions.fire(GTActionSettingsPuller.UPDATE_RENDERER);
			}
		};
		
		return VerticalFlowLayout.createPanel( 
				VerticalPairingLayout.createPanel( 
						new FWLabel(ANTI_ALIASING, SwingConstants.RIGHT), antiAliasingCB,
						new FWLabel(BLUR, SwingConstants.RIGHT), blur.getComponent(l)
						),
				new FWLabel(POLYGON, SwingConstants.CENTER),
				VerticalPairingLayout.createPanel( 
						new FWLabel(MAX_DIST, SwingConstants.RIGHT), maxDist.getComponent(l),
						new FWLabel(MAX_NUM_POINTS, SwingConstants.RIGHT), maxNumPoints.getComponent(l)));
	}
}