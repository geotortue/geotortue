/**
 * 
 */
package fw.app.prefs;

import fw.gui.params.FWString;
import fw.xml.XMLTagged;

public class FWStringEntry extends FWString implements FWPreferenceEntryI {
	
	private final String defaultValue;
	
	public FWStringEntry(XMLTagged parent, String key, String def) {
		super(parent.getXMLTag()+"."+key, def);
		this.defaultValue = def;
		FWLocalPreferences.register(this);
	}

	
	@Override
	public String getEntryValue() {
		return getValue();
	}

	@Override
	public void fetchValue(String v) {
		setValue(v);
	}

	@Override
	public void fetchDefaultValue() {
		setValue(defaultValue);
	}
}