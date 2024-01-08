package fw.app;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;

import fw.files.CSVException;
import fw.files.FileUtilities.HTTPException;
import fw.gui.FWAccessibilityManager;
import fw.gui.FWAccessible;
import fw.text.TextStyle;

public abstract class FWLauncher implements FWApplicationI, FWAccessible {
	
	public static final Color LIGHT_BLUE = new Color(245, 251, 255);
	public static final Color YELLOW = new Color(255, 255, 150);
	public static final Color PURPLE = new Color(200, 0, 50);
	public static final Color BLUE_BORDER = new Color(205, 220, 230);
	
	public static final BufferedImage ICON = FWManager.getImage("/cfg/icon.png");
	
	public FWLauncher(Locale locale) {
		FWManager.init(this);
		
		System.setProperty("awt.useSystemAAFontSettings","on");
		System.setProperty("swing.aatext", "true");
		
		// UI
		initUI();
		
		try {
			Translator.buildTable(locale);
		} catch (IOException | CSVException | HTTPException ex) {
			ex.printStackTrace();
		}
		
		// Tooltip
		ToolTipManager.sharedInstance().setInitialDelay(100);

		// Header xml file
		try {
			FWManager.setHeader("/cfg/header.xml");
		} catch (HTTPException ex) {
			ex.printStackTrace();
		}
		
		FWAccessibilityManager.register(this);		
	}
	
	protected void initUI() {
		initFonts();
		UIManager.put("ToolTip.background", LIGHT_BLUE);
		UIManager.put("TextPane.selectionBackground", YELLOW);
		UIManager.put("TextField.selectionBackground", YELLOW);
		UIManager.put("TextArea.selectionBackground", YELLOW);
		UIManager.put("EditorPane.selectionBackground", YELLOW);
		UIManager.put("MenuItem.selectionBackground", LIGHT_BLUE);
		UIManager.put("MenuItem.acceleratorSelectionForeground", PURPLE);
		UIManager.put("Menu.selectionBackground", LIGHT_BLUE);
		UIManager.put("List.selectionBackground", YELLOW);
		UIManager.put("List.focusSelectedCellHighlightBorder", BorderFactory.createLineBorder(BLUE_BORDER, 1));
		UIManager.put("List.border", BorderFactory.createLineBorder(BLUE_BORDER, 1));
		UIManager.put("ComboBox.selectionBackground", LIGHT_BLUE);

		Vector<Object> buttonGradient = getGradient(0.15, 0.5, new Color(220, 220, 230), new Color(245, 245, 250), new Color(190, 200, 240));
		UIManager.put("Button.gradient", buttonGradient);
		UIManager.put("ToggleButton.gradient", buttonGradient);
		UIManager.put("RadioButton.gradient", buttonGradient);
		UIManager.put("CheckBoxMenuItem.gradient", buttonGradient);
		UIManager.put("CheckBox.gradient", buttonGradient);
		UIManager.put("RadioButtonMenuItem.gradient", buttonGradient);
		UIManager.put("Slider.gradient", buttonGradient);
		UIManager.put("ScrollBar.gradient", buttonGradient);
		
		
		UIManager.put("ToggleButton.select", new Color(250, 255, 250));
		UIManager.put("TabbedPane.selected", new Color(250, 255, 250));
		UIManager.put("TabbedPane.contentAreaColor", new Color(238, 238, 238));

		UIManager.put("TitledBorder.border", BorderFactory.createLineBorder(BLUE_BORDER));
		
		// FW
		UIManager.put("EnhancedTextPane.highlightBorder", new Color(200, 220, 210)); 
		UIManager.put("EnhancedTextPane.highlight", new Color(245, 255, 250));
		UIManager.put("FWTitledPane.lineColor", new Color(200, 220, 210));
		UIManager.put("FWToolBar.background", new Color(240, 240, 242));
	}

	protected void initFonts(){
		final String incosolontaG = "Inconsolata-g";
		final String dejaVuSans = "DejaVu Sans";
		final String dejaVuSansMono = "DejaVu Sans Mono";
		final String dialog = "Dialog";
		
		Font font = null;
		Font font12 = null;
		
		if (isFontInstalled(incosolontaG) || (installFont(incosolontaG, "Inconsolata-g.ttf")))
			font = new Font(incosolontaG, Font.PLAIN, 15); 

		if (font==null) 
			if (isFontInstalled(dejaVuSansMono) || (installFont(dejaVuSansMono, "DejaVuSansMono.ttf")))
				font = new Font(dejaVuSansMono, Font.PLAIN, 14);

		
		if (isFontInstalled(dejaVuSans) || (installFont(dejaVuSans, "DejaVuSans.ttf"))) {
			if (font==null)
				font = new Font(dejaVuSans, Font.PLAIN, 14) ;
			font12 = new Font(dejaVuSans, Font.PLAIN, 12);
		}
		
		if (font == null) 
			font = new Font(dialog, Font.PLAIN, 14);
		
		if (font12 == null)
			font12 = new Font(dialog, Font.PLAIN, 12);
		
		UIManager.put("FWFont", font); // FW Font
		
		TextStyle style = new TextStyle();
		style.setFontFamily(font12.getFamily());
		style.setFontSize(font12.getSize());
		
		setFont(style);
	}

	@Override
	public void setFont(TextStyle s) {
		Font font12 = s.getFont();
		Font font14 = s.deriveFont(TextStyle.PLAIN, 2);
		Font font12b = s.deriveFont(TextStyle.BOLD, 0);
		
		UIManager.put("FWFont.font12", font12); // FW Font
		UIManager.put("FWFont.font10", font12.deriveFont(10f)); // FW Font
		UIManager.put("FWTabs.font", font12); // FW Font
		UIManager.put("RoundToggleButton.font", font14);
		
		UIManager.put("Label.font", font12);
		UIManager.put("CheckBox.font", font12);
		UIManager.put("ComboBox.font", font12);
		UIManager.put("MenuItem.font", font12);
		UIManager.put("List.font", font14);
		UIManager.put("Menu.font", font14);
		UIManager.put("Button.font", font12);
		UIManager.put("TextField.font", font12);
		UIManager.put("RadioButton.font", font12);
		UIManager.put("ToggleButton.font", font12);
		UIManager.put("ProgressBar.font", font14);
		UIManager.put("TabbedPane.font", font12b);
		UIManager.put("TitledBorder.font", font12);
		UIManager.put("ToolTip.font", font12);		
	}

	protected final boolean isFontInstalled(String font) {
		for (String f : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) 
			if (f.equals(font)) 
				return true;
		return false;
	}
	
	protected final boolean installFont(String name, String file) {
		InputStream is = getClass().getResourceAsStream("/cfg/"+file);
		
		try {
			Font f = Font.createFont(Font.TRUETYPE_FONT, is);
			f = f.deriveFont(14f);
			GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(f);
		} catch (FontFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return isFontInstalled(name);
	}

	private static Vector<Object> getGradient(double m1, double m2, Color c0, Color c1, Color c2) {
		Vector<Object> v = new Vector<Object>();
		v.add(m1);
		v.add(m2);
		v.add(c0);
		v.add(c1);
		v.add(c2);
		return v;
	}
}