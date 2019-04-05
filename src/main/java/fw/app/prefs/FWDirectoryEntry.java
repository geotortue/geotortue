/**
 * 
 */
package fw.app.prefs;

import java.awt.Window;
import java.io.File;

import fw.app.FWManager;
import fw.gui.params.FWDirectory;


/**
 * 
 * 
 * 
 * @author Salvatore Tummarello
 *
 */
public class FWDirectoryEntry extends FWDirectory  implements FWPreferenceEntryI {

	private final File defaultValue = FWManager.getUserDirectory();
	
	public FWDirectoryEntry(Window owner, String tag) {
		super(tag);
		FWLocalPreferences.register(this);
	}
	

	@Override
	public void fetchDefaultValue() {
		setValue(defaultValue);
	}
	
	@Override
	public String getEntryValue() {
		return getValue().getAbsolutePath();
	}
	
	@Override
	public void fetchValue(String v) {
		setValue(new File(v));
	}	
}