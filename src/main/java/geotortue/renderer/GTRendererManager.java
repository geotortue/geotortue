package geotortue.renderer;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SwingConstants;

import fw.app.Translator.TKey;
import fw.gui.FWLabel;
import fw.gui.FWSettings;
import fw.gui.FWSettingsActionPuller;
import fw.gui.layout.VerticalPairingLayout;
import fw.gui.params.FWBoolean;
import fw.gui.params.FWInteger;
import fw.gui.params.FWParameterListener;
import fw.renderer.core.RenderJob;
import fw.renderer.core.RendererManager;
import fw.xml.XMLCapabilities;
import fw.xml.XMLReader;
import fw.xml.XMLWriter;
import geotortue.geometry.GTPoint;
import geotortue.gui.GTActionSettingsPuller;


public class GTRendererManager extends RendererManager<GTPoint> implements FWSettings, XMLCapabilities {

	private static final TKey NAME = new TKey(GTRendererManager.class, "settings");

	@Override
	public TKey getTitle() {
		return NAME;
	}


	private static final TKey ANAGLYPH_ENABLED = new TKey(GTRendererManager.class, "anaglyphEnabled");
	private static final TKey ANAGLYPH_SHIFT = new TKey(GTRendererManager.class, "anaglyphShift");
	
	private final FWBoolean anaglyphEnabled = new FWBoolean("anaglyphEnabled", false);
	private GTLightingContext lightingContext;
	private RENDERER_TYPE rendererType = RENDERER_TYPE.FW2D;
	
	public enum RENDERER_TYPE {FW2D, FW3D, FW4D};
	
	private GTRendererManager(GTRendererSettings s, GTLightingContext gtlc, GTRendererI... renderers){
		super(s, renderers);
		this.lightingContext = gtlc;
	}
	
	private GTRendererManager(GTRendererSettings s, GTLightingContext gtlc, RenderJob<GTPoint> job) {
		this(s, gtlc, 
					new GTFWRenderer2D(s, job),
					new GTFWRenderer3D(s, job, gtlc),
					new GTFWRenderer4D(s, job, gtlc),
					new GTAnaglyphRenderer(s, job, gtlc)
					//new GTGLRenderer(s, job)
				);
	}
	

	public GTRendererManager(RenderJob<GTPoint> job) {
		this (new GTRendererSettings(), new GTLightingContext(), job);
	}
	
	public GTRendererSettings getRendererSettings() {
		return (GTRendererSettings) super.getRendererSettings();
	}
	
	public GTLightingContext getLightingContext() {
		return lightingContext;
	}

	public void setRendererType(RENDERER_TYPE type) {
		rendererType = type;
	}
	
	public void updateSettings() {
//		if (glEnabled.isSelected() && GLToolKit.isGLAvailable())
//			setRenderer(-1);
//		else
		switch (rendererType) {
		case FW2D:
			setRenderer(0);
			break;
		case FW3D:
			if (anaglyphEnabled.getValue())
				setRenderer(3);
			else
				setRenderer(1);
			break;
		case FW4D:
			setRenderer(2);
			break;

		}
		super.updateSettings();
	}
	
	/*
	 * XML
	 */
	
	@Override
	public String getXMLTag() {
		return "GTRendererManager";
	}
	

	@Override
	public XMLWriter getXMLProperties() {
		XMLWriter e = new XMLWriter(this);
		anaglyphEnabled.storeValue(e);
		e.put(getRendererSettings());
		e.put(getLightingContext());
		return e;
	}

	@Override
	public XMLReader loadXMLProperties(XMLReader e) {
		XMLReader child = e.popChild(this);
		anaglyphEnabled.fetchValue(child, false);
		getRendererSettings().loadXMLProperties(child);
		getLightingContext().loadXMLProperties(child);
		return child;
	}

			
	/*
	 * FWS
	 */

	public JPanel getSettingsPane(final FWSettingsActionPuller actions) {
		
		final FWInteger shift=  ((GTAnaglyphRenderer) getAvailableRenderers()[3]).getShift();
		shift.setEnabled(anaglyphEnabled.getValue());
		
		JCheckBox anaglyphEnabledCB = anaglyphEnabled.getComponent(new FWParameterListener<Boolean>() {
			@Override
			public void settingsChanged(Boolean value) {
				shift.setEnabled(anaglyphEnabled.getValue());
				actions.fire(GTActionSettingsPuller.UPDATE_GEOMETRY);
			}
		});
		
		JSpinner shiftSpinner = shift.getComponent(new FWParameterListener<Integer>() {
			@Override
			public void settingsChanged(Integer value) {
				actions.fire(GTActionSettingsPuller.UPDATE_RENDERER);
			}
		});

		return VerticalPairingLayout.createPanel(
				new FWLabel(ANAGLYPH_ENABLED, SwingConstants.RIGHT), anaglyphEnabledCB,
				new FWLabel(ANAGLYPH_SHIFT, SwingConstants.RIGHT), shiftSpinner);
	}
}