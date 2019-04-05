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

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;

import fw.app.Translator.TKey;
import fw.files.FileUtilities.HTTPException;
import fw.gui.FWButton.BKey;
import fw.gui.FWOptionPane.OPTKey;
import fw.gui.params.FWDirectory;
import fw.gui.params.FWFileAssistant;

/**
 * A class containing static informations about the application : title, version, etc.
 * 
 * 
 * @author Salvatore Tummarello
 *
 */
public class FWManager {
	
	private static File CONFIG_DIR;
	private static File USER_DIR;
	private static boolean IS_RESTRICTED = false;
	
	private static String APP_TITLE;
	private static String APP_VERSION;
	
	private static URL HEADER_URL;
	
	private static boolean SET = false;
	
	private static final TKey SELECT = new TKey(FWManager.class, "select");
	private static final BKey SELECT_FOLDER = new BKey(FWManager.class, "selectFolder");
	
	private static String[] DEFAULT_PATHS = new String[]{
			System.getProperty("user.home"), 
			System.getProperty("user.dir"),
			System.getProperty("java.io.tmpdir")
			};
	
	// TODO : (done) --udir /home/euclide/blabla --cdir /home/euclide/myconfidir
	
	public static void setAccess(ArrayList<String> argsList) {
		restrictIfRequired(argsList);
		if (!isRestricted())
			try {
				setConfigDir(argsList);
			} catch (FWRestrictedAccessException ex) {
				// cannot happen
			}
		setUserDir(argsList);
	}
	
	private static void restrictIfRequired(ArrayList<String> args) {
		String configTag = "--restrict";
		int idx = args.indexOf(configTag);
		if (idx<0)
			return;
		
		args.remove(configTag);
		restrict();
	}

	private static void setConfigDir(ArrayList<String> args) throws FWRestrictedAccessException {
		String configTag = "--cdir";
		int idx = args.indexOf(configTag);
		if (idx<0)
			return;
		if (idx+1<args.size()) {
			String fileName = args.get(idx+1);
			File dir = new File(fileName);
			if (checkConfigDirectory(dir)) {
				CONFIG_DIR = dir;
				System.out.println("Custom configuration directory succesfully set : "+dir);
			} else {
				System.err.println("Invalid configuration directory : "+dir+"\nContinuing without configuration directory.");
				restrict();
			}
			args.remove(fileName);
		}
		args.remove(configTag);
	}
	
	private static void setUserDir(ArrayList<String> args) {
		String configTag = "--udir";
		int idx = args.indexOf(configTag);
		if (idx<0)
			return;
		if (idx+1<args.size()) {
			String fileName = args.get(idx+1);
			File dir = new File(fileName);
			USER_DIR = dir;
			System.out.println("Custom user directory succesfully set : "+dir);
			args.remove(fileName);
		}
		args.remove(configTag);
	}
	
	private static boolean checkConfigDirectory(File dir) {
		return dir !=null && dir.exists() && dir.isDirectory() && dir.canRead() && dir.canWrite();
	}
	
	public static void init(FWApplicationI appI) {
		if (SET) {
			new Exception("FWManager already set").printStackTrace();
			return;
		}
		
		APP_TITLE = appI.getName();
		APP_VERSION = appI.getVersion();
		
		if (!isRestricted()) {
			if (CONFIG_DIR == null)
				initConfigDirectory(appI.getDefaultConfigDirName());
		}
		
		
		if (USER_DIR == null)
			initUserDirectory();
		
		SET = true;
	}
	
	private static void initConfigDirectory(String defaultConfigDirName) {
		if (isRestricted())
			return;
		
		if (CONFIG_DIR != null)
			return;
		
		for (String path : DEFAULT_PATHS) {
			File dir  = new File(path, defaultConfigDirName);
			dir.mkdirs();
			if (checkConfigDirectory(dir)) {
				CONFIG_DIR = dir;
				return;
			}

		System.err.println("Cannont find a writable directory for configuration files"
					+ " \nContinuing without configuration directory.");
		restrict();
		}
	}
	
	private static void initUserDirectory() {
		if (USER_DIR != null)
			return;
		
		for (String path : DEFAULT_PATHS) {
			File dir  = new File(path);
			if (dir.exists()) {
				USER_DIR = dir;
				return;
			}
		}
		USER_DIR = new File(".");
		
	}
	
	final public static File getConfigDirectory() throws FWRestrictedAccessException {
		if (isRestricted())
			throw new FWRestrictedAccessException();
		System.out.println("FWManager.getConfigDirectory() "+CONFIG_DIR );
		return CONFIG_DIR;
	}
	
	/**
	 * @param string
	 * @return
	 */
	public static File getSubDirectory(Window owner, String name) {
		File currentDir = getUserDirectory(); 
		JFileChooser chooser = new JFileChooser(currentDir);
		chooser.setDialogTitle(SELECT_FOLDER.translate());
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (chooser.showDialog(owner, SELECT.translate()) == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			if (file!=null)
				return file;
		}
		return null;
	}

	
	private static void restrict() {
		IS_RESTRICTED = true;
	}

	public static boolean isRestricted() {
		return IS_RESTRICTED;
	}
	
	final public static File getUserDirectory() {
		return USER_DIR;
	}

	public static URL getResource(String str) {
		return FWManager.class.getResource(str);
	}

	public static BufferedImage getImage(String path) {
		try {
			return ImageIO.read(getResource(path));
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}
	}
	public static String getApplicationTitle() {
		return APP_TITLE;
	}

	/**
	 * @return
	 */
	public static String getApplicationVersion() {
		return APP_VERSION;
	}

	/**
	 * @return
	 */
	public static URL getHeaderURL() {
		return HEADER_URL;
	}

	/**
	 * @param string
	 * @throws IOException 
	 * @throws HTTPException 
	 */
	public static void setHeader(String path) throws HTTPException, IOException {
		HEADER_URL = getResource(path);
	}
}
