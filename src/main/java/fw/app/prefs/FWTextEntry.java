/**
 * 
 */
package fw.app.prefs;

import fw.gui.params.FWText;
import fw.xml.XMLTagged;

/**
 *
 */
public class FWTextEntry extends FWText implements FWPreferenceEntryI {
	
	private final String defaultValue;
	
	public FWTextEntry(XMLTagged parent, String key, String def, int cols) {
		super(parent.getXMLTag()+"."+key, def, cols);
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