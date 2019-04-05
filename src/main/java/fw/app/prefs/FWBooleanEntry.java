/**
 * 
 */
package fw.app.prefs;

import fw.gui.params.FWBoolean;
import fw.xml.XMLTagged;

public class FWBooleanEntry extends FWBoolean implements FWPreferenceEntryI { 
	
	private final boolean defaultValue;
	
	public FWBooleanEntry(XMLTagged parent, String key, boolean def) {
		super(parent.getXMLTag()+"."+key, def);
		this.defaultValue = def;
		FWLocalPreferences.register(this);
	}
	
	@Override
	public String getEntryValue() {
		return String.valueOf(getValue());
	}

	@Override
	public void fetchValue(String v) {
		setValue(Boolean.valueOf(v));
	}

	@Override
	public void fetchDefaultValue() {
		setValue(defaultValue);
	}
	
	
}