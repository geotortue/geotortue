package geotortue.sandbox;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import fw.app.Translator.TKey;
import fw.app.prefs.FWFileEntry;
import fw.gui.FWButton;
import fw.gui.FWButton.BKey;
import fw.gui.FWButton.FWButtonListener;
import fw.gui.FWComboBox;
import fw.gui.FWDialog;
import fw.gui.FWLabel;
import fw.gui.FWOptionPane;
import fw.gui.FWOptionPane.OPTKey;
import fw.gui.FWSettingsAction;
import fw.gui.FWTabbedPane;
import fw.gui.layout.HorizontalFixedLayout;
import fw.gui.layout.VerticalFlowLayout;
import fw.gui.layout.VerticalPairingLayout;
import fw.gui.params.FWFileAssistant;
import fw.gui.params.FWFileAssistant.FKey;
import fw.text.TextStyleWFontP;
import fw.xml.XMLCapabilities;
import fw.xml.XMLEntry;
import fw.xml.XMLException;
import fw.xml.XMLFile;
import fw.xml.XMLReader;
import fw.xml.XMLTagged;
import fw.xml.XMLWriter;
import geotortue.core.GTDocumentFactory;
import geotortue.sandbox.GTSandBoxButtonPane.TYPE;


public class GTSandBox implements XMLCapabilities {

	private static final TKey CONFIG = new TKey(GTSandBox.class, "config");
	private static final TKey BUTTON_FONT = new TKey(GTSandBox.class, "font");
	private static final BKey IMPORT = new BKey(GTSandBox.class, "import");
	private static final BKey EXPORT = new BKey(GTSandBox.class, "export");
	private static final TKey PRESET = new TKey(GTSandBox.class, "preset");
	private static final OPTKey INVALID_FILE = new OPTKey(GTSandBox.class, "invalidFile");
	
	private static final TKey FONT_SIZE = new TKey(GTSandBox.class, "fontSize");
	private static final TKey FONT_FAMILY = new TKey(GTSandBox.class, "fontFamily");

	
	private static final TKey LEFT_PANE = new TKey(GTSandBox.class, "leftPane");
	private static final TKey RIGHT_PANE = new TKey(GTSandBox.class, "rightPane");
	private static final TKey SETTINGS = new TKey(GTSandBox.class, "settings");
	private static final TKey TITLE = new TKey(GTSandBox.class, "settings.title");
	
	private final GTDocumentFactory docFactory;
	private final GTSandBoxButtonPane leftButtonPane, rightButtonPane;
	
	public final static XMLTagged XML_TAG = XMLTagged.Factory.create("GTSandBox");
	final static TextStyleWFontP buttonStyle = new TextStyleWFontP(XML_TAG, "button", UIManager.getFont("FWFont.font12"));
	private final static FWFileEntry FILE = new FWFileEntry(XML_TAG, "file");
	private static final FKey XML_FILE = new FKey(GTSandBox.class, "xml");
	
	private final GTPresetSandBoxConfigurations presetSBConfigs = new GTPresetSandBoxConfigurations(this);
	private final FWComboBox presetCB = presetSBConfigs.getComboBox(); 
	
	public GTSandBox(GTDocumentFactory f) {
		this.docFactory = f;
		this.leftButtonPane = new GTSandBoxButtonPane(docFactory, TYPE.LEFT);
		this.rightButtonPane = new GTSandBoxButtonPane(docFactory, TYPE.RIGHT);
		try {
			loadXMLProperties(new XMLFile(FILE.getValue()).parse());
		} catch (IOException | XMLException e) {
			loadDefault();
		}
	}	
	
	private void loadDefault() {
		FILE.fetchDefaultValue();
		presetSBConfigs.load();
	}
	
	private void updateButtonStyle() {
		for (Component c: leftButtonPane.getComponents()) 
			((GTSandBoxButton) c).updateStyle();
		
		for (Component c: rightButtonPane.getComponents()) 
			((GTSandBoxButton) c).updateStyle();

	}

	public Component[] getExtraComponents() {
		return new Component[]{new GTSandBoxCompass(), new JScrollPane(leftButtonPane), new JScrollPane(rightButtonPane)};
	}

	@Override
	public String getXMLTag() {
		return "GTSandBox";
	}
	
	@Override
	public XMLWriter getXMLProperties() {
		XMLWriter e = new XMLWriter(this);
		e.put(leftButtonPane);
		e.put(rightButtonPane);
		return e;
	}

	@Override
	public XMLReader loadXMLProperties(XMLReader e) {
		final XMLReader child = e.popChild(this);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (child != XMLEntry.NULL_ENTRY) {
					leftButtonPane.loadXMLProperties(child);
					rightButtonPane.loadXMLProperties(child);
					updateButtonStyle();
				}
			}
		});
		return child;
	}
	
	void loadPresetConfiguration(XMLReader e) {
		FILE.fetchDefaultValue();
		loadXMLProperties(e);
	}

	
	private FWDialog dial;
	
	public void showDialog(Window owner){
		JLabel label = new JLabel();
		label.setPreferredSize(new Dimension(175, 10));
		
		FWTabbedPane tabbedPane = new FWTabbedPane();
		tabbedPane.add(getSettingsPane(), SETTINGS.translate());
		tabbedPane.add(new GTSandBoxButtonPaneEditor(owner, leftButtonPane), LEFT_PANE.translate());
		tabbedPane.add(new GTSandBoxButtonPaneEditor(owner, rightButtonPane), RIGHT_PANE.translate());
		
		dial = new FWDialog(owner, TITLE, tabbedPane, true, true);
		dial.setModal(true);
		dial.setSize(525, 700);
		dial.setLocationRelativeTo(owner);
		dial.setVisible(true);
	}
	
	private JPanel getSettingsPane() {
		FWButton importButton = new FWButton(IMPORT, new FWButtonListener() {
			@Override
			public void actionPerformed(ActionEvent ae, JButton source) {
				File f = new FWFileAssistant(dial, XML_FILE).getFileForLoading();
				if (f==null)
					return;
				try {
					XMLReader e = new XMLFile(f).parse();
					if (e.hasChild(XML_TAG)) {
						loadXMLProperties(e);
						FILE.setValue(f);
						dial.validate();
						dial.repaint();
					} else
						FWOptionPane.showErrorMessage(dial, INVALID_FILE);
				} catch (XMLException ex) {
					FWOptionPane.showErrorMessage(dial, INVALID_FILE);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		});
		
		FWButton exportButton = new FWButton(EXPORT, new FWButtonListener() {
			@Override
			public void actionPerformed(ActionEvent ae, JButton source) {
				File f = new FWFileAssistant(dial, XML_FILE).getFileForSaving();
				if (f==null)
					return;

				BufferedWriter w = null;
				try {
					w = new BufferedWriter(new FileWriter(f));
					w.write(getXMLProperties().getXMLCode());
				} catch (IOException ex) {
					ex.printStackTrace();
				} finally {
					try {
						w.close();
					} catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			}
		});
		
		JPanel importExportButtons = HorizontalFixedLayout.createPanel(10, importButton, exportButton);
		
		JPanel modelP = VerticalPairingLayout.createPanel(new FWLabel(PRESET), presetCB);
				
		JPanel settingsP = VerticalFlowLayout.createPanel(modelP, importExportButtons);
		settingsP.setBorder(BorderFactory.createTitledBorder(CONFIG.translate()));
		
		FWSettingsAction updateButtonAction = new FWSettingsAction() {
			@Override
			public void fire() {
				updateButtonStyle();
			}
		};
		FWComboBox fontFamilyCB = buttonStyle.getFontFamilyComboBox(updateButtonAction);
		JSpinner fontSizeSpinner = buttonStyle.getFontSizeSpinner(updateButtonAction);
		JPanel styleP = VerticalPairingLayout.createPanel(10, 10, new FWLabel(FONT_FAMILY, SwingConstants.RIGHT), fontFamilyCB,
				new FWLabel(FONT_SIZE, SwingConstants.RIGHT), fontSizeSpinner);
		styleP.setBorder(BorderFactory.createTitledBorder(BUTTON_FONT.translate()));
		
		
		return VerticalFlowLayout.createPanel(settingsP, styleP);
	}
}