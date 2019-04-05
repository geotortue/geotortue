/**
 * 
 */
package fw.app.prefs;

import fw.gui.params.FWInteger;
import fw.xml.XMLTagged;

/**
 * 
 * INTEGER
 *
 */
public class FWIntegerEntry extends FWInteger implements FWPreferenceEntryI {
	
	private final int defaultValue;
	
	public FWIntegerEntry(XMLTagged parent, String key, int def, int min, int max, int step) {
		super(parent.getXMLTag()+"."+key, def, min, max, step);
		this.defaultValue = def;
		FWLocalPreferences.register(this);
	}
	
	public FWIntegerEntry(XMLTagged parent, String key, int def, int min, int max) {
		this(parent, key, def, min, max, 1);
	}

	@Override
	public String getEntryValue() {
		return String.valueOf(getValue());
	}

	@Override
	public void fetchValue(String v) {
		setValue(Integer.valueOf(v));
	}

	@Override
	public void fetchDefaultValue() {
		setValue(defaultValue);
		
	}
}