/**
 * 
 */
package files;

import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.nfunk.jep.addon.JEPException;
import org.nfunk.jep.addon.JEPTroubleI;

import fw.app.FWManager;
import fw.app.FWRestrictedAccessException;
import fw.app.Translator.TKey;
import fw.app.prefs.FWDirectoryEntry;
import fw.app.prefs.FWWritableDirectoryEntry;
import fw.files.FileUtilities.HTTPException;
import fw.files.TextFile;
import fw.gui.FWLabel;
import fw.gui.FWOptionPane;
import fw.gui.FWOptionPane.ANSWER;
import fw.gui.FWOptionPane.OPTKey;
import fw.gui.FWSettings;
import fw.gui.FWSettingsActionPuller;
import fw.gui.layout.VerticalFlowLayout;
import fw.xml.XMLTagged;
import geotortue.gallery.Gallery;

/**
 * @author Salvatore Tummarello
 *
 */
public class GTUserFileManager implements FWSettings, XMLTagged {

	private static final TKey NAME = new TKey(GTUserFileManager.class, "settings");
	private static final TKey DIR = new TKey(GTUserFileManager.class, "dir");
	private static final OPTKey OVERWRITE = new OPTKey(GTUserFileManager.class, "overwrite");
	private static final OPTKey CHOOSE_DIR_KEY = new OPTKey(GTUserFileManager.class, "chooseDirectory");
	
	private final FWWritableDirectoryEntry directory;
	private final Window owner;
	
	public enum GTFileTrouble implements JEPTroubleI {GTFILE_IMPORT, GTFILE_EXPORT}
	
	public GTUserFileManager(Window owner) {
		this.owner = owner;
		File f;
		try {
			f = new File(FWManager.getConfigDirectory(), "export");
			f.mkdirs();
		} catch (FWRestrictedAccessException e) {
			f = null;
		}
		
		this.directory = new FWWritableDirectoryEntry(owner, "GTUserFile", CHOOSE_DIR_KEY);
	}
	
	public String importFile(String pathname) throws JEPException {
		
		try {
			URL url = getFile(pathname);
			TextFile f = new TextFile(url);
			return f.getText();
		} catch (HTTPException | IOException | IllegalArgumentException ex) {
			throw new JEPException(GTFileTrouble.GTFILE_IMPORT, pathname, ex.getMessage());
		}		
	}
	
	public boolean exportFile(String pathname, String content) throws JEPException {
		TextFile f = new TextFile(content);
		
		try {
			URL url = getFile(pathname);
			File file = new File(url.toURI());
			
			if (file.exists()) {
				ANSWER ans = FWOptionPane.showConfirmDialog(owner, OVERWRITE, pathname);
				if (ans != ANSWER.YES)
					return false;
			}
			
			f.write(file);
			return true;
			
		} catch (URISyntaxException | IllegalArgumentException | IOException ex) {
			throw new JEPException(GTFileTrouble.GTFILE_EXPORT, pathname, ex.getMessage());
		}
	}
	
	private URL getFile(String pathname) throws MalformedURLException {
		try {
			return new URL(pathname);
		} catch (MalformedURLException | IllegalArgumentException ex) {
			return new File(directory.getValue(), pathname).toURI().toURL();	
		}
	}
	
	@Override
	public String getXMLTag() {
		return "GTUserFileManager";
	}
	
	@Override
	public TKey getTitle() {
		return NAME;
	}
	
	@Override
	public JPanel getSettingsPane(final FWSettingsActionPuller actions) {
		return VerticalFlowLayout.createPanel(new FWLabel(DIR, SwingConstants.LEFT), directory.getComponent());
	}
}
