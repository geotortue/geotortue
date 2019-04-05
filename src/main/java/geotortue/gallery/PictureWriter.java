/** 
 * 
 */
package geotortue.gallery;

import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import fw.app.FWManager;
import fw.app.FWRestrictedAccessException;
import fw.app.Translator.TKey;
import fw.app.prefs.FWDirectoryEntry;
import fw.app.prefs.FWWritableDirectoryEntry;
import fw.files.FileUtilities;
import fw.gui.FWLabel;
import fw.gui.FWSettings;
import fw.gui.FWSettingsActionPuller;
import fw.gui.FWOptionPane.OPTKey;
import fw.gui.layout.VerticalFlowLayout;

/**
 *
 */
public class PictureWriter implements FWSettings {
	
	private static final TKey NAME = new TKey(PictureWriter.class, "settings");

	@Override
	public TKey getTitle() {
		return NAME;
	}

	private static final TKey DIR = new TKey(PictureWriter.class, "directory");
	private static OPTKey CHOOSE_DIR_KEY = new OPTKey(PictureWriter.class, "chooseDirectory");
	
	private final FWWritableDirectoryEntry directory;

	public PictureWriter(Window owner) {
		File f;
		try {
			f = new File(FWManager.getConfigDirectory(), "photos");
		} catch (FWRestrictedAccessException e) {
			f = null;
		}
		this.directory = new FWWritableDirectoryEntry(owner, "PictureWriter", CHOOSE_DIR_KEY); 
	}
	
	public void writePicture(final BufferedImage img, final String pattern) {
		new Thread(){
			@Override
			public void run() {
				try {
					File out = FileUtilities.getNewFile(directory.getValue(), pattern+"-%%%%%.png");					
					ImageIO.write(img, "png", out);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}.start();
	}
	
	@Override
	public JPanel getSettingsPane(final FWSettingsActionPuller actions) {
		return VerticalFlowLayout.createPanel(new FWLabel(DIR, SwingConstants.LEFT), directory.getComponent());
	}
	
}