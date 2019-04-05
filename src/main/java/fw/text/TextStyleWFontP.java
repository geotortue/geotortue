/**
 * 
 */
package fw.text;

import java.awt.Font;

import javax.swing.JSpinner;

import fw.app.prefs.FWFontFamilyEntry;
import fw.app.prefs.FWIntegerEntry;
import fw.gui.FWComboBox;
import fw.gui.FWSettingsAction;
import fw.gui.params.FWFontFamily;
import fw.gui.params.FWInteger;
import fw.gui.params.FWParameterListener;
import fw.xml.XMLTagged;

/**
 * @author Salvatore Tummarello
 *
 */
public class TextStyleWFontP extends TextStyle {
	private static final long serialVersionUID = 4989076329982934991L;
	
	private final FWFontFamily fontFamily;
	private final FWInteger fontSize;
	
	public TextStyleWFontP(XMLTagged parent, String key, Font font) {
		this.fontFamily = new FWFontFamilyEntry(parent, key + ".family", font.getFamily());
		this.fontSize = new FWIntegerEntry(parent, key + ".size", font.getSize(), 2, 60);
		setFontSize(fontSize.getValue());
		setFontFamily(fontFamily.getValue());
	}
	
	/**
	 * create TextStyle with FWParams
	 * @param key
	 * @param font
	 * @param c
	 */
	public TextStyleWFontP(String key, Font font) {
		if (font != null) {
			String family = font.getFamily();
			int size = font.getSize();
			this.fontFamily = new FWFontFamily(key + ".family", family);
			this.fontSize = new FWInteger(key + ".size", size, 2, 60);
			setFontFamily(family);
			setFontSize(size);
		} else {
			this.fontFamily = null;
			this.fontSize = null;
		}
	}
	
	public FWComboBox getFontFamilyComboBox(final FWSettingsAction... actions) {
		return fontFamily.getComponent(new FWParameterListener<String>() {
			@Override
			public void settingsChanged(String v) {
				setFontFamily(v);
				for (FWSettingsAction action : actions) 
					action.fire();
			};
		});
	}
	
	public JSpinner getFontSizeSpinner(final FWSettingsAction... actions) {
		return fontSize.getComponent(new FWParameterListener<Integer>() {
			@Override
			public void settingsChanged(Integer v) {
				setFontSize(v);
				for (FWSettingsAction action : actions) 
					action.fire();
			}
		});
	}
	
	@Override
	public void setFontSize(int size){
		super.setFontSize(size);
		fontSize.getComponent().setValue(size);
	}
}