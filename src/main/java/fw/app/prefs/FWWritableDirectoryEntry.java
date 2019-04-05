/**
 * 
 */
package fw.app.prefs;

import java.awt.Window;
import java.io.File;

import javax.swing.JButton;

import fw.app.FWManager;
import fw.gui.FWOptionPane;
import fw.gui.FWOptionPane.ANSWER;
import fw.gui.FWOptionPane.OPTKey;
import fw.gui.params.FWWritableDirectory;

/**
 * @author Salvatore Tummarello
 *
 */
public class FWWritableDirectoryEntry extends FWWritableDirectory implements FWPreferenceEntryI {

	private final File defaultValue = FWManager.getUserDirectory();
	
	private final OPTKey CONFIRM = new OPTKey(FWWritableDirectory.class, "confirmDirectory");
	private final OPTKey key;
	private boolean initialized;
	
	/**
	 * @param owner
	 * @param tag
	 */
	public FWWritableDirectoryEntry(Window owner, String tag, OPTKey key) {
		super(owner, tag);
		this.key = key;
		this.initialized = FWLocalPreferences.register(this);
	}

	@Override
	public void fetchDefaultValue() {
		setValue(defaultValue);
	}
	
	@Override
	public String getEntryValue() {
		if (initialized)
			return super.getValue().getAbsolutePath();
		return null;
	}
	
	@Override
	public void fetchValue(String v) {
		setValue(new File(v));
	}
	
	@Override @Deprecated
	public File getValue() { // use getValueSafely() instead
		return super.getValue();
	}
	
	public File askForValue() throws UninitializedDirectoryException {
		while (!initialized) {
			FWOptionPane.showInformationMessage(owner, key);
			File f = askForDirectory();
			if (f!=null)
				initialized = setValue(f);
			else throw new UninitializedDirectoryException();
		}
		return super.getValue();
	}

	
	private File askForDirectory() {
		File f = FWManager.getSubDirectory(owner, getXMLTag());
		if (f==null)
			return null;
		ANSWER answer = FWOptionPane.showConfirmDialog(owner, CONFIRM, f.getPath());
		switch (answer) {
		case YES:
			return f;
		case NO :
			return askForDirectory();
		default:
			return null;
		}
	}

	public class UninitializedDirectoryException extends Exception {
		private static final long serialVersionUID = 740666226320628794L;
	}

	/**
	 * @return
	 * @throws UninitializedDirectoryException 
	 */
	public File getValueSafely() throws UninitializedDirectoryException {
		if (initialized)
			return super.getValue();
		throw new UninitializedDirectoryException();
	}

	@Override
	protected boolean setValue(File v) {
		initialized = (v!=null);
		return super.setValue(v);
	}
	
	
}
