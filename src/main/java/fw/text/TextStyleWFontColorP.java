/**
 * 
 */
package fw.text;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JComponent;
import javax.swing.text.StyleConstants;

import fw.app.prefs.FWColorEntry;
import fw.gui.FWSettingsAction;
import fw.gui.params.FWColor;
import fw.gui.params.FWParameterListener;
import fw.xml.XMLReader;
import fw.xml.XMLTagged;
import fw.xml.XMLWriter;

/**
 * @author Salvatore Tummarello
 *
 */
public class TextStyleWFontColorP extends TextStyleWFontP {
	private static final long serialVersionUID = 4989076329982934991L;
	
	private final FWColor color;
	
	public TextStyleWFontColorP(XMLTagged parent, String key, Font font, Color c) {
		super(parent, key, font);
		this.color = new FWColorEntry(parent, key + ".color", c);
		StyleConstants.setForeground(this, color.getValue());
	}
	
	/**
	 * create TextStyle with FWParams
	 * @param key
	 * @param font
	 * @param c
	 */
	public TextStyleWFontColorP(String key, Font font, Color c) {
		super(key, font);
		this.color = new FWColor(key + ".color", c);
		StyleConstants.setForeground(this, c);
	}
	
	
	public JComponent getColorBox(final FWSettingsAction... actions) {
		return color.getComponent(new FWParameterListener<Color>() {
			@Override
			public void settingsChanged(Color v) {
				StyleConstants.setForeground(TextStyleWFontColorP.this, v);
				for (FWSettingsAction action : actions) 
					action.fire();
			}
		});
	}
	
	
	
	public XMLWriter getXMLProperties() {
		XMLWriter e = super.getXMLProperties();
		color.storeValue(e);
		return e;
	}
	
	@Override
	public XMLReader loadXMLProperties(XMLReader e) {
		XMLReader child = super.loadXMLProperties(e);
		color.fetchValue(e, Color.BLACK);
		return child;
	}

}
