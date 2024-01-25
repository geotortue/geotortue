/**
 * 
 */
package fw.gui;

import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import fw.app.Translator.TKey;
import fw.gui.layout.VerticalPairingLayout;
import fw.text.TextStyleWFontP;
import fw.xml.XMLTagged;

/**
 * @author Salvatore Tummarello
 *
 */
public class FWAccessibilityManager implements FWSettings {

	private final Vector<FWAccessible> accessibles = new Vector<>();
	private final XMLTagged xmlTag = XMLTagged.Factory.create("FWAccessibilityManager");
	private final TextStyleWFontP style = new TextStyleWFontP(xmlTag, "font", UIManager.getFont("FWFont.font12"));
	private static final FWAccessibilityManager COMMON_INSTANCE = new FWAccessibilityManager();
	
	private static final TKey TITLE = new TKey(FWAccessibilityManager.class, "title");
	private static final TKey FONT_SIZE = new TKey(FWAccessibilityManager.class, "fontSize");
	private static final TKey FONT_FAMILY = new TKey(FWAccessibilityManager.class, "fontFamily");
	
	public static void register(final FWAccessible a) {
		SwingUtilities.invokeLater(() -> {
				COMMON_INSTANCE.accessibles.add(a);
				a.setFont(COMMON_INSTANCE.style);
			}
		);
	}

	public static void update() {
		for (FWAccessible a : COMMON_INSTANCE.accessibles)
			a.setFont(COMMON_INSTANCE.style);
	}
	
	
	@Override
	public JPanel getSettingsPane(FWSettingsActionPuller actions) {
		FWSettingsAction updateButtonAction = new FWSettingsAction() {
			@Override
			public void fire() {
				update();
			}
		};
		
		FWComboBox fontFamilyCB = style.getFontFamilyComboBox(updateButtonAction);
		JSpinner fontSizeSpinner = style.getFontSizeSpinner(updateButtonAction);
		
		return VerticalPairingLayout.createPanel(10, 10, new FWLabel(FONT_FAMILY, SwingConstants.RIGHT), fontFamilyCB,
				new FWLabel(FONT_SIZE, SwingConstants.RIGHT), fontSizeSpinner);
	}

	@Override
	public TKey getTitle() {
		return TITLE;
	}

	public static FWSettings getCommonInstance() {
		return COMMON_INSTANCE;
	}
}
