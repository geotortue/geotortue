package geotortue.renderer;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import fw.app.Translator.TKey;
import fw.app.prefs.FWIntegerEntry;
import fw.geometry.util.QRotation;
import fw.gui.FWLabel;
import fw.gui.FWSettings;
import fw.gui.FWSettingsActionPuller;
import fw.gui.layout.VerticalPairingLayout;
import fw.gui.params.FWInteger;
import fw.gui.params.FWParameterListener;
import fw.renderer.core.GraphicSpace;
import fw.xml.XMLCapabilities;
import fw.xml.XMLReader;
import fw.xml.XMLWriter;
import geotortue.core.Turtle;
import geotortue.geometry.GTGeometryI;
import geotortue.geometry.GTGeometryManager;
import geotortue.geometry.GTPoint;
import geotortue.geometry.proj.GTPerspective;
import geotortue.geometry.proj.GTPerspectiveManager;
import geotortue.gui.GTActionSettingsPuller;


public class GTGraphicSpace extends GraphicSpace<GTPoint> implements XMLCapabilities, FWSettings {

	private static final TKey NAME = new TKey(GTGraphicSpace.class, "settings");

	@Override
	public TKey getTitle() {
		return NAME;
	}

	private static final TKey HD_SETTINGS = new TKey(HDExportTool.class, "hdSettings");
	private static final TKey WIDTH = new TKey(GTGraphicSpace.class, "width");
	private static final TKey HEIGHT = new TKey(GTGraphicSpace.class, "height");
	private static final TKey NEW_SIZE = new TKey(GTGraphicSpace.class, "newSize");
	private static final TKey SCALE_FACTOR = new TKey(GTGraphicSpace.class, "scaleFactor");
	
	private final HDExportTool hdExportTool = new HDExportTool();
	
	private final FWInteger width = new FWInteger("width", getSize().width, 200, 2048, 1);
	
	private final FWInteger height = new FWInteger("height", getSize().height, 200, 2048, 1);
	

	public GTGraphicSpace(GTGeometryManager gm, GTRendererManager rm, GTPerspectiveManager pm) {
		super(gm, rm, pm);
		addChangeListeners();
	}

	public GTGraphicSpace(GTGraphicSpace gs) {
		super(gs);
		addChangeListeners();
	}
	
	private void addChangeListeners() {
		width.addParamaterListener(new FWParameterListener<Integer>() {
			@Override
			public void settingsChanged(Integer value) {
				setWidth(value);
			}
		});
		height.addParamaterListener(new FWParameterListener<Integer>() {
			@Override
			public void settingsChanged(Integer value) {
				setHeight(value);
			}
		});
	}

	private GTGeometryI getGeometry() {
		return (GTGeometryI) geometryManager.getGeometry();
	}
	
	public void update() {
		GTGeometryManager gm = (GTGeometryManager) geometryManager;
		GTRendererManager rm = (GTRendererManager) rendererManager;
		
		rm.setRendererType(getGeometry().getRendererType());
		super.update();
		
		gm.getMouseManager().addMouseListener(rm.getRenderer());
	}
	
	public void updateRenderer() {
		rendererManager.getRenderer().updateSettings();
		repaint();
	}

	public GTRendererI getRenderer() {
		return  (GTRendererI) rendererManager.getRenderer();
	}
	
	public GTPerspective getPerspective() {
		return (GTPerspective) perspectiveManager.getPerspective();
	}

	
	public void resetGeometry() {
		getRenderer().setSpaceTransform(new QRotation());
		getGeometry().init(getRenderer());
		repaint();
	}
	
	public void centerOn(Turtle t) {
		getGeometry().centerWorldOn(t.getPosition(), getRenderer());
		repaint();
	}

	public BufferedImage getImage() {
		return getRenderer().getImage();
	}
	
	
	/*
	 * XML
	 */
	
	@Override
	public JPanel getPane() {
		JPanel pane = super.getPane();
		pane.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		return pane;
	}

	@Override
	public String getXMLTag() {
		return "GTGraphicSpace";
	}
	
	
	@Override
	public XMLWriter getXMLProperties() {
		XMLWriter e = new XMLWriter(this);
		width.storeValue(e);
		height.storeValue(e);
		return e;
	}
	
	@Override
	public XMLReader loadXMLProperties(XMLReader e) {
		XMLReader child = e.popChild(this);
		width.fetchValue(child, 640);
		height.fetchValue(child, 480);
		update();
		return child;
	}
	
	
	
	/*
	 * FWSettings 
	 */
	private Dimension maximumSize = new Dimension(2048, 2048);

	public void setMaximumSize(int w, int h) {
		if (maximumSize.width != w) {
			maximumSize.width = w;
			if (getSize().width > w)
				setWidth(w-1);
			((SpinnerNumberModel) width.getComponent().getModel()).setMaximum(w);
		} 
		if (maximumSize.height != h) {
			maximumSize.height = h;
			if (getSize().height > h)
				setHeight(h-1);
			((SpinnerNumberModel) height.getComponent().getModel()).setMaximum(h);
		}
	}
	
	@Override
	public void setWidth(int w) {
		if (w>=200 && w<maximumSize.width) {
			int w1 = (w<maximumSize.width-20) ? w : maximumSize.width-1; 
			super.setWidth(w1);
			if (widthSpinner != null)
				widthSpinner.setValue(w1);
		}
	}

	@Override
	public void setHeight(int h) {
		if (h>=200 && h<maximumSize.height) {
			int h1 = (h<maximumSize.height-20) ? h : maximumSize.height-1;
			super.setHeight(h1);
			if (heightSpinner != null)
				heightSpinner.setValue(h1);
		}
	}

	public void forceHeight(int h) {
		super.setHeight(h);
	}

	private JSpinner widthSpinner, heightSpinner;
	
	@Override
	public JPanel getSettingsPane(final FWSettingsActionPuller actions) {
		FWParameterListener<Integer> l = new FWParameterListener<Integer>() {
			
			@Override
			public void settingsChanged(Integer value) {
				actions.fire(GTActionSettingsPuller.REPAINT);
			}
		};
		widthSpinner = width.getComponent(l); 
		heightSpinner = height.getComponent(l);
		return VerticalPairingLayout.createPanel(10, 10, 
				new FWLabel(WIDTH, SwingConstants.RIGHT), widthSpinner,
				new FWLabel(HEIGHT, SwingConstants.RIGHT), heightSpinner); 
	}

	/*
	 * 
	 */
	
	public HDExportTool getHdExportTool() {
		return hdExportTool;
	}

	public class HDExportTool implements FWSettings {
		
		@Override
		public TKey getTitle() {
			return HD_SETTINGS;
		}



		private FWIntegerEntry scaleFactor = new FWIntegerEntry(GTGraphicSpace.this, "scaleFactor", 2, 2, 8);
		
		private JLabel sizeLabel = new JLabel("");
		
		private void updateSizeLabel() {
			Dimension d = getSize();
			int k = scaleFactor.getValue();
			sizeLabel.setText(d.width*k +" × "+d.height*k +"");
		}
		
		public HDExportTool() {
			updateSizeLabel();
			scaleFactor.addParamaterListener(new FWParameterListener<Integer>() {
				@Override
				public void settingsChanged(Integer value) {
					updateSizeLabel();
				}
			});
		}

		public BufferedImage getHDImage() {
			return getRenderer().getHDImage(scaleFactor.getValue());
		}

		@Override
		public JPanel getSettingsPane(final FWSettingsActionPuller actions) {
			return VerticalPairingLayout.createPanel(10, 10, 
					new FWLabel(SCALE_FACTOR, SwingConstants.RIGHT), scaleFactor.getComponent(),
					new FWLabel(NEW_SIZE, SwingConstants.RIGHT), sizeLabel);
		}
	}
}