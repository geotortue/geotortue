/**
 * 
 */
package fw.app;

import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;

import fw.app.Translator.TKey;
import fw.files.FileUtilities.HTTPException;
import fw.gui.FWButton.BKey;
import fw.gui.FWOptionPane.OPTKey;
import fw.gui.params.FWDirectory;
import fw.gui.params.FWFileAssistant;

/**
 * A singleton with only static data about the application: title, version, etc.
 * 
 * These data are made available through a set of static functions.
 * 
 * The singleton must be initialized by calling FWManager.init(FWApplicationI appI), and if needed
 * FWManager.setHeader(String path) and FWManager.setAccess(final List<String> argsList)
 * 
 * 
 * @author Salvatore Tummarello
 *
 */
public final class FWManager {
	
	private static File configDir;
	private static File userDir;
	private static boolean restricted = false;
	
	private static String appTitle;
	private static String appVersion;
	
	private static URL headerUrl;
	
	private static boolean ready = false;
	
	private static final TKey SELECT = new TKey(FWManager.class, "select");
	private static final BKey SELECT_FOLDER = new BKey(FWManager.class, "selectFolder");
	
	private static final String[] DEFAULT_PATHS = new String[]{
			System.getProperty("user.home"), 
			System.getProperty("user.dir"),
			System.getProperty("java.io.tmpdir")
			};
	
	// TODO : (done) --udir /home/euclide/blabla --cdir /home/euclide/myconfidir

	private FWManager() {}
	
	public static void setAccess(final List<String> argsList) {
		restrictIfRequired(argsList);
		if (!isRestricted())
			try {
				setConfigDir(argsList);
			} catch (FWRestrictedAccessException ex) {
				// cannot happen
			}
		setUserDir(argsList);
	}
	
	private static void restrictIfRequired(final List<String> args) {
		final String configTag = "--restrict";
		final int idx = args.indexOf(configTag);
		if (idx < 0) {
			return;
		}

		args.remove(configTag);
		restrict();
	}

	private static void setConfigDir(final List<String> args) throws FWRestrictedAccessException {
		final String configTag = "--cdir";
		final int idx = args.indexOf(configTag);
		if (idx < 0) {
			return;
		}

		if (idx < args.size() - 1) {  // c'est la valeur du paramètre qu'il s'agit de récupérer
			final String fileName = args.get(idx + 1);
			final File dir = new File(fileName);
			if (checkConfigDirectory(dir)) {
				configDir = dir;
				System.out.println("Custom configuration directory succesfully set: " + dir);
			} else {
				System.err.println("Invalid configuration directory: " + dir + "\nContinuing without configuration directory.");
				restrict();
			}
			args.remove(fileName);
		}
		args.remove(configTag);
	}
	
	private static void setUserDir(List<String> args) {
		String configTag = "--udir";
		int idx = args.indexOf(configTag);
		if (idx < 0) {
			return;
		}

		if (idx < args.size() - 1) { // c'est la valeur du paramètre qu'il s'agit de récupérer
			final String fileName = args.get(idx + 1);
			final File dir = new File(fileName);
			userDir = dir;
			System.out.println("Custom user directory succesfully setup: " + dir);
			args.remove(fileName);
		}
		args.remove(configTag);
	}
	
	private static boolean checkConfigDirectory(File dir) {
		return dir !=null && dir.exists() && dir.isDirectory() && dir.canRead() && dir.canWrite();
	}
	
	public static void init(final FWApplicationI appI) {
		if (ready) {
			new Exception("FWManager already setup").printStackTrace();
			return;
		}
		
		appTitle = appI.getName();
		appVersion = appI.getVersion();
		
		final boolean configDirNotInitialized = !isRestricted() && configDir == null;
		if (configDirNotInitialized) {
			initConfigDirectory(appI.getDefaultConfigDirName());
		}
		
		if (userDir == null)
			initUserDirectory();
		
		ready = true;
	}
	
	private static void initConfigDirectory(final String defaultConfigDirName) {
		final boolean configDirInitialized = isRestricted() || configDir != null;
		if (configDirInitialized) {
			return;
		}
				
		for (final String path : DEFAULT_PATHS) {
			final File dir = new File(path, defaultConfigDirName);
			dir.mkdirs();
			if (checkConfigDirectory(dir)) {
				configDir = dir;
				return;
			}

		System.err.println("Cannot find a writable directory for configuration files"
					+ " \nContinuing without configuration directory.");
		restrict();
		}
	}
	
	private static void initUserDirectory() {
		if (userDir != null) {
			return;
		}
		
		for (final String path : DEFAULT_PATHS) {
			final File dir = new File(path);
			if (dir.exists()) {
				userDir = dir;
				return;
			}
		}
		userDir = new File(".");
	}
	
	public static File getConfigDirectory() throws FWRestrictedAccessException {
		if (isRestricted()) {
			throw new FWRestrictedAccessException();
		}

		System.out.println("FWManager.getConfigDirectory() " + configDir );
		return configDir;
	}
	
	/**
	 * @param window
	 * @param string
	 * @return file
	 */
	public static File getSubDirectory(final Window owner, final String name) {
		final File currentDir = getUserDirectory(); 
		final JFileChooser chooser = new JFileChooser(currentDir);
		chooser.setDialogTitle(SELECT_FOLDER.translate());
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (chooser.showDialog(owner, SELECT.translate()) == JFileChooser.APPROVE_OPTION) {
			final File file = chooser.getSelectedFile();
			if (file != null) {
				return file;
			}
		}
		return null;
	}

	
	private static void restrict() {
		restricted = true;
	}

	public static boolean isRestricted() {
		return restricted;
	}
	
	public static File getUserDirectory() {
		return userDir;
	}

	public static URL getResource(final String str) {
		return FWManager.class.getResource(str);
	}

	public static BufferedImage getImage(final String path) {
		try {
			return ImageIO.read(getResource(path));
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}
	}
	public static String getApplicationTitle() {
		return appTitle;
	}

	/**
	 * @return
	 */
	public static String getApplicationVersion() {
		return appVersion;
	}

	/**
	 * @return
	 */
	public static URL getHeaderURL() {
		return headerUrl;
	}

	/**
	 * @param string
	 * @throws HTTPException 
	 */
	public static void setHeader(final String path) throws HTTPException {
		headerUrl = getResource(path);
	}
}
