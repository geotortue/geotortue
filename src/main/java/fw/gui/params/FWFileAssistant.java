/**
 * 
 */
package fw.gui.params;

import java.awt.Window;
import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import fw.app.Translator.TKey;
import fw.app.prefs.FWDirectoryEntry;
import fw.files.FileUtilities;
import fw.gui.FWOptionPane;
import fw.gui.FWOptionPane.ANSWER;
import fw.gui.FWOptionPane.OPTKey;

public class FWFileAssistant {
	
	private static final TKey UNTITLED = new TKey(FWFileAssistant.class, "untitledFile");
	private static final TKey DEFAULT_FILE_NAME = new TKey(FWFileAssistant.class, "defaultFileName");
	private static final TKey SELECT_FILE = new TKey(FWFileAssistant.class, "selectFile");

	private static final OPTKey OVERWRITE_FILE = new OPTKey(FWFileAssistant.class, "overwriteFile");
	private static final OPTKey INVALID_FILE = new OPTKey(FWFileAssistant.class, "invalidFile");		
	
	private Window owner;
	private boolean initialized = false;
	private final FWFile file;
	private final FWDirectoryEntry dir;
	private final FKey key;	
	
	public FWFileAssistant(final Window owner, final FKey key) {
		this.owner = owner;
		this.file = new FWFile(key.tag);
		this.key = key;
		this.dir = new FWDirectoryEntry(owner, key.tag);
		this.file.addParamaterListener(new FWParameterListener<File>() {
			@Override
			public void settingsChanged(File value) {
				dir.setValue(value);
				initialized = true;
			}
		});
	}
	
	public String getName() {
		File f = file.getValue();
		if (initialized) {
			return f.getName();
		}

		return UNTITLED.translate();
	}
	
	public File getFileForLoading() {
		JFileChooser chooser = getFileChooser(key.extensions);
		chooser.setCurrentDirectory(dir.getValue());

		if (chooser.showOpenDialog(owner) != JFileChooser.APPROVE_OPTION) {
			return null;
		}

		File f = chooser.getSelectedFile();
		if (f == null) {
			return getFileForLoading();
		}

		if (!f.exists() || !f.isFile()) {
			return null;
		}
		
		setValue(f);
		return f;
	}

	public File getFileForSaving() {
		JFileChooser chooser = getFileChooser(key.extensions[0]);
		if (initialized) {
			chooser.setSelectedFile(file.getValue());
		}
		else {
			chooser.setSelectedFile(new File(dir.getValue(), DEFAULT_FILE_NAME.translate()));
		}
		
		if (chooser.showSaveDialog(owner) != JFileChooser.APPROVE_OPTION) {
			return null;
		}

		File f = chooser.getSelectedFile();
		if (f == null) {
			return getFileForSaving();
		}

		f = FileUtilities.checkExtension(f, key.extensions[0]);

		if (f.exists()) {
			ANSWER answer = FWOptionPane.showConfirmDialog(owner, OVERWRITE_FILE);
			if (answer == ANSWER.NO) {
				return getFileForSaving();
			}
			if (answer == ANSWER.CANCEL) {
				return null;
			}
		} else {
			try {
				f.createNewFile();
			} catch (IOException e) {
				// do nothing
			}
		}
	
		if (!f.exists() || !f.canWrite() || !f.isFile()) {
			FWOptionPane.showErrorMessage(owner, INVALID_FILE);
			return getFileForSaving();
		}
			
		setValue(f);
		return f;
	}
	
	public File getFile() {
		if (initialized)
			return file.getValue();
		return getFileForSaving();
	}
	
	private JFileChooser getFileChooser(final String... exts) {
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter(key.translate(), exts);
		chooser.setFileFilter(filter);
		chooser.setDialogTitle(SELECT_FILE.translate());
		return chooser;
	}

	public void addParamaterListener(final FWParameterListener<File> l) {
		file.addParamaterListener(l);
	}

	public void setValue(final File f) {
		if (f.getName().endsWith(key.extensions[0]))
			file.setValue(f);
	}

	public static class FKey extends TKey {
		private final String[] extensions;

		private final String tag;

		public FKey(final Class<?> c, final String... extensions) {
			super(c, sum(extensions));
			this.extensions = extensions;
			this.tag = getTag(c);
		}
		
		public String getTag(final Class<?> c) {
			String t = c.getSimpleName();
			if (extensions.length>0) {
				t += "." + extensions[0];
			}
			t += ".path";
			return t;
		}
	}
	
	private static String sum(String... exts) {
		String sum = exts[0];
		for (int idx = 1; idx < exts.length; idx++) {
			sum += "." + exts[idx];
		}
		return sum;
	}
}
