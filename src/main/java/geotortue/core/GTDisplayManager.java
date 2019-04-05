package geotortue.core;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import fw.app.Translator.TKey;
import fw.geometry.util.Point3D;
import fw.gui.FWComboBox;
import fw.gui.FWLabel;
import fw.gui.FWSettings;
import fw.gui.FWSettingsActionPuller;
import fw.gui.layout.HorizontalCenteredFlowLayout;
import fw.gui.layout.VerticalFlowLayout;
import fw.gui.layout.VerticalPairingLayout;
import fw.gui.params.FWFileAssistant;
import fw.gui.params.FWFileAssistant.FKey;
import fw.gui.params.FWInteger;
import fw.gui.params.FWParameterListener;
import fw.renderer.core.RenderJob;
import fw.renderer.core.RendererI;
import fw.text.TextStyle;
import fw.text.TextStyleWFontColorP;
import fw.xml.XMLCapabilities;
import fw.xml.XMLReader;
import fw.xml.XMLWriter;
import geotortue.geometry.GTGeometry;
import geotortue.geometry.GTGeometryManager;
import geotortue.geometry.GTPoint;
import geotortue.gui.GTActionSettingsPuller;
import geotortue.gui.GTFileLoaderWidget;
import geotortue.renderer.GTRendererI;


public class GTDisplayManager implements RenderJob<GTPoint>, XMLCapabilities, FWSettings {

	/**
	 * 
	 */
	private static final TKey OPEN = new TKey(GTDisplayManager.class, "open.tooltip");
	private static final TKey DELETE = new TKey(GTDisplayManager.class, "delete.tooltip");
	private static final TKey NAME = new TKey(GTDisplayManager.class, "settings");

	private static BufferedImage DEFAULT_BACKGROUND;
	
	@Override
	public TKey getTitle() {
		return NAME;
	}

	private static final TKey STYLE_SETTINGS = new TKey(GTDisplayManager.class, "styleSettings");	
	private static final TKey NONE = new TKey(GTDisplayManager.class, "none");
	private static final TKey FONT_SIZE = new TKey(GTDisplayManager.class, "fontSize");
	private static final TKey FONT_FAMILY = new TKey(GTDisplayManager.class, "fontFamily");
	private static final TKey OFFSET_Y = new TKey(GTDisplayManager.class, "offsetY");
	private static final TKey OFFSET_X = new TKey(GTDisplayManager.class, "offsetX");
	private static final FKey IMG_EXT = new FKey(GTActions.class, new String[]{"png", "jpg", "gif"});
	
	private final TurtleManager turtleManager;
	private final GTGeometryManager geometryManager;
	
	private FWInteger offsetX = new FWInteger("offsetX", 0, -2048, 2048, 1);
	private FWInteger offsetY = new FWInteger("offsetY", 0, -2048, 2048, 1);
	
	private BufferedImage img = null;
	private boolean isImageEditable = true;
	
	private String imageTitle = "";
	
	private final TextStyleWFontColorP style = new TextStyleWFontColorP(this, "font", UIManager.getFont("FWFont.font12"), Color.BLACK); 
	private boolean startingPointVisible = false;

	public GTDisplayManager(GTGeometryManager gm, TurtleManager tm) {
		this.geometryManager = gm;
		this.turtleManager = tm;
		if (DEFAULT_BACKGROUND !=null) {
				setImage(DEFAULT_BACKGROUND, "");
				isImageEditable = false;
		}
	}
	
	@Override
	public synchronized void display(RendererI<GTPoint> renderer) {
		GTGeometry g = geometryManager.getGeometry();

		g.paintBackground(renderer);
		
		Point3D origin3d = g.get3DCoordinates(new GTPoint(0, 0, 0));
		if (img != null) {
			double x = origin3d.x + offsetX.getValue();
			double y = origin3d.y + offsetY.getValue();

			renderer.drawImage(img, new Point3D(x, y, 0));
		}

		GTRendererI gtRenderer = (GTRendererI) renderer;
		if (startingPointVisible)
			gtRenderer.drawTick(origin3d, 5);
		
		turtleManager.display(gtRenderer, g);
		
		g.paintForeground(renderer);
	}

	public TextStyle getStyle() {
		return style;
	}

	public void toggleStartingPointVisibility() {
		startingPointVisible = !startingPointVisible;
	}
	
	public BufferedImage getImage() {
		return img;
	}
	
	
	public void setImage(BufferedImage img, String title) {
		if (img!=null && isImageEditable){
			this.img = img;
			this.imageTitle = title;
		}
	}
	
	public void removeImage() {
		if (img==null && isImageEditable)
			return;
		this.img = null;
		this.imageTitle = "";
	}
	
	/*
	 * XML
	 */
	
	public String getXMLTag() {
		return "GTDisplayManager";
	}
	
	public XMLWriter getXMLProperties() {
		XMLWriter e = new XMLWriter(this);
		offsetX.storeValue(e);
		offsetY.storeValue(e);
		e.setAttribute("imageTitle", imageTitle);
		return e;
	}
	
	@Override
	public XMLReader loadXMLProperties(XMLReader e) {
		XMLReader child = e.popChild(this);
		offsetX.fetchValue(child, 0);
		offsetY.fetchValue(child, 0);
		imageTitle = child.getAttribute("imageTitle", "");
		return child;
	}

	/*
	 * FWS
	 */
	

	public FWSettings getStyleSettings() {
		return new FWSettings() {
			
			@Override
			public TKey getTitle() {
				return STYLE_SETTINGS;
			}

			@Override
			public JPanel getSettingsPane(FWSettingsActionPuller actions) {
				FWComboBox fontFamilyCB = style.getFontFamilyComboBox(actions.get(GTActionSettingsPuller.REPAINT));
				JSpinner fontSizeSpinner = style.getFontSizeSpinner(actions.get(GTActionSettingsPuller.REPAINT));
				return VerticalPairingLayout.createPanel(10, 10, 
						new FWLabel(FONT_FAMILY, SwingConstants.RIGHT), fontFamilyCB,
						new FWLabel(FONT_SIZE, SwingConstants.RIGHT), fontSizeSpinner);
			}
		};
	}
	
	private final FWFileAssistant imgFile = new FWFileAssistant(null, IMG_EXT);

	@Override
	public JPanel getSettingsPane(final FWSettingsActionPuller actions) {
		final JLabel imgLabel= new FWLabel(NONE);
		if (!imageTitle.equals(""))
			imgLabel.setText(imageTitle);
		
		GTFileLoaderWidget imgButtons = new GTFileLoaderWidget(imgLabel, OPEN, "image-open.png", DELETE, "edit-clear.png") {
			private static final long serialVersionUID = 429531432318954179L;

			@Override
			protected void open() {
				try {
					File file = imgFile.getFileForLoading();
					if (file == null)
						return;
					setImage(ImageIO.read(file), file.getName());
					imgLabel.setText(imageTitle);
					actions.fire(FWSettingsActionPuller.REPAINT); 
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}

			@Override
			protected void delete() {
				removeImage();
				imgLabel.setText(NONE.translate());
				actions.fire(FWSettingsActionPuller.REPAINT);
			}
		};
		imgButtons.setEnabled(isImageEditable);
		
		FWParameterListener<Integer> l = new FWParameterListener<Integer>() {

			@Override
			public void settingsChanged(Integer value) {
				actions.fire(FWSettingsActionPuller.REPAINT);
			}
		};
		

		
		return VerticalFlowLayout.createPanel(
				HorizontalCenteredFlowLayout.createPanel(imgButtons),
				VerticalPairingLayout.createPanel(10, 10,new FWLabel(OFFSET_X, SwingConstants.RIGHT), offsetX.getComponent(l),
				new FWLabel(OFFSET_Y, SwingConstants.RIGHT), offsetY.getComponent(l)));
	}

	/**
	 * @param im
	 */
	public static void setDefaultBackground(BufferedImage im) {
		DEFAULT_BACKGROUND = im;
	}
}