package fw.app.prefs;

import java.awt.Color;

import fw.gui.params.FWColor;
import fw.xml.XMLTagged;

public class FWColorEntry extends FWColor implements FWPreferenceEntryI {

	private final Color defaultValue;
	
	public FWColorEntry(XMLTagged parent, String key, Color c) {
		super(parent.getXMLTag()+"."+key, c);
		this.defaultValue = c;
		FWLocalPreferences.register(this);
	}

	@Override
	public void fetchDefaultValue() {
		setValue(defaultValue);
	}

	@Override
	public void fetchValue(String v) {
		setValue(new Color(Integer.valueOf(v)));
	}

	@Override
	public String getEntryValue() {
		return String.valueOf(getValue().getRGB());
	}
}
