/**
 * 
 */
package fw.app.prefs;

import java.io.File;

import fw.app.FWManager;
import fw.app.FWRestrictedAccessException;
import fw.gui.params.FWFile;
import fw.xml.XMLTagged;

/**
 *
 */
public class FWFileEntry extends FWFile implements FWPreferenceEntryI {

	public FWFileEntry(XMLTagged parent, String key) {
		super(parent.getXMLTag()+"."+key);
		FWLocalPreferences.register(this);
	}
	

	@Override
	public void fetchDefaultValue() {
		try {
			setValue(FWManager.getConfigDirectory());
		} catch (FWRestrictedAccessException e) {
		}
	}
	
	@Override
	public String getEntryValue() {
		return getValue().getAbsolutePath();
	}
	
	@Override
	public void fetchValue(String v) {
		setValue(new File(v));
	}
	
	public boolean setValue(File v) {
		return super.setValue(v);
	}

}
