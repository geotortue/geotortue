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
	
	private static final OPTKey CONFIRM = new OPTKey(FWWritableDirectory.class, "confirmDirectory");
	private final OPTKey key;
	private boolean initialized;
	
	/**
	 * @param owner
	 * @param tag
	 * @param key
	 */
	public FWWritableDirectoryEntry(final Window owner, final String tag, final OPTKey key) {
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
		if (!initialized) {
			return null;
		}

		return super.getValue().getAbsolutePath();
	}
	
	@Override
	public void fetchValue(String v) {
		setValue(new File(v));
	}
	
	/**
	 * 
	 * @return file
	 * 
	 * @deprecated
     * This method is no longer acceptable to get file value
     * <p> Use {@link FWWritableDirectoryEntry#getValueSafely()} instead.
	 */
	@Override @Deprecated
	public File getValue() { 
		return super.getValue();
	}
	
	public File askForValue() throws UninitializedDirectoryException {
		while (!initialized) {
			FWOptionPane.showInformationMessage(owner, key);
			final File f = askForDirectory();
			if (f == null) {
				throw new UninitializedDirectoryException();
			}

			initialized = setValue(f);
		}
		return super.getValue();
	}

	
	private File askForDirectory() {
		final File f = FWManager.getSubDirectory(owner, getXMLTag());
		if (f == null) {
			return null;
		}

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
	 * @return file
	 * 
	 * @throws UninitializedDirectoryException 
	 */
	public File getValueSafely() throws UninitializedDirectoryException {
		if (initialized) {
			return super.getValue();
		}

		throw new UninitializedDirectoryException();
	}

	@Override
	protected boolean setValue(File v) {
		initialized = (v != null);
		return super.setValue(v);
	}
	
	
}
